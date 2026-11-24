package edu.sjsu.cmpe202;

import java.util.List;

/** Chain of Responsibility: each classifier owns one record type. */
public final class ClassifierChain implements RecordClassifier {
    private final List<RecordClassifier> classifiers;
    public ClassifierChain(List<RecordClassifier> classifiers) { this.classifiers = List.copyOf(classifiers); }
    @Override public RecordType classify(ParsedLine line) {
        for (RecordClassifier classifier : classifiers) {
            RecordType result = classifier.classify(line);
            if (result != null) return result;
        }
        return null;
    }
}
