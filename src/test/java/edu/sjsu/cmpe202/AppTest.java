package edu.sjsu.cmpe202;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @TempDir Path tmp;

    private static final String SAMPLE = String.join("\n",
            "ts=2024-05-10T08:00:00Z sensor=temperature_celsius device=thermostat-01 reading=21.5",
            "ts=2024-05-10T08:00:05Z severity=INFO device=thermostat-01 msg=\"Heartbeat OK\"",
            "ts=2024-05-10T08:00:10Z user=user-42 door=door-A result=DENIED duration_ms=30",
            "ts=2024-05-10T08:00:15Z sensor=humidity_percent device=hygro-02 reading=45",
            "ts=2024-05-10T08:00:20Z severity=ERROR device=door-lock-07 msg=\"Latch jammed\" code=E41",
            "ts=2024-05-10T08:00:25Z user=user-42 door=door-A result=DENIED duration_ms=28",
            "ts=2024-05-10T08:00:30Z sensor=temperature_celsius device=thermostat-02 reading=25.0",
            "ts=2024-05-10T08:00:35Z severity=WARN device=thermostat-01 msg=\"Temp drifting\"",
            "ts=2024-05-10T08:00:40Z user=user-17 door=door-B result=GRANTED duration_ms=12",
            "ts=2024-05-10T08:00:45Z user=user-42 door=door-A result=DENIED duration_ms=35",
            "ts=2024-05-10T08:00:50Z ###CORRUPTED LINE### !!!",
            "ts=2024-05-10T08:00:55Z sensor=temperature_celsius device=thermostat-01 reading=abc",
            "ts=2024-05-10T08:01:00Z severity=CRITICAL device=door-lock-07 msg=\"Tamper detected\"",
            "ts=2024-05-10T08:01:05Z user=user-08 door=door-A result=GRANTED duration_ms=18") + "\n";

    @Test void writesAllThreeFilesForSampleInput() throws Exception {
        Path input = tmp.resolve("events.txt");
        Files.writeString(input, SAMPLE);
        Path out = tmp.resolve("out");
        App.analyze(input, out);
        assertTrue(Files.readString(out.resolve("sensors.json")).contains("\"std_dev\": 1.8"));
        assertTrue(Files.readString(out.resolve("devices.json")).contains("\"most_frequent_severity\": \"WARN\""));
        assertTrue(Files.readString(out.resolve("access.json")).contains("\"50_percentile\": 28"));
    }

    @Test void emptyInputWritesEmptyObjectsInAllFiles() throws Exception {
        Path input = tmp.resolve("empty.txt");
        Files.writeString(input, "###CORRUPTED###\n");
        Path out = tmp.resolve("out");
        App.analyze(input, out);
        assertEquals("{}\n", Files.readString(out.resolve("sensors.json")));
        assertEquals("{}\n", Files.readString(out.resolve("devices.json")));
        assertEquals("{}\n", Files.readString(out.resolve("access.json")));
    }

    @Test void missingInputFileIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> App.analyze(tmp.resolve("missing.txt"), tmp));
    }

    @Test void parsesFileAndOutdirArguments() {
        var args = App.Arguments.parse(new String[] {"--file", "events.txt", "--outdir", "results"});
        assertEquals(Path.of("events.txt"), args.file());
        assertEquals(Path.of("results"), args.outDir());
    }

    @Test void outdirDefaultsToCurrentDirectory() {
        assertEquals(Path.of("."), App.Arguments.parse(new String[] {"--file", "events.txt"}).outDir());
    }

    @Test void missingFileArgumentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> App.Arguments.parse(new String[] {"--outdir", "x"}));
    }
}
