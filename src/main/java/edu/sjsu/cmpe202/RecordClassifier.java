package edu.sjsu.cmpe202;

public interface RecordClassifier {
    RecordType classify(ParsedLine line);
}
