package poussecafe.environment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import poussecafe.messaging.Message;

import static java.util.Collections.emptySet;

public class MessageListenersPool {

    public void registerListenerForMessageClass(MessageListener listener, Class<? extends Message> messageClass) {
        Set<MessageListener> messageClassListeners = listenersByMessageClass.computeIfAbsent(messageClass, key -> new HashSet<>());
        if(!messageClassListeners.add(listener)) {
            throw new IllegalArgumentException("Listener could not be registered for message " + messageClass.getName() + ", would hide another one: " + listener);
        }

        Set<Class<? extends Message>> messageClasses = messageClassesByListener.computeIfAbsent(listener, key -> new HashSet<>());
        if(!messageClasses.add(messageClass)) {
            throw new IllegalArgumentException("Listener " + listener + " already linked to message " + messageClass);
        }
    }

    private Map<Class<? extends Message>, Set<MessageListener>> listenersByMessageClass = new HashMap<>();

    public Set<MessageListener> getListeners(Class<? extends Message> messageClass) {
        return getListenersForMessageClass(messageClass);
    }

    private Set<MessageListener> getListenersForMessageClass(Class<? extends Message> key) {
        return Optional.ofNullable(listenersByMessageClass.get(key)).map(Collections::unmodifiableSet).orElse(emptySet());
    }

    public Collection<MessageListener> allListeners() {
        return Collections.unmodifiableSet(messageClassesByListener.keySet());
    }

    public MessageListenersPool[] split(MessageListenersPoolSplitStrategy strategy) {
        return strategy.split(this);
    }

    private Map<MessageListener, Set<Class<? extends Message>>> messageClassesByListener = new HashMap<>();

    public boolean contains(MessageListener listener) {
        return messageClassesByListener.keySet().contains(listener);
    }

    public int countListeners() {
        return messageClassesByListener.size();
    }

    public Map<MessageListener, Set<Class<? extends Message>>> messageClassesByListener() {
        return Collections.unmodifiableMap(messageClassesByListener);
    }
}
