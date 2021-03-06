package com.cyanelix.railwatch.domain;

import java.util.Objects;

public final class Journey {
    private final Station from;
    private final Station to;

    private Journey(Station from, Station to) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
    }

    public static Journey of(Station from, Station to) {
        return new Journey(from, to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Journey journey = (Journey) o;
        return Objects.equals(from, journey.from) &&
                Objects.equals(to, journey.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return from.getStationCode() + " -> " + to.getStationCode();
    }

    public Station getFrom() {
        return from;
    }

    public Station getTo() {
        return to;
    }
}
