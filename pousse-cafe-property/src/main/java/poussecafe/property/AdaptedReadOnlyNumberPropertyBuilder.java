package poussecafe.property;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <U> Stored type
 * @param <T> Property type
 */
public class AdaptedReadOnlyNumberPropertyBuilder<U, T> {

    AdaptedReadOnlyNumberPropertyBuilder(Supplier<T> getter) {
        this.getter = getter;
    }

    private Supplier<T> getter;

    public AdaptingReadWriteOptionalPropertyBuilder<U, T> adapt(Function<T, U> adapter) {
        Objects.requireNonNull(adapter);
        return new AdaptingReadWriteOptionalPropertyBuilder<>(getter, adapter);
    }
}