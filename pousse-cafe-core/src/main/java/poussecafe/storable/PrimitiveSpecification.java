package poussecafe.storable;

import poussecafe.util.AbstractBuilder;

import static poussecafe.check.AssertionSpecification.value;
import static poussecafe.check.Checks.checkThat;

public class PrimitiveSpecification<T> {

    public static <T> PrimitiveSpecification<T> ofClass(Class<T> primitiveClass) {
        return new PrimitiveSpecification.Builder<T>().withPrimitiveClass(primitiveClass).build();
    }

    private Class<T> primitiveClass;

    private boolean withData;

    public Class<T> getPrimitiveClass() {
        return primitiveClass;
    }

    public boolean isWithData() {
        return withData;
    }

    public Object getExistingData() {
        return existingData;
    }

    private Object existingData;

    public static class Builder<T> extends AbstractBuilder<PrimitiveSpecification<T>> {
        protected Builder() {
            super(new PrimitiveSpecification<>());
        }

        public Builder<T> withPrimitiveClass(Class<T> primitiveClass) {
            product().primitiveClass = primitiveClass;
            return this;
        }

        public Builder<T> withData(boolean withData) {
            product().withData = withData;
            return this;
        }

        public Builder<T> withExistingData(Object existingData) {
            product().existingData = existingData;
            return this;
        }

        @Override
        protected void checkProduct(PrimitiveSpecification<T> product) {
            checkThat(value(product.primitiveClass).notNull());
            if(product.withData) {
                checkThat(value(product.existingData == null).isTrue());
            }
        }
    }

    private PrimitiveSpecification() {

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((primitiveClass == null) ? 0 : primitiveClass.hashCode());
        result = prime * result + (withData ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        PrimitiveSpecification other = (PrimitiveSpecification) obj;
        if (primitiveClass == null) {
            if (other.primitiveClass != null) {
                return false;
            }
        } else if (!primitiveClass.equals(other.primitiveClass)) {
            return false;
        }
        if (withData != other.withData) {
            return false;
        }
        return true;
    }
}