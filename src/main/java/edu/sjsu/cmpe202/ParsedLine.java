package edu.sjsu.cmpe202;

import java.util.Map;

public record ParsedLine(Map<String, String> fields) {
    public String get(String key) { return fields.get(key); }
    public boolean has(String key) { return fields.containsKey(key); }
}
