package edu.sjsu.cmpe202;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KeyValueParserTest {
    private final KeyValueParser parser = new KeyValueParser();
    @Test void parsesSimplePairs() { var p=parser.parse("ts=t sensor=temp reading=21.5"); assertEquals("temp",p.get("sensor")); }
    @Test void parsesQuotedSpaces() { var p=parser.parse("ts=t severity=INFO device=d msg=\"Heartbeat OK\""); assertEquals("Heartbeat OK",p.get("msg")); }
    @Test void rejectsMissingEquals() { assertTrue(parser.parse("ts=t broken").fields().isEmpty()); }
    @Test void rejectsUnclosedQuote() { assertTrue(parser.parse("ts=t msg=\"bad").fields().isEmpty()); }
    @Test void rejectsMissingTimestamp() { assertTrue(parser.parse("sensor=temp reading=1").fields().isEmpty()); }
}
