package com.hms.model;

/**
 * Emergency Severity Index (ESI) triage levels.
 * Lower {@code code} means higher clinical priority (Level 1 = most critical).
 */
public enum TriageLevel {
    CRITICAL(1, "Critical"),
    EMERGENT(2, "Emergent"),
    URGENT(3, "Urgent"),
    LESS_URGENT(4, "Less Urgent"),
    ROUTINE(5, "Routine");

    private final int code;
    private final String label;

    TriageLevel(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** @return numeric ESI code (1 = highest priority). */
    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /** Resolve a {@code TriageLevel} from its numeric ESI code. */
    public static TriageLevel fromCode(int code) {
        for (TriageLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid triage code: " + code);
    }
}
