package poussecafe.domain;

import java.util.Objects;
import poussecafe.attribute.entity.EntityAttribute;
import poussecafe.environment.EntityFactory;
import poussecafe.environment.MessageFactory;
import poussecafe.environment.NewEntityInstanceSpecification;
import poussecafe.storage.MessageCollection;
import poussecafe.storage.Storage;

public abstract class Entity<I, D extends EntityAttributes<I>> {

    @SuppressWarnings("unchecked")
    public void attributes(Object data) {
        Objects.requireNonNull(data);
        this.attributes = (D) data;
    }

    private D attributes;

    public D attributes() {
        return attributes;
    }

    public void entityFactory(EntityFactory entityFactory) {
        Objects.requireNonNull(entityFactory);
        this.entityFactory = entityFactory;
    }

    private EntityFactory entityFactory;

    protected EntityFactory entityFactory() {
        return entityFactory;
    }

    public void messageFactory(MessageFactory messageFactory) {
        Objects.requireNonNull(messageFactory);
        this.messageFactory = messageFactory;
    }

    private MessageFactory messageFactory;

    protected MessageFactory messageFactory() {
        return messageFactory;
    }

    @Deprecated
    protected void dontPersist(boolean value) {
        dontPersist = value;
    }

    @Deprecated
    private boolean dontPersist;

    @Deprecated
    public boolean dontPersist() {
        return dontPersist;
    }

    public MessageCollection messageCollection() {
        return messageCollection;
    }

    private MessageCollection messageCollection;

    public void messageCollection(MessageCollection messageCollection) {
        this.messageCollection = messageCollection;
    }

    public Storage storage() {
        return storage;
    }

    private Storage storage;

    public void storage(Storage storage) {
        this.storage = storage;
    }

    public void emitDomainEvent(DomainEvent event) {
        messageCollection().addMessage(event);
    }

    public <E extends DomainEvent> E newDomainEvent(Class<E> eventClass) {
        return messageFactory.newMessage(eventClass);
    }

    public void context(Object context) {
        this.context = context;
    }

    private Object context;

    @SuppressWarnings("unchecked")
    public <C> C context() {
        Objects.requireNonNull(context);
        return (C) context;
    }

    public <E extends Entity<?, ?>> E newEntity(Class<E> entityClass) {
        E entity = entityFactory.newEntity(new NewEntityInstanceSpecification.Builder<E>()
                .entityClass(entityClass)
                .instantiateData(true)
                .build());
        entity.storage(storage);
        entity.messageCollection(messageCollection);
        return entity;
    }

    public <K, E extends Entity<K, ?>> Setter<K, E> setNew(EntityAttribute<E> attribute) {
        return new Setter<>(attribute);
    }

    public class Setter<K, E extends Entity<K, ?>> {

        public Setter(EntityAttribute<E> attribute) {
            this.attribute = attribute;
        }

        private EntityAttribute<E> attribute;

        public E withKey(K key) {
            E newEntity = attribute.newInContextOf(Entity.this);
            newEntity.attributes().identifier().value(key);
            attribute.inContextOf(Entity.this).value(newEntity);
            return newEntity;
        }
    }
}
