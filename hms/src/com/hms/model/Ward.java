package com.hms.model;

/**
 * Hospital wards and the inclusive bed-number range each one owns.
 * Used by the bed-allocation system (Level 4) and ward statistics.
 */
public enum Ward {
    ICU(1, 10),
    GENERAL(11, 50),
    EMERGENCY(51, 60),
    PEDIATRIC(61, 80),
    MATERNITY(81, 100);

    private final int firstBed;
    private final int lastBed;

    Ward(int firstBed, int lastBed) {
        this.firstBed = firstBed;
        this.lastBed = lastBed;
    }

    public int getFirstBed() {
        return firstBed;
    }

    public int getLastBed() {
        return lastBed;
    }

    /** @return total number of beds in this ward. */
    public int capacity() {
        return lastBed - firstBed + 1;
    }

    /** @return true if {@code bedNumber} belongs to this ward. */
    public boolean contains(int bedNumber) {
        return bedNumber >= firstBed && bedNumber <= lastBed;
    }

    /** Resolve the ward that owns a given bed number. */
    public static Ward forBed(int bedNumber) {
        for (Ward ward : values()) {
            if (ward.contains(bedNumber)) {
                return ward;
            }
        }
        throw new IllegalArgumentException("No ward owns bed #" + bedNumber);
    }
}
