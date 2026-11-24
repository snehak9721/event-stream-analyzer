# Event Stream Analyzer Design

## Problem statement

The application reads one event record per line from a plain-text IoT or smart-building log. It parses key/value fields, ignores malformed records, classifies valid records as sensor readings, device events, or access records, and writes the required aggregates as three readable JSON files. The design keeps each responsibility separate so new record types and formats can be added without rewriting existing logic.

## Pattern selection

### Chain of Responsibility

`ClassifierChain` holds one classifier per record type (`SensorClassifier`, `DeviceClassifier`, `AccessClassifier`), each behind the `RecordClassifier` interface. The chain tries them in registration order and returns the first matching `RecordType`; a line that matches none returns `null` and is skipped. This fits because classification is an ordered set of independent validation rules, and each rule can be tested on its own. A single large conditional would be shorter at first but harder to test and extend as record types grow.

This is a first-match chain rather than a linked-handler chain: the classifiers do not hold successor references. That simpler form is sufficient because each classifier only recognizes its own record type.

### Strategy

The output boundary is `OutputWriter`, with `JsonOutputWriter` as the current implementation. A future `CsvOutputWriter` can implement the same interface and be selected in `App`. This keeps format-specific code out of the aggregators and parser. The trade-off is a small abstraction and one more class per format.

### Factory

`ClassifierChainFactory` builds the ordered classifier chain, and `AggregatorFactory` creates the aggregator for each `RecordType`. Both are Simple Factories. Factories keep construction decisions out of the processing loop, so `App` does not construct concrete classifiers or aggregators directly. The trade-off is a small amount of indirection.

## Consequences

The patterns create more small classes than a procedural implementation, but each class has one reason to change. This improves unit testing, keeps malformed-input behavior local, and supports extension. The main costs are:

- **Indirection:** a contributor follows `App` to the chain and factories before seeing the complete flow.
- **Class count:** there is one classifier and aggregator per record type, plus factories and an output implementation.
- **Strategy overhead:** an output interface for one current implementation adds a small abstraction.

The implementation uses a small dependency-free JSON serializer. This reduces setup and honors the core-logic constraint, but a future complex JSON requirement should be handled by replacing that serializer with a well-tested JSON library.

## Extensibility

- **Network Logs:** add `NETWORK` to `RecordType`, add and register `NetworkClassifier`, add `NetworkAggregator` and its factory case, and extend the output mapping. Existing parser and aggregators remain unchanged.
- **JSON Lines input:** add a parser interface, implement `JsonLineParser`, and select the parser at the composition boundary. Classification and aggregation remain unchanged because both parsers return `ParsedLine`.
- **CSV output:** add `CsvOutputWriter` implementing `OutputWriter` and select it in `App`. JSON serialization, classification, and aggregation remain unchanged.

## Input and output rules implemented

- Every valid input line begins with a `ts` field. The timestamp is ignored during aggregation.
- Blank lines, missing `=`, empty keys or values such as `sensor=`, unclosed quotes, and duplicate keys are skipped.
- Lines with missing or invalid classification fields are skipped silently.
- Decimal results are rounded to one decimal place. Whole-number results are written without a decimal point.
- Percentiles use the nearest-rank method; the sensor median averages the two middle values for an even count.
