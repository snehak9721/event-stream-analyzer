package edu.sjsu.cmpe202;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/** Strategy boundary for output formats. */
public interface OutputWriter {
    void write(Path outDir, Map<String,Object> sensors, Map<String,Object> devices, Map<String,Object> access) throws IOException;
}
