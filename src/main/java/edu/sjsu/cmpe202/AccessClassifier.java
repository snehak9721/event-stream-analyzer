package edu.sjsu.cmpe202;

import java.util.List;

/** Recognizes access events with a valid result and non-negative duration. */
final class AccessClassifier implements RecordClassifier {
    private static final List<String> RESULTS = List.of("GRANTED", "DENIED");

    @Override
    public RecordType classify(ParsedLine line) {
        return line.has("user") && line.has("door") && line.has("duration_ms")
                && RESULTS.contains(line.get("result"))
                && Validation.isNonNegativeInteger(line.get("duration_ms"))
                ? RecordType.ACCESS : null;
    }
}
