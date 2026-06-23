package com.medqueue.server;

import java.util.*;

/**
 * Minimal JSON parser — handles flat objects and arrays of objects.
 * No external libraries needed.
 */
public class JsonParser {

    /** Parse a JSON object into a Map<String, String> */
    public static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.lastIndexOf("}"));

        List<String> tokens = splitTopLevel(json);
        for (String token : tokens) {
            int colon = token.indexOf(':');
            if (colon < 0) continue;
            String key = token.substring(0, colon).trim().replaceAll("^\"|\"$", "");
            String val = token.substring(colon + 1).trim();
            if (val.startsWith("[")) {
                map.put(key, parseArray(val));
            } else if (val.startsWith("{")) {
                map.put(key, parseObject(val));
            } else if (val.startsWith("\"")) {
                map.put(key, val.replaceAll("^\"|\"$", "").replace("\\\"", "\""));
            } else if (val.equals("null")) {
                map.put(key, null);
            } else if (val.equals("true") || val.equals("false")) {
                map.put(key, Boolean.parseBoolean(val));
            } else {
                try { map.put(key, Double.parseDouble(val)); }
                catch (NumberFormatException e) { map.put(key, val); }
            }
        }
        return map;
    }

    /** Parse a JSON array into a List */
    public static List<Object> parseArray(String json) {
        List<Object> list = new ArrayList<>();
        if (json == null || json.isBlank()) return list;
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.lastIndexOf("]"));
        for (String item : splitTopLevel(json)) {
            String v = item.trim();
            if (v.startsWith("{")) list.add(parseObject(v));
            else if (v.startsWith("[")) list.add(parseArray(v));
            else if (v.startsWith("\"")) list.add(v.replaceAll("^\"|\"$", ""));
            else if (v.equals("null")) list.add(null);
            else { try { list.add(Double.parseDouble(v)); } catch (Exception e) { list.add(v); } }
        }
        return list;
    }

    /** Get string value safely */
    public static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : v.toString();
    }

    /** Get int value safely */
    public static int intVal(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        if (v == null) return def;
        try { return (int) Double.parseDouble(v.toString()); }
        catch (Exception e) { return def; }
    }

    /** Get double value safely */
    public static double dbl(Map<String, Object> m, String key, double def) {
        Object v = m.get(key);
        if (v == null) return def;
        try { return Double.parseDouble(v.toString()); }
        catch (Exception e) { return def; }
    }

    /** Split JSON tokens at the top level (respecting nested braces/brackets) */
    private static List<String> splitTopLevel(String s) {
        List<String> parts = new ArrayList<>();
        int depth = 0; boolean inStr = false; int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i-1) != '\\')) inStr = !inStr;
            if (!inStr) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == ',' && depth == 0) {
                    parts.add(s.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }
        if (start < s.length()) parts.add(s.substring(start).trim());
        return parts;
    }

    /** Build a simple JSON response */
    public static String ok(String message) {
        return "{\"message\":\"" + message + "\"}";
    }

    public static String error(String message) {
        return "{\"error\":\"" + message + "\"}";
    }

    public static String listToJson(List<?> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toString());
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
