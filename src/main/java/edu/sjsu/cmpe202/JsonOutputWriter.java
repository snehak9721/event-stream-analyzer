package edu.sjsu.cmpe202;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

public final class JsonOutputWriter implements OutputWriter {
    @Override
    public void write(Path outDir, Map<String,Object> sensors, Map<String,Object> devices, Map<String,Object> access) throws IOException {
        Files.createDirectories(outDir); Files.writeString(outDir.resolve("sensors.json"), JsonWriter.pretty(sensors));
        Files.writeString(outDir.resolve("devices.json"), JsonWriter.pretty(devices)); Files.writeString(outDir.resolve("access.json"), JsonWriter.pretty(access));
    }
}
