package edu.sjsu.cmpe202;

import java.util.List;

/** Recognizes device events with an allowed severity. */
final class DeviceClassifier implements RecordClassifier {
    private static final List<String> SEVERITIES = List.of("CRITICAL", "ERROR", "WARN", "INFO");

    @Override
    public RecordType classify(ParsedLine line) {
        return line.has("device") && line.has("severity")
                && SEVERITIES.contains(line.get("severity"))
                ? RecordType.DEVICE : null;
    }
}
