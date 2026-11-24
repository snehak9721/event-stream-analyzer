package edu.sjsu.cmpe202;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parses one key=value line. A malformed line is represented by an empty result. */
public final class KeyValueParser {
    public ParsedLine parse(String line) {
        if (line == null || line.isBlank()) return new ParsedLine(Map.of());
        Map<String, String> fields = new LinkedHashMap<>();
        int i = 0;
        while (i < line.length()) {
            while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
            if (i == line.length()) break;
            int keyStart = i;
            while (i < line.length() && !Character.isWhitespace(line.charAt(i)) && line.charAt(i) != '=') i++;
            if (i == keyStart || i >= line.length() || line.charAt(i) != '=') return new ParsedLine(Map.of());
            String key = line.substring(keyStart, i);
            i++;
            if (i >= line.length()) return new ParsedLine(Map.of());
            String value;
            if (line.charAt(i) == '"') {
                i++;
                int valueStart = i;
                while (i < line.length() && line.charAt(i) != '"') i++;
                if (i >= line.length()) return new ParsedLine(Map.of());
                value = line.substring(valueStart, i++);
                if (i < line.length() && !Character.isWhitespace(line.charAt(i))) return new ParsedLine(Map.of());
            } else {
                int valueStart = i;
                while (i < line.length() && !Character.isWhitespace(line.charAt(i))) i++;
                value = line.substring(valueStart, i);
            }
            if (key.isBlank() || value.isBlank() || fields.containsKey(key)) return new ParsedLine(Map.of());
            fields.put(key, value);
        }
        if (!fields.containsKey("ts")) return new ParsedLine(Map.of());
        return new ParsedLine(Map.copyOf(fields));
    }
}
