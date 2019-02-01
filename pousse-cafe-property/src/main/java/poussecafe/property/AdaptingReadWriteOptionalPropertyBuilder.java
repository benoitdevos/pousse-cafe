package poussecafe.property;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <U> Stored type
 * @param <T> Property type
 */
public class AdaptingReadWriteOptionalPropertyBuilder<U, T> {

    AdaptingReadWriteOptionalPropertyBuilder(Supplier<T> getter, Function<T, U> adapter) {
        this.getter = getter;
        this.adapter = adapter;
    }

    private Supplier<T> getter;

    private Function<T, U> adapter;

    public ReadWriteOptionalPropertyBuilder<T> set(Consumer<U> setter) {
        Objects.requireNonNull(setter);

        CompositeProperty<T, T> compositeProperty = new CompositeProperty<>();
        compositeProperty.getter = getter;
        compositeProperty.setter = value -> {
            if(value != null) {
                setter.accept(adapter.apply(value));
            } else {
                setter.accept(null);
            }
        };
        return new ReadWriteOptionalPropertyBuilder<>(compositeProperty);
    }
}