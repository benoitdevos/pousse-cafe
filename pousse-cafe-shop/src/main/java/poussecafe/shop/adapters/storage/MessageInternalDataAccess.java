package poussecafe.shop.adapters.storage;

import java.util.List;
import poussecafe.discovery.DataAccessImplementation;
import poussecafe.shop.domain.CustomerKey;
import poussecafe.shop.domain.Message;
import poussecafe.shop.domain.MessageDataAccess;
import poussecafe.shop.domain.MessageKey;
import poussecafe.storage.internal.InternalDataAccess;
import poussecafe.storage.internal.InternalStorage;

import static java.util.Arrays.asList;

@DataAccessImplementation(
    aggregateRoot = Message.class,
    dataImplementation = MessageData.class,
    storageName = InternalStorage.NAME
)
public class MessageInternalDataAccess extends InternalDataAccess<MessageKey, MessageData> implements MessageDataAccess<MessageData> {

    @Override
    protected List<Object> extractIndexedData(MessageData data) {
        return asList(data.customerKey().value());
    }

    @Override
    public List<MessageData> findByCustomer(CustomerKey customerKey) {
        return findBy(customerKey);
    }

}