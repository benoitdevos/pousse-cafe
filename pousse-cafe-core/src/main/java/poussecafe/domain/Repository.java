package poussecafe.domain;

import java.util.List;
import java.util.Objects;
import poussecafe.exception.NotFoundException;
import poussecafe.property.MessageCollection;

import static java.util.stream.Collectors.toList;
import static poussecafe.check.AssertionSpecification.value;
import static poussecafe.check.Checks.checkThat;

public abstract class Repository<A extends AggregateRoot<K, D>, K, D extends EntityData<K>> extends Component {

    @SuppressWarnings("unchecked")
    void setEntityClass(Class<?> entityClass) {
        checkThat(value(entityClass).notNull().because("Entity class cannot be null"));
        this.entityClass = (Class<A>) entityClass;
    }

    private Class<A> entityClass;

    public A find(K key) {
        checkKey(key);
        D data = dataAccess.findData(key);
        return newEntityWithData(data);
    }

    protected EntityDataAccess<K, D> dataAccess;

    private void checkKey(K key) {
        checkThat(value(key).notNull().because("Key cannot be null"));
    }

    protected A newEntityWithData(D data) {
        if(data == null) {
            return null;
        } else {
            return newComponent(new ComponentSpecification.Builder<A>()
                    .withComponentClass(entityClass)
                    .withExistingData(data)
                    .build());
        }
    }

    public A get(K key) {
        A entity = find(key);
        if (entity == null) {
            throw new NotFoundException("Entity with key " + key + " not found");
        } else {
            return entity;
        }
    }

    public void add(A entity) {
        checkEntity(entity);
        addData(entity);
    }

    protected void addData(A entity) {
        MessageCollection messageCollection = entity.messageCollection();
        if(!entity.dontPersist()) {
            D data = entity.getData();
            dataAccess.addData(data);
        }
        considerMessageSendingAfterAdd(entity, messageCollection);
    }

    protected void considerMessageSendingAfterAdd(A entity,
            MessageCollection messageCollection) {
        entity.storage().getMessageSendingPolicy().considerSending(messageCollection);
    }

    private void checkEntity(A entity) {
        checkThat(value(entity).notNull().because("Entity cannot be null"));
        checkThat(value(entity.getData()).notNull().because("Entity data cannot be null"));
    }

    public void update(A entity) {
        checkEntity(entity);
        updateData(entity);
    }

    protected void updateData(A entity) {
        MessageCollection messageCollection = entity.messageCollection();
        if(!entity.dontPersist()) {
            D data = entity.getData();
            dataAccess.updateData(data);
        }
        considerMessageSendingAfterUpdate(entity, messageCollection);
    }

    protected void considerMessageSendingAfterUpdate(A entity,
            MessageCollection messageCollection) {
        entity.storage().getMessageSendingPolicy().considerSending(messageCollection);
    }

    public void delete(K key) {
        checkKey(key);
        A entity = find(key);
        if (entity != null) {
            deleteData(entity);
        }
    }

    protected void deleteData(A entity) {
        MessageCollection messageCollection = entity.messageCollection();
        dataAccess.deleteData(entity.getKey());
        considerMessageSendingAfterDelete(entity, messageCollection);
    }

    protected void considerMessageSendingAfterDelete(A entity,
            MessageCollection messageCollection) {
        entity.storage().getMessageSendingPolicy().considerSending(messageCollection);
    }

    protected List<A> newEntitiesWithData(List<D> data) {
        return data.stream().map(this::newEntityWithData).filter(Objects::nonNull).collect(toList());
    }

    @SuppressWarnings("unchecked")
    void setDataAccess(Object dataAccess) {
        checkThat(value(dataAccess).notNull().because("Data access cannot be null"));
        this.dataAccess = (EntityDataAccess<K, D>) dataAccess;
    }

    public EntityDataAccess<K, D> getDataAccess() {
        return dataAccess;
    }

}
