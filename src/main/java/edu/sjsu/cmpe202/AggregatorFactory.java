package edu.sjsu.cmpe202;

public final class AggregatorFactory {
    private AggregatorFactory() {}
    public static Aggregator create(RecordType type) { return switch (type) { case SENSOR -> new SensorAggregator(); case DEVICE -> new DeviceAggregator(); case ACCESS -> new AccessAggregator(); }; }
}
