package edu.sjsu.cmpe202;

import java.util.*;

public final class DeviceAggregator implements Aggregator {
    private static final List<String> ORDER = List.of("CRITICAL", "ERROR", "WARN", "INFO");
    private final Map<String, Integer> severityCounts = new LinkedHashMap<>();
    private final Map<String, Map<String, Integer>> deviceCounts = new TreeMap<>();
    public DeviceAggregator() { ORDER.forEach(s -> severityCounts.put(s, 0)); }
    public void accept(ParsedLine line) {
        String severity = line.get("severity"), device = line.get("device");
        severityCounts.computeIfPresent(severity, (k, v) -> v + 1);
        deviceCounts.computeIfAbsent(device, d -> { Map<String,Integer> m = new LinkedHashMap<>(); ORDER.forEach(s -> m.put(s, 0)); return m; }).computeIfPresent(severity, (k,v) -> v + 1);
    }
    public Map<String, Object> result() {
        if (deviceCounts.isEmpty()) return new TreeMap<>();
        Map<String,Object> out = new LinkedHashMap<>(); out.put("by_severity", new LinkedHashMap<>(severityCounts));
        Map<String,Object> devices = new TreeMap<>();
        deviceCounts.forEach((device, counts) -> { String best = ORDER.get(0); for (String s : ORDER) if (counts.get(s) > counts.get(best)) best = s;
            Map<String,Object> d = new LinkedHashMap<>(); d.put("total", counts.values().stream().mapToInt(Integer::intValue).sum()); d.put("most_frequent_severity", best); devices.put(device, d); });
        out.put("by_device", devices); return out;
    }
}
