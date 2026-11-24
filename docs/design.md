# Event Stream Analyzer Design

## Problem statement

The application reads one event record per line from a plain-text IoT or smart-building log. It parses key/value fields, ignores malformed records, classifies valid records as sensor readings, device events, or access records, and writes the required aggregates as three readable JSON files. The design keeps each responsibility separate so new record types and formats can be added without rewriting existing logic.

## Pattern selection

### Chain of Responsibility

`ClassifierChain` holds independent classifiers for sensors, devices, and access records. Each classifier either recognizes the line or passes it on. This fits because classification is an ordered set of independent validation rules. A single large conditional in the application would be shorter initially, but would become difficult to test and extend as record types grow.

### Strategy

The output boundary is isolated in `JsonOutputWriter`. A future `CsvOutputWriter` can implement the same output contract and be selected by the command-line composition code without changing parsers or aggregators. This is preferable to putting JSON and CSV branches into every aggregator, though it adds a small abstraction and one more class per format.

### Factory

`ClassifierChainFactory` and `AggregatorFactory` create the configured classifier chain and the appropriate aggregator for each `RecordType`. Factories keep object-construction decisions out of the processing loop. The trade-off is extra indirection, but tests and future configuration changes do not need to know the concrete class graph.

## Consequences

The patterns create more small classes than a procedural implementation, but each class has one reason to change. This improves unit testing, makes malformed-input behavior local, and supports extension. The main cost is indirection: a new contributor must follow the chain and factories before seeing the complete flow. The implementation also deliberately uses a small dependency-free JSON serializer, which reduces setup but means a future complex JSON requirement should be handled by replacing that serializer with a well-tested library.

## Extensibility

- **Network Logs:** add a `NetworkClassifier`, `NetworkAggregator`, and `Network` record type; register the classifier in `ClassifierChainFactory`, register the aggregator in `AggregatorFactory`, and add one output file mapping. Existing parser and aggregators remain unchanged.
- **JSON Lines input:** add a `RecordParser` interface, move the current logic into `KeyValueParser`, and add `JsonLineParser`. Select the parser at the composition boundary based on an input-format option. Classification and aggregation remain unchanged because both parsers return `ParsedLine`.
- **CSV output:** add a `CsvOutputWriter` implementing the output-writer contract and choose it at the output boundary. Existing JSON serialization and aggregation logic remain unchanged.
