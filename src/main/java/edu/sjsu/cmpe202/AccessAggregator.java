package edu.sjsu.cmpe202;

import java.util.*;

public final class AccessAggregator implements Aggregator {
    private record Attempt(String user, String result, int duration) {}
    private final Map<String, List<Attempt>> doors = new TreeMap<>();
    public void accept(ParsedLine line) { doors.computeIfAbsent(line.get("door"), d -> new ArrayList<>()).add(new Attempt(line.get("user"), line.get("result"), Integer.parseInt(line.get("duration_ms")))); }
    public Map<String,Object> result() {
        Map<String,Object> out = new TreeMap<>();
        doors.forEach((door, attempts) -> { List<Integer> times = attempts.stream().map(Attempt::duration).sorted().toList(); Map<String,Object> decision = new LinkedHashMap<>();
            decision.put("min", times.get(0)); decision.put("50_percentile", percentile(times, .50)); decision.put("90_percentile", percentile(times, .90)); decision.put("99_percentile", percentile(times, .99)); decision.put("max", times.get(times.size()-1));
            Map<String,Integer> results = new LinkedHashMap<>(); results.put("GRANTED", 0); results.put("DENIED", 0); Map<String,Integer> denied = new TreeMap<>();
            attempts.forEach(a -> { results.computeIfPresent(a.result(), (k,v)->v+1); if (a.result().equals("DENIED")) denied.merge(a.user(), 1, Integer::sum); });
            Map<String,Object> record = new LinkedHashMap<>(); record.put("decision_times", decision); record.put("results", results); record.put("flagged_users", denied.entrySet().stream().filter(e -> e.getValue() >= 3).map(Map.Entry::getKey).toList()); out.put(door, record); });
        return out;
    }
    private static int percentile(List<Integer> sorted, double p) { int index = (int)Math.ceil(p * sorted.size()) - 1; return sorted.get(Math.max(0, index)); }
}
