package dev.kensa.render.diagram;

import dev.kensa.render.diagram.svg.GroupHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class GroupedMessageCollector implements Collector<String, List<String>, List<String>> {

    public static Collector<String, List<String>, List<String>> toGroupedMessages() {
        return new GroupedMessageCollector();
    }

    private final GroupHelper groupHelper = new GroupHelper();

    @Override
    public Supplier<List<String>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<String>, String> accumulator() {
        return (list, nextLine) -> list.addAll(groupHelper.markupGroup(nextLine));
    }

    @Override
    public BinaryOperator<List<String>> combiner() {
        return (a, b) -> {
            a.addAll(b);
            return a;
        };
    }

    @Override
    public Function<List<String>, List<String>> finisher() {
        return list -> {
            list.addAll(groupHelper.cleanUpOpenGroups());
            return list;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return new HashSet<>();
    }
}