package dev.kensa.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class WrappingCollector<T> implements Collector<T, List<T>, List<T>> {

    public static <T> WrappingCollector<T> wrapping(T start, T finish) {
        return new WrappingCollector<>(start, finish);
    }

    private final T start;
    private final T finish;

    private WrappingCollector(T start, T finish) {
        this.start = start;
        this.finish = finish;
    }

    @Override
    public Supplier<List<T>> supplier() {
        return () -> {
            List<T> ts = new ArrayList<>();
            ts.add(start);
            return ts;
        };
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (a, b) -> {
            a.addAll(b);
            return a;
        };
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        return list -> {
            list.add(finish);
            return list;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return new HashSet<>();
    }
}