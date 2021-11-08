package crfa.app.utils;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class MoreOptionals {

    public static <L, R> void ifPresent(Optional<L> left, Optional<R> right, BiConsumer<L, R> consumer) {

        left.ifPresent(l -> right.ifPresent(r -> consumer.accept(l, r)));
    }

    public static <T> Optional<T> allOf(Optional<T> o1, Optional<T> o2,
                                       BiFunction<T, T, T> fun) {
        return o1.flatMap(t -> o2.map(u -> fun.apply(t, u)));
    }

}
