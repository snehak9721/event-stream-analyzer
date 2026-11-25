package edu.sjsu.cmpe202;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class SensorAggregator implements Aggregator {
    private final Map<String, List<Double>> values = new TreeMap<>();
    public void accept(ParsedLine line) { values.computeIfAbsent(line.get("sensor"), ignored -> new ArrayList<>()).add(Double.parseDouble(line.get("reading"))); }
    public Map<String, Object> result() {
        Map<String, Object> out = new TreeMap<>();
        for (var entry : values.entrySet()) {
            List<Double> sorted = new ArrayList<>(entry.getValue()); sorted.sort(Double::compare);
            double sum = sorted.stream().mapToDouble(Double::doubleValue).sum(); double avg = sum / sorted.size();
            double variance = sorted.stream().mapToDouble(v -> (v - avg) * (v - avg)).sum() / sorted.size();
            Map<String, Object> stats = new LinkedHashMap<>(); stats.put("count", sorted.size());
            stats.put("minimum", rounded(sorted.get(0))); stats.put("median", rounded(median(sorted)));
            stats.put("average", rounded(avg)); stats.put("maximum", rounded(sorted.get(sorted.size() - 1)));
            stats.put("std_dev", rounded(Math.sqrt(variance))); out.put(entry.getKey(), stats);
        }
        return out;
    }
    private static double median(List<Double> v) { int n = v.size(), mid = n / 2; return n % 2 == 1 ? v.get(mid) : (v.get(mid - 1) + v.get(mid)) / 2.0; }
    static Number rounded(double value) {
    BigDecimal rounded = BigDecimal.valueOf(value)
            .setScale(1, RoundingMode.HALF_UP);

    BigDecimal minLong = BigDecimal.valueOf(Long.MIN_VALUE);
    BigDecimal maxLong = BigDecimal.valueOf(Long.MAX_VALUE);

    if (rounded.stripTrailingZeros().scale() <= 0
            && rounded.compareTo(minLong) >= 0
            && rounded.compareTo(maxLong) <= 0) {
        return rounded.longValueExact();
    }

    return rounded.doubleValue();
}
}
