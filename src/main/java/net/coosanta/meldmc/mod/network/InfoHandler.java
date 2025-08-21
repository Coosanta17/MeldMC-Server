package net.coosanta.meldmc.mod.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.coosanta.meldmc.mod.MeldMC;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static net.coosanta.meldmc.mod.MeldMC.LOGGER;

public class InfoHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InfoHandler() {
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method not allowed");
            return;
        }

        try {
            var info = MeldMC.getMeldData();
            String jsonResponse = objectMapper.writeValueAsString(info);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");

            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            LOGGER.info("Mod info request served to {}", exchange.getRemoteAddress());

        } catch (Exception e) {
            LOGGER.error("Error handling info request", e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
