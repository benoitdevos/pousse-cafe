package poussecafe.domain;

import java.util.UUID;

public class SimpleAggregateKey {

    private String id;

    public SimpleAggregateKey() {
        id = UUID.randomUUID().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        SimpleAggregateKey other = (SimpleAggregateKey) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SimpleAggregateKey [id=" + id + "]";
    }
}
