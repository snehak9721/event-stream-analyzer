package edu.sjsu.cmpe202;

/** Shared value validation used by record classifiers. */
final class Validation {
    private Validation() {}

    static boolean isFiniteNumber(String value) {
        if (value == null) return false;
        try {
            return Double.isFinite(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static boolean isNonNegativeInteger(String value) {
        if (value == null) return false;
        try {
            return Integer.parseInt(value) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
