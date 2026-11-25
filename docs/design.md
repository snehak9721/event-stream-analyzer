# Event Stream Analyzer Design

## Problem statement

The application reads one event record per line from a plain-text IoT or smart-building log. It parses key/value fields, ignores malformed records, classifies valid records as sensor readings, device events, or access records, and writes the required aggregates as three readable JSON files. The design keeps each responsibility separate so new record types and formats can be added without rewriting existing logic.

## Pattern selection

### Chain of Responsibility - Ordered First-Match Chain

`ClassifierChain` holds one classifier per record type (`SensorClassifier`, `DeviceClassifier`, `AccessClassifier`), each behind the `RecordClassifier` interface. The chain tries them in registration order and returns the first matching `RecordType`; a line that matches none returns `null` and is skipped. This fits because classification is an ordered set of independent validation rules, and each rule can be tested on its own. A single large conditional would be shorter at first but harder to test and extend as record types grow.

This is a first-match chain rather than a linked-handler chain: the classifiers do not hold successor references. That simpler form is sufficient because each classifier only recognizes its own record type.

### Strategy

The output boundary is `OutputWriter`, with `JsonOutputWriter` as the current implementation. A future `CsvOutputWriter` can implement the same interface and be selected in `App`. This keeps format-specific code out of the aggregators and parser. The trade-off is a small abstraction and one more class per format.

### Factory

`ClassifierChainFactory` builds the ordered classifier chain, and `AggregatorFactory` creates the aggregator for each `RecordType`. Both are Simple Factories. `App` uses these factories instead of constructing concrete classifiers or aggregators directly. `App` remains the composition root and directly creates the parser and JSON output writer. This keeps construction decisions centralized while preserving a simple application flow. The trade-off is a small amount of indirection.

## Consequences

The patterns create more small classes than a procedural implementation, but each class has one reason to change. This improves unit testing, keeps malformed-input behavior local, and supports extension. The main costs are:

- **Indirection:** a contributor follows `App` to the chain and factories before seeing the complete flow.
- **Class count:** there is one classifier and aggregator per record type, plus factories and an output implementation.
- **Strategy overhead:** an output interface for one current implementation adds a small abstraction.

The implementation uses a small dependency-free JSON serializer. This reduces setup and honors the core-logic constraint, but a future complex JSON requirement should be handled by replacing that serializer with a well-tested JSON library.

## Extensibility

The following changes require updates only at registration or composition points. Existing parser, classifier, aggregator, and serializer logic remains unchanged unless explicitly listed.

| Hypothetical change | New classes | Existing classes or files to modify |
|---|---|---|
| Network Logs | `NetworkClassifier`, `NetworkAggregator` | Add `NETWORK` to `RecordType`; register the classifier in `ClassifierChainFactory`; add a case in `AggregatorFactory`; extend the output mapping in `App`, `OutputWriter`, and `JsonOutputWriter` |
| JSON Lines input | `LineParser`, `JsonLineParser` | Make `KeyValueParser` implement `LineParser`; select the parser in `App` |
| CSV output | `CsvOutputWriter` | Select the writer in `App`; `CsvOutputWriter` implements the existing `OutputWriter` interface |

Both key/value and JSON Lines parsers return `ParsedLine`, so classification and aggregation do not need to change. Both JSON and CSV writers receive completed aggregation results, so the aggregators do not need to change when a new output format is added.

## Input and output rules implemented

- Every valid input line begins with a `ts` field. The timestamp is ignored during aggregation.
- Blank lines, missing `=`, empty keys or values such as `sensor=`, unclosed quotes, duplicate keys, and non-whitespace characters immediately following a closing quote are skipped.
- Lines with missing or invalid classification fields are skipped silently.
- Decimal results are rounded to one decimal place. Whole-number results are written without a decimal point.
- Percentiles use the nearest-rank method; the sensor median averages the two middle values for an even count.
