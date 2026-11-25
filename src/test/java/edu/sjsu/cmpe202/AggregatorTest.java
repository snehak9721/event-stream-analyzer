package edu.sjsu.cmpe202;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AggregatorTest {
    private ParsedLine p(String s) {
        return new KeyValueParser().parse(s);
    }

    @Test
    void classifiesSensor() {
        assertEquals(RecordType.SENSOR, ClassifierChainFactory.createDefault().classify(p("ts=t sensor=s reading=2")));
    }

    @Test
    void rejectsBadSensor() {
        assertNull(ClassifierChainFactory.createDefault().classify(p("ts=t sensor=s reading=x")));
    }

    @Test
    void classifiesDevice() {
        assertEquals(RecordType.DEVICE,
                ClassifierChainFactory.createDefault().classify(p("ts=t severity=ERROR device=d")));
    }

    @Test
    void rejectsBadSeverity() {
        assertNull(ClassifierChainFactory.createDefault().classify(p("ts=t severity=BAD device=d")));
    }

    @Test
    void classifiesAccess() {
        assertEquals(RecordType.ACCESS,
                ClassifierChainFactory.createDefault().classify(p("ts=t user=u door=d result=DENIED duration_ms=10")));
    }

    @Test
    void rejectsBadDuration() {
        assertNull(
                ClassifierChainFactory.createDefault().classify(p("ts=t user=u door=d result=DENIED duration_ms=no")));
    }

    @Test
    void sensorAggregationUsesPopulationStdDev() {
        var a = new SensorAggregator();
        a.accept(p("ts=t sensor=s reading=1"));
        a.accept(p("ts=t sensor=s reading=3"));
        var s = (java.util.Map<?, ?>) a.result().get("s");
        assertEquals(2, s.get("count"));
        assertEquals(1L, s.get("minimum"));
        assertEquals(2L, s.get("median"));
        assertEquals(1L, s.get("std_dev"));
    }

    @Test
    void sensorMedianEvenIsAverage() {
        var a = new SensorAggregator();
        a.accept(p("ts=t sensor=s reading=1"));
        a.accept(p("ts=t sensor=s reading=4"));
        var s = (java.util.Map<?, ?>) a.result().get("s");
        assertEquals(2.5, s.get("median"));
    }

    @Test
    void deviceTieUsesHigherSeverity() {
        var a = new DeviceAggregator();
        a.accept(p("ts=t severity=INFO device=d"));
        a.accept(p("ts=t severity=ERROR device=d"));
        var d = (java.util.Map<?, ?>) ((java.util.Map<?, ?>) a.result().get("by_device")).get("d");
        assertEquals("ERROR", d.get("most_frequent_severity"));
    }

    @Test
    void deviceCountsAllSeverityLevels() {
        var a = new DeviceAggregator();
        a.accept(p("ts=t severity=CRITICAL device=d"));
        var out = (java.util.Map<?, ?>) a.result();
        assertEquals(1, ((java.util.Map<?, ?>) out.get("by_severity")).get("CRITICAL"));
    }

    @Test
    void accessUsesNearestRankPercentile() {
        var a = new AccessAggregator();
        for (int i = 1; i <= 4; i++)
            a.accept(p("ts=t user=u door=d result=GRANTED duration_ms=" + i));
        var d = (java.util.Map<?, ?>) ((java.util.Map<?, ?>) a.result().get("d"));
        var times = (java.util.Map<?, ?>) d.get("decision_times");
        assertEquals(2, times.get("50_percentile"));
        assertEquals(4, times.get("99_percentile"));
    }

    @Test
    void accessFlagsThreeDeniedAttempts() {
        var a = new AccessAggregator();
        for (int i = 1; i <= 3; i++)
            a.accept(p("ts=t user=u door=d result=DENIED duration_ms=" + i));
        var d = (java.util.Map<?, ?>) ((java.util.Map<?, ?>) a.result().get("d"));
        assertEquals(java.util.List.of("u"), d.get("flagged_users"));
    }

    @Test
    void accessSeparatesResults() {
        var a = new AccessAggregator();
        a.accept(p("ts=t user=u door=d result=GRANTED duration_ms=1"));
        a.accept(p("ts=t user=u door=d result=DENIED duration_ms=2"));
        var d = (java.util.Map<?, ?>) ((java.util.Map<?, ?>) a.result().get("d"));
        assertEquals(1, ((java.util.Map<?, ?>) d.get("results")).get("GRANTED"));
    }

    @Test
    void emptyAggregatorsReturnEmptyObjects() {
        assertTrue(new SensorAggregator().result().isEmpty());
        assertTrue(new DeviceAggregator().result().isEmpty());
        assertTrue(new AccessAggregator().result().isEmpty());
    }

    @Test
    void jsonWriterEscapesQuotes() {
        assertTrue(JsonWriter.pretty(java.util.Map.of("x", "a\"b")).contains("a\\\"b"));
    }

    @Test
    void negativeSensorValuesUseSymmetricRounding() {
        var aggregator = new SensorAggregator();

        aggregator.accept(p("ts=t sensor=s reading=-2.25"));

        var stats = (java.util.Map<?, ?>) aggregator.result().get("s");
        assertEquals(-2.3,
                ((Number) stats.get("minimum")).doubleValue(), 0.0001);
    }

    @Test
    void jsonWriterEscapesControlCharacters() {
        String json = JsonWriter.pretty(
                java.util.Map.of("message", "hello\u0001world"));

        assertTrue(json.contains("\\u0001"));
    }

}
