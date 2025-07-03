package net.coosanta.meldmc.mod;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientModScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModScanner.class);
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/version_files";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, ClientMod> modsMap = new HashMap<>();

    public void scanClientMods(Path clientModsDir) {
        checkDirectory(clientModsDir);

        List<String> hashes = scanModFilesAndAddToModsMap(clientModsDir);

        if (!hashes.isEmpty()) {
            Map<String, Map<String, Object>> response = sendHashesToModrinth(hashes);
            if (response != null) {
                editClientModsFromResponse(response);
            }
        } else {
            LOGGER.info("No mod files found in client-mods directory");
        }
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

    private List<String> scanModFilesAndAddToModsMap(Path directory) {
        List<String> hashes = new ArrayList<>();

        try (var pathStream = Files.list(directory)) {
            pathStream
                    .filter(path -> path.toString().toLowerCase().endsWith(".jar"))
                    .forEach(path -> {
                        try {
                            String hash = calculateSHA512(path.toFile());
                            hashes.add(hash);
                            modsMap.put(hash, new ClientMod(hash));
                            LOGGER.debug("Added hash for file {}: {}", path.getFileName(), hash);
                        } catch (Exception e) {
                            LOGGER.error("Error processing file: {}", path.getFileName(), e);
                        }
                    });

            LOGGER.info("Scanned {} mod files", hashes.size());
        } catch (IOException e) {
            LOGGER.error("Error scanning client-mods directory", e);
        }

        return hashes;
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
