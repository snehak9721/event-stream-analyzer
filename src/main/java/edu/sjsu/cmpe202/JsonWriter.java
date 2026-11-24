package edu.sjsu.cmpe202;

import java.util.*;

/** Small dependency-free JSON serializer for maps, lists, strings, numbers, and booleans. */
public final class JsonWriter {
    private JsonWriter() {}
    public static String pretty(Object value) { StringBuilder b = new StringBuilder(); write(value, b, 0); return b.append('\n').toString(); }
    private static void write(Object v, StringBuilder b, int level) {
        if (v instanceof Map<?,?> m) { if (m.isEmpty()) { b.append("{}"); return; } b.append("{\n"); int i=0; for (var e:m.entrySet()) { indent(b,level+1); write(e.getKey().toString(),b,level+1); b.append(": "); write(e.getValue(),b,level+1); if (++i<m.size()) b.append(','); b.append('\n'); } indent(b,level); b.append('}'); }
        else if (v instanceof List<?> l) { if (l.isEmpty()) { b.append("[]"); return; } b.append("[\n"); for(int i=0;i<l.size();i++){ indent(b,level+1); write(l.get(i),b,level+1); if(i+1<l.size()) b.append(','); b.append('\n'); } indent(b,level); b.append(']'); }
        else if (v instanceof String s) b.append('"').append(escape(s)).append('"'); else b.append(v);
    }
    private static void indent(StringBuilder b,int n){ b.append("  ".repeat(n)); }
    private static String escape(String s){ return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t"); }
}
