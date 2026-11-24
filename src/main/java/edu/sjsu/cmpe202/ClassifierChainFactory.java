package edu.sjsu.cmpe202;

import java.util.List;

/** Simple Factory for the ordered Chain of Responsibility. */
public final class ClassifierChainFactory {
    private ClassifierChainFactory() {}

    public static RecordClassifier createDefault() {
        return new ClassifierChain(List.of(
                new SensorClassifier(), new DeviceClassifier(), new AccessClassifier()));
    }
}
