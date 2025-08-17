package net.coosanta.meldmc.mod.modlist;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class ClientModScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModScanner.class);
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CACHE_FILE_NAME = "client-mods.json";

    private static boolean cacheModified = false;
    private static final Map<String, ClientMod> modsMap = new HashMap<>();

    public static Map<String, ClientMod> scanClientMods(Path clientModsDir) {
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

    private static void checkDirectory(Path directory) {
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

    private static void loadModsCache(Path cacheFile) {
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

    private static void saveModsCache(Path cacheFile) { // TODO: To prevent tampering of files, do not save and instead reload certain fields.
        try {
            String jsonContent = GSON.toJson(modsMap);
            Files.writeString(cacheFile, jsonContent, StandardCharsets.UTF_8);
            LOGGER.info("Saved {} mod entries to cache", modsMap.size());
        } catch (IOException e) {
            LOGGER.error("Failed to save mods cache", e);
        }
    }

    private static Map<String, ClientMod> scanCurrentModFiles(Path directory) {
        Map<String, ClientMod> currentMods = new HashMap<>();

        try (var pathStream = Files.list(directory)) {
            pathStream
                    .filter(path -> path.toString().toLowerCase().endsWith(".jar"))
                    .forEach(path -> scanCurrentModFile(path, currentMods));
            LOGGER.info("Found {} mod files in directory", currentMods.size());
        } catch (IOException e) {
            LOGGER.error("Error scanning client-mods directory", e);
        }

        return currentMods;
    }

    private static void scanCurrentModFile(Path path, Map<String, ClientMod> currentMods) {
        try {
            File jarFile = path.toFile();
            String hash = calculateSHA512(jarFile);
            String fileName = path.getFileName().toString();
            long fileSize = jarFile.length();

            String modsToml = readModsToml(jarFile);

            String version = "unknown";
            String modname = "unknown";
            String modId = "unknown";
            String authors = "unknown";
            String description = "No description";

            if (modsToml != null) {
                try {
                    TomlParser parser = new TomlParser();
                    UnmodifiableConfig config = parser.parse(new StringReader(modsToml));

                    Object modsObj = config.get("mods");
                    if (modsObj instanceof List<?> modsList) { // TODO: Multi-mod files
                        if (!modsList.isEmpty() && modsList.get(0) instanceof UnmodifiableConfig firstMod) {
                            version = firstMod.get("version");
                            if (version.equals("${file.jarVersion}")) {
                                String implVersion = getImplementationVersion(jarFile);
                                if (implVersion != null) version = implVersion;
                            }
                            modname = firstMod.get("displayName");
                            modId = firstMod.get("modId");
                            authors = firstMod.getOrElse("authors", authors);
                            description = firstMod.getOrElse("description", description);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to parse mods.toml for {}: {}", fileName, e.getMessage());
                }
            }

            ClientMod mod = new ClientMod(version, hash, fileName, modname, modId, authors, description, fileSize);
            currentMods.put(hash, mod);
            LOGGER.debug("Found mod file {}: {}", fileName, hash);
        } catch (Exception e) {
            LOGGER.error("Error processing file: {}", path.getFileName(), e);
        }
    }

    public static String getImplementationVersion(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                return manifest.getMainAttributes().getValue("Implementation-Version");
            }
        } catch (Exception e) {
            LOGGER.error("Error getting mod implementation version for mod: {}", jarFile.toString(), e);
        }
        return null;
    }

    private static List<String> detectChanges(Map<String, ClientMod> currentMods) {
        List<String> newOrChangedHashes = new ArrayList<>();

        for (Map.Entry<String, ClientMod> entry : currentMods.entrySet()) {
            String hash = entry.getKey();
            ClientMod currentMod = entry.getValue();
            ClientMod cachedMod = modsMap.get(hash);

            if (cachedMod == null) { // New file
                newOrChangedHashes.add(hash);
                modsMap.put(hash, currentMod);
                cacheModified = true;
                LOGGER.debug("New mod file detected: {}", currentMod.getFilename());
            } else if (!cachedMod.getFilename().equals(currentMod.getFilename())) { // Changed file metadata
                cachedMod.setFilename(currentMod.getFilename());
                cacheModified = true;
                LOGGER.debug("Modified mod file metadata detected: {}", currentMod.getFilename());
            }
        }

        return newOrChangedHashes;
    }

    private static void removeDeletedMods(Map<String, ClientMod> currentMods) {
        List<String> toRemove = new ArrayList<>();

        for (String hash : modsMap.keySet()) {
            if (!currentMods.containsKey(hash)) {
                toRemove.add(hash);
            }
        }

        if (!toRemove.isEmpty()) {
            for (String hash : toRemove) {
                ClientMod removedMod = modsMap.remove(hash);
                LOGGER.debug("Removed deleted mod file: {}", removedMod.getFilename());
            }
            cacheModified = true;
            LOGGER.info("Removed {} deleted mod entries from cache", toRemove.size());
        }
    }

    private static String calculateSHA512(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        try (InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        return HexFormat.of().formatHex(hashBytes);
    }

    private static Map<String, Map<String, Object>> sendHashesToModrinth(List<String> hashes) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("algorithm", "sha512");

            JsonArray hashesArray = new JsonArray();
            for (String hash : hashes) {
                hashesArray.add(hash);
            }
            requestBody.add("hashes", hashesArray);

            String jsonPayload = GSON.toJson(requestBody);

            HttpURLConnection connection = setUpHttpConnection(MODRINTH_API_URL + "version_files", "POST", jsonPayload);

            return getConnectionResponse(connection, new TypeToken<>() {
            });

        } catch (Exception e) {
            LOGGER.error("Error communicating with Modrinth API", e);
        }
        return null;
    }

    private static <T> @Nullable T getConnectionResponse(HttpURLConnection connection, TypeToken<T> returnType) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return GSON.fromJson(response.toString(), returnType);
            }
        } else {
            LOGGER.error("Failed to get response from Modrinth API. Response code: {}", responseCode);
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.error("Response error: {}", response);
                }
            }
            return null;
        }
    }

    public static void editClientModsFromResponse(Map<String, Map<String, Object>> modInfo) {
        var idMap = new HashMap<String, String>();

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
            String projectId = (String) entry.getValue().get("project_id");

            if (url != null || projectId != null) {
                ClientMod mod = modsMap.get(hash);
                mod.setUrl(url);
                mod.setProjectId(projectId);
                idMap.put(projectId, hash);
            }
        }

        getProjectUrlsFromProjectIds(idMap);
    }

    private static void getProjectUrlsFromProjectIds(Map<String, String> projectIds) { // Key: ID, Value: Hash
        LOGGER.debug("Getting project URLs from {} project IDs", projectIds.size());

        if (projectIds.isEmpty()) return;

        JsonArray projects = new JsonArray();
        for (var projectId : projectIds.keySet()) {
            projects.add(projectId);
        }

        String jsonPayload = GSON.toJson(projects);
        String encodedIds = URLEncoder.encode(jsonPayload, StandardCharsets.UTF_8);
        String urlString = MODRINTH_API_URL + "projects?ids=" + encodedIds; // TODO: Batching long IDs.
        try {
            HttpURLConnection connection = setUpHttpConnection(urlString, "GET");

            List<Map<String, Object>> projectsResponse = getConnectionResponse(connection, new TypeToken<>() {
            });

            if (projectsResponse == null) {
                throw new NullPointerException("Null response from Modrinth API.");
            }

            for (var project : projectsResponse) {
                if (project.get("project_type") instanceof String projectType &&
                        project.get("slug") instanceof String projectSlug &&
                        project.get("id") instanceof String projectId) {

                    String projectUrl = "https://modrinth.com/" + projectType + "/" + projectSlug;

                    modsMap.get(projectIds.get(projectId)).setProjectUrl(projectUrl);
                } else {
                    throw new IllegalArgumentException("Invalid project data from Modrinth API.");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get project URLs from Modrinth", e);
        }
    }

    private static HttpURLConnection setUpHttpConnection(String urlString, String method) throws IOException {
        return setUpHttpConnection(urlString, method, null);
    }

    private static HttpURLConnection setUpHttpConnection(String urlString, String method, @Nullable String jsonPayload) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        if (jsonPayload != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        return connection;
    }

    public static String readModsToml(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            ZipEntry entry = jar.getEntry("META-INF/mods.toml");
            if (entry != null) {
                try (InputStream is = jar.getInputStream(entry);
                     Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
                    return scanner.useDelimiter("\\A").next();
                }
            }
        }
        return null;
    }
}
