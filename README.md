# Event Stream Analyzer

Java 17 command-line application for classifying IoT and smart-building event records and writing aggregate results to `sensors.json`, `devices.json`, and `access.json`.

## Build and run

```bash
mvn clean test
mvn package
java -jar target/event-stream-analyzer-1.0.0.jar --file events.txt --outdir results/
```

The application creates all three output files. Malformed records are skipped silently. Missing `--file` and missing input files produce an error and a non-zero exit status.

## Input rules

Each valid line begins with a `ts` field and contains whitespace-separated `key=value` pairs. Quoted values may contain spaces. Blank lines, missing `=`, empty keys or values, unclosed quotes, duplicate keys, and records with invalid classification fields are skipped.

## Aggregation rules

- Sensor standard deviation is population standard deviation: variance is divided by `N`.
- Sensor median averages the two middle values for an even-sized set.
- Access percentiles use nearest rank: sorted index `ceil(p / 100 * N) - 1`.
- Access 50th percentile intentionally uses nearest rank and can differ from sensor median.
- Decimal sensor results are rounded to one decimal place; whole-number results are written without a decimal point.

## Design

The project separates parsing, classification, aggregation, and output. `ClassifierChain` applies a first-match Chain of Responsibility over the classifiers created by `ClassifierChainFactory`. `JsonOutputWriter` implements the `OutputWriter` Strategy interface. `ClassifierChainFactory` and `AggregatorFactory` are Simple Factories that centralize object creation. See [`docs/design.md`](docs/design.md), [`docs/class-diagram.puml`](docs/class-diagram.puml), and the high-resolution [`docs/UMLDiagram.png`](docs/UMLDiagram.png).

## Testing

The JUnit 5 suite covers parsing, classification, aggregation, JSON escaping, empty input, CLI argument validation, and end-to-end output generation.
