package poussecafe.sample.domain;

import poussecafe.domain.AggregateRoot;
import poussecafe.storable.ActiveStorableData;

public class Customer extends AggregateRoot<CustomerKey, Customer.Data> {

    public static interface Data extends ActiveStorableData<CustomerKey> {

    }

}
