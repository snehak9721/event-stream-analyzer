# Event Stream Analyzer

Java 17+ command-line application for classifying IoT and smart-building event records and writing aggregate results to `sensors.json`, `devices.json`, and `access.json`.

## Build and run

```bash
mvn test
mvn package
java -jar target/event-stream-analyzer-1.0.0.jar --file events.txt --outdir results/
```

The default output directory is the current directory. Malformed records are skipped silently. Missing `--file` and missing input files produce an error and non-zero exit status.

## Reproducible aggregation rules

- Sensor standard deviation is population standard deviation: variance is divided by `N`.
- Sensor median averages the two middle values for an even-sized set.
- Access percentiles use nearest rank: sorted index `ceil(p / 100 * N) - 1`.
- Access 50th percentile intentionally uses nearest rank and can differ from sensor median.
- Sensor decimal results are rounded to one decimal place; whole-number results are serialized as integers.

## Design

Parsing, classification, aggregation, and output are separate responsibilities. `ClassifierChain` applies the Chain of Responsibility pattern. `JsonOutputWriter` is a Strategy-style output component that can be replaced by a CSV writer later. `ClassifierChainFactory` and `AggregatorFactory` centralize object creation. See `docs/design.md` and `docs/class-diagram.puml`.
