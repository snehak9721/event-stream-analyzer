package edu.sjsu.cmpe202;

import java.util.List;

public final class ClassifierChainFactory {
    private ClassifierChainFactory() {}
    public static RecordClassifier createDefault() {
        return new ClassifierChain(List.of(new SensorClassifier(), new DeviceClassifier(), new AccessClassifier()));
    }
    private static boolean validDouble(String value) {
        try { return value != null && Double.isFinite(Double.parseDouble(value)); }
        catch (NumberFormatException e) { return false; }
    }
    private static boolean validInteger(String value) {
        try { return value != null && Integer.parseInt(value) >= 0; }
        catch (NumberFormatException e) { return false; }
    }
    private static final class SensorClassifier implements RecordClassifier {
        public RecordType classify(ParsedLine l) { return l.has("sensor") && validDouble(l.get("reading")) ? RecordType.SENSOR : null; }
    }
    private static final class DeviceClassifier implements RecordClassifier {
        public RecordType classify(ParsedLine l) {
            return l.has("device") && l.has("severity") &&
                    List.of("CRITICAL", "ERROR", "WARN", "INFO").contains(l.get("severity")) ? RecordType.DEVICE : null;
        }
    }
    private static final class AccessClassifier implements RecordClassifier {
        public RecordType classify(ParsedLine l) {
            return l.has("user") && l.has("door") && l.has("duration_ms") &&
                    List.of("GRANTED", "DENIED").contains(l.get("result")) && validInteger(l.get("duration_ms"))
                    ? RecordType.ACCESS : null;
        }
    }
}
