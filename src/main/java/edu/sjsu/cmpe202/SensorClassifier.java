package edu.sjsu.cmpe202;

/** Recognizes sensor readings with a finite numeric reading. */
final class SensorClassifier implements RecordClassifier {
    @Override
    public RecordType classify(ParsedLine line) {
        return line.has("sensor") && Validation.isFiniteNumber(line.get("reading"))
                ? RecordType.SENSOR : null;
    }
}
