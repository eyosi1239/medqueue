package com.medqueue.server;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Base HTTP handler — handles routing, CORS, request parsing, response writing.
 * All controllers extend this class (OOP inheritance).
 */
public abstract class BaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // CORS headers
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PATCH,DELETE,OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        ex.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equals(ex.getRequestMethod())) {
            sendResponse(ex, 200, "{}");
            return;
        }

        try {
            route(ex);
        } catch (Exception e) {
            sendResponse(ex, 500, JsonParser.error("Internal error: " + e.getMessage()));
        }
    }

    /** Subclasses implement routing logic here */
    protected abstract void route(HttpExchange ex) throws IOException;

    /** Read request body */
    protected String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Send JSON response */
    protected void sendResponse(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    /** Parse query parameters from URL */
    protected Map<String, String> queryParams(HttpExchange ex) {
        Map<String, String> params = new HashMap<>();
        String query = ex.getRequestURI().getQuery();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) params.put(kv[0], kv[1]);
        }
        return params;
    }

    /** Extract path segment after base path */
    protected String pathId(HttpExchange ex, String basePath) {
        String path = ex.getRequestURI().getPath();
        String after = path.substring(basePath.length());
        if (after.startsWith("/")) after = after.substring(1);
        return after.isEmpty() ? null : after.split("/")[0];
    }

    /** Check HTTP method */
    protected boolean isMethod(HttpExchange ex, String method) {
        return method.equals(ex.getRequestMethod());
    }
}
