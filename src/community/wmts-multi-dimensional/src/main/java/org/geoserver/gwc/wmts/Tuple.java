package org.geoserver.gwc.wmts;

import java.util.Objects;

public final class Tuple<T, U> {

    public final T first;
    public final U second;

    private Tuple(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public static <R, S> Tuple<R, S> tuple(R first, S second) {
        return new Tuple<>(first, second);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) object;
        return Objects.equals(first, tuple.first) &&
                Objects.equals(second, tuple.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
