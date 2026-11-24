package edu.sjsu.cmpe202;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class App {
    public static void main(String[] args) {
        try { Arguments options = Arguments.parse(args); analyze(options.file(), options.outDir()); }
        catch (IllegalArgumentException | IOException e) { System.err.println("Error: " + e.getMessage()); System.exit(1); }
    }
    static void analyze(Path file, Path outDir) throws IOException {
        if (!Files.isRegularFile(file)) throw new IllegalArgumentException("input file does not exist: " + file);
        KeyValueParser parser = new KeyValueParser(); RecordClassifier classifier = ClassifierChainFactory.createDefault();
        Map<RecordType, Aggregator> aggregators = new EnumMap<>(RecordType.class); for (RecordType type : RecordType.values()) aggregators.put(type, AggregatorFactory.create(type));
        try (var lines = Files.lines(file)) { lines.map(parser::parse).forEach(line -> { RecordType type = classifier.classify(line); if (type != null) aggregators.get(type).accept(line); }); }
        OutputWriter writer = new JsonOutputWriter();
        writer.write(outDir, aggregators.get(RecordType.SENSOR).result(), aggregators.get(RecordType.DEVICE).result(), aggregators.get(RecordType.ACCESS).result());
    }
    record Arguments(Path file, Path outDir) {
        static Arguments parse(String[] args) { Path file=null, out=Path.of("."); for(int i=0;i<args.length;i++){ if(args[i].equals("--file")&&i+1<args.length) file=Path.of(args[++i]); else if(args[i].equals("--outdir")&&i+1<args.length) out=Path.of(args[++i]); else throw new IllegalArgumentException("usage: java -jar analyzer.jar --file <filename> [--outdir <directory>]"); } if(file==null) throw new IllegalArgumentException("missing required --file argument"); return new Arguments(file,out); }
    }
}
