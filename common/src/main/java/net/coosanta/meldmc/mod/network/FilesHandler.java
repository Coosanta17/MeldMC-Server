package net.coosanta.meldmc.mod.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.coosanta.meldmc.mod.modsinfo.MeldData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static net.coosanta.meldmc.mod.MeldMC.LOGGER;

public class FilesHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ServerConfig config;
    private final MeldData meldData;

    public FilesHandler(ServerConfig config, MeldData meldData) {
        this.config = config;
        this.meldData = meldData;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method not allowed");
            return;
        }

        try {
            String requestBody = readRequestBody(exchange);
            Collection<String> requestedHashes = objectMapper.readValue(requestBody, new TypeReference<>() {
            });

            if (requestedHashes == null || requestedHashes.isEmpty()) {
                sendErrorResponse(exchange, 400, "No file hashes provided");
                return;
            }

            for (String hash : requestedHashes) {
                if (!isValidSha512(hash)) {
                    sendErrorResponse(exchange, 400, "Invalid hash format: " + hash);
                    return;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int filesFound = createZipResponse(baos, requestedHashes);

            if (filesFound == 0) {
                sendErrorResponse(exchange, 404, "No requested files found");
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"files.zip\"");
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");

            byte[] responseBytes = baos.toByteArray();
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            LOGGER.info("Files request served to {} - {} files sent", exchange.getRemoteAddress(), filesFound);
        } catch (Exception e) {
            LOGGER.error("Error handling files request", e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private boolean isValidSha512(String hash) {
        return hash != null && hash.matches("^[a-fA-F0-9]{128}$");
    }

    private int createZipResponse(ByteArrayOutputStream baos, Collection<String> requestedHashes)
            throws IOException {
        int filesFound = 0;
        var modMap = meldData.modMap();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String hash : requestedHashes) {
                if (!modMap.containsKey(hash)) {
                    LOGGER.warn("Failed to find requested file with hash: {}", hash);
                    continue;
                }
                String fileName = modMap.get(hash).getFilename();
                Path filePath = config.filesDirectory().resolve(fileName);

                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                Files.copy(filePath, zos);
                zos.closeEntry();

                filesFound++;
            }
        }
        return filesFound;
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] response = message.getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
