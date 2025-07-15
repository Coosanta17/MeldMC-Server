package net.coosanta.meldmc.mod.modlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ClientModScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModScanner.class);
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/version_files";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CACHE_FILE_NAME = "client-mods.json";

    private boolean cacheModified = false;
    private final Map<String, ClientMod> modsMap = new HashMap<>();

    public Map<String, ClientMod> scanClientMods(Path clientModsDir) {
        checkDirectory(clientModsDir);

        Path cacheFile = clientModsDir.resolve(CACHE_FILE_NAME);
        loadModsCache(cacheFile);

        Map<String, ClientMod> currentMods = scanCurrentModFiles(clientModsDir);
        List<String> newOrChangedHashes = detectChanges(currentMods);

        removeDeletedMods(currentMods);

        if (!newOrChangedHashes.isEmpty()) {
            LOGGER.info("Querying Modrinth API for {} new or changed mod files", newOrChangedHashes.size());
            Map<String, Map<String, Object>> response = sendHashesToModrinth(newOrChangedHashes);
            if (response != null) {
                editClientModsFromResponse(response);
            }
        } else {
            LOGGER.info("No new or changed mod files found");
        }

        if (cacheModified) {
            saveModsCache(cacheFile);
        }

        return modsMap;
    }

    private void checkDirectory(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
                LOGGER.info("Created client-mods directory at: {}", directory.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.error("Failed to create client-mods directory", e);
            }
        } else {
            LOGGER.debug("Found client-mods directory: {}", directory.toAbsolutePath());
        }
    }

    private void loadModsCache(Path cacheFile) {
        if (!Files.exists(cacheFile)) {
            LOGGER.debug("No existing cache file found at: {}", cacheFile);
            return;
        }

        try {
            String jsonContent = Files.readString(cacheFile, StandardCharsets.UTF_8);
            Type mapType = new TypeToken<Map<String, ClientMod>>() {
            }.getType();
            Map<String, ClientMod> cachedMods = GSON.fromJson(jsonContent, mapType);

            if (cachedMods != null) {
                modsMap.putAll(cachedMods);
                LOGGER.info("Loaded {} cached mod entries", cachedMods.size());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load mods cache", e);
        } catch (Exception e) {
            LOGGER.error("Failed to parse mods cache JSON", e);
        }
    }

    private void saveModsCache(Path cacheFile) {
        try {
            String jsonContent = GSON.toJson(modsMap);
            Files.writeString(cacheFile, jsonContent, StandardCharsets.UTF_8);
            LOGGER.info("Saved {} mod entries to cache", modsMap.size());
        } catch (IOException e) {
            LOGGER.error("Failed to save mods cache", e);
        }
    }

    private Map<String, ClientMod> scanCurrentModFiles(Path directory) {
        Map<String, ClientMod> currentMods = new HashMap<>();

        try (var pathStream = Files.list(directory)) {
            pathStream
                    .filter(path -> path.toString().toLowerCase().endsWith(".jar"))
                    .forEach(path -> {
                        try {
                            String hash = calculateSHA512(path.toFile());
                            String fileName = path.getFileName().toString();

                            ClientMod mod = new ClientMod(hash, fileName);
                            currentMods.put(hash, mod);
                            LOGGER.debug("Found mod file {}: {}", fileName, hash);
                        } catch (Exception e) {
                            LOGGER.error("Error processing file: {}", path.getFileName(), e);
                        }
                    });

            LOGGER.info("Found {} mod files in directory", currentMods.size());
        } catch (IOException e) {
            LOGGER.error("Error scanning client-mods directory", e);
        }

        return currentMods;
    }

    private List<String> detectChanges(Map<String, ClientMod> currentMods) {
        List<String> newOrChangedHashes = new ArrayList<>();

        for (Map.Entry<String, ClientMod> entry : currentMods.entrySet()) {
            String hash = entry.getKey();
            ClientMod currentMod = entry.getValue();
            ClientMod cachedMod = modsMap.get(hash);

            if (cachedMod == null) { // New file
                newOrChangedHashes.add(hash);
                modsMap.put(hash, currentMod);
                cacheModified = true;
                LOGGER.debug("New mod file detected: {}", currentMod.getFileName());
            } else if (!cachedMod.getFileName().equals(currentMod.getFileName())) { // Changed file metadata
                cachedMod.setFileName(currentMod.getFileName());
                cacheModified = true;
                LOGGER.debug("Modified mod file metadata detected: {}", currentMod.getFileName());
            }
        }

        return newOrChangedHashes;
    }

    private void removeDeletedMods(Map<String, ClientMod> currentMods) {
        List<String> toRemove = new ArrayList<>();

        for (String hash : modsMap.keySet()) {
            if (!currentMods.containsKey(hash)) {
                toRemove.add(hash);
            }
        }

        if (!toRemove.isEmpty()) {
            for (String hash : toRemove) {
                ClientMod removedMod = modsMap.remove(hash);
                LOGGER.debug("Removed deleted mod file: {}", removedMod.getFileName());
            }
            cacheModified = true;
            LOGGER.info("Removed {} deleted mod entries from cache", toRemove.size());
        }
    }

    private String calculateSHA512(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] hashBytes = digest.digest(fileBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Map<String, Map<String, Object>> sendHashesToModrinth(List<String> hashes) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("algorithm", "sha512");

            JsonArray hashesArray = new JsonArray();
            for (String hash : hashes) {
                hashesArray.add(hash);
            }
            requestBody.add("hashes", hashesArray);

            String jsonPayload = GSON.toJson(requestBody);

            HttpURLConnection connection = setUpHttpConnection(jsonPayload);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
                    }.getType();
                    return GSON.fromJson(response.toString(), mapType);
                }
            } else {
                LOGGER.error("Failed to get response from Modrinth API. Response code: {}", responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.error("Error response: {}", response);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error communicating with Modrinth API", e);
        }
        return null;
    }

    public void editClientModsFromResponse(Map<String, Map<String, Object>> modInfo) {
        for (Map.Entry<String, Map<String, Object>> entry : modInfo.entrySet()) {
            String hash = entry.getKey();

            if (!modsMap.containsKey(hash)) {
                LOGGER.error("Unknown hash returned from API: {}", hash);
                continue;
            }

            Map<String, Object> modData = entry.getValue();

            @SuppressWarnings("unchecked") // I trust you Modrinth!!
            List<Map<String, Object>> files = (List<Map<String, Object>>) modData.get("files");
            if (files == null || files.isEmpty()) {
                LOGGER.warn("No file found for mod with hash: {}", hash);
                continue;
            }

            // Get primary file in files list
            Map<String, Object> primaryFile = null;
            for (Map<String, Object> file : files) {
                if (Boolean.TRUE.equals(file.get("primary"))) {
                    primaryFile = file;
                    break;
                }
            }

            // If no primary file found, use the first one - extreme edge case (I trust you Modrinth!!)
            if (primaryFile == null) {
                primaryFile = files.get(0);
            }

            String url = (String) primaryFile.get("url");

            if (url != null) {
                ClientMod mod = modsMap.get(hash);
                mod.setUrl(url);
                modsMap.replace(hash, mod);
            }
        }
    }

    private HttpURLConnection setUpHttpConnection(String jsonPayload) throws IOException {
        URL url = new URL(MODRINTH_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }
}
