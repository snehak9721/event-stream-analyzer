package edu.sjsu.cmpe202;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassifierTest {
    private final RecordClassifier chain = ClassifierChainFactory.createDefault();
    private ParsedLine p(String s) { return new KeyValueParser().parse(s); }

    @Test void classifiesSensor() { assertEquals(RecordType.SENSOR, chain.classify(p("ts=t sensor=s reading=2"))); }
    @Test void rejectsSensorWithNonNumericReading() { assertNull(chain.classify(p("ts=t sensor=s reading=x"))); }
    @Test void rejectsSensorWithNonFiniteReading() { assertNull(chain.classify(p("ts=t sensor=s reading=NaN"))); }
    @Test void rejectsSensorWithoutReadingKey() { assertNull(chain.classify(p("ts=t sensor=s"))); }
    @Test void classifiesDevice() { assertEquals(RecordType.DEVICE, chain.classify(p("ts=t severity=ERROR device=d"))); }
    @Test void rejectsBadSeverity() { assertNull(chain.classify(p("ts=t severity=BAD device=d"))); }
    @Test void rejectsDeviceWithoutDeviceKey() { assertNull(chain.classify(p("ts=t severity=WARN"))); }
    @Test void classifiesAccess() { assertEquals(RecordType.ACCESS, chain.classify(p("ts=t user=u door=d result=DENIED duration_ms=10"))); }
    @Test void rejectsBadDuration() { assertNull(chain.classify(p("ts=t user=u door=d result=DENIED duration_ms=no"))); }
    @Test void rejectsNegativeDuration() { assertNull(chain.classify(p("ts=t user=u door=d result=GRANTED duration_ms=-1"))); }
    @Test void rejectsUnknownResult() { assertNull(chain.classify(p("ts=t user=u door=d result=MAYBE duration_ms=5"))); }
    @Test void rejectsCorruptedLine() { assertNull(chain.classify(p("###CORRUPTED LINE### !!!"))); }
}
