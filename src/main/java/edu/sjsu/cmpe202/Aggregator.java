package edu.sjsu.cmpe202;

import java.util.Map;

public interface Aggregator {
    void accept(ParsedLine line);
    Map<String, Object> result();
}
