package poussecafe.processing;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.Test;
import poussecafe.environment.MessageListener;
import poussecafe.messaging.Message;
import poussecafe.runtime.DefaultConsumptionHandler;
import poussecafe.runtime.FailFastException;
import poussecafe.runtime.OriginalAndMarshaledMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageProcessorTest {

    @Test
    public void messageProcessorForwardsMessageToListenersConsumers() {
        givenListeners();
        givenMessageProcessor();
        whenProcessingMessage();
        thenMessageForwardedToListenersConsumers();
    }

    private void givenListeners() {
        listeners.add(buildListener("id1"));
        listeners.add(buildListener("id2"));
        listeners.add(buildListener("id3"));
        listeners.add(buildListener("id4"));
    }

    private Set<MessageListener> listeners = new HashSet<>();

    private MessageListener buildListener(String id) {
        @SuppressWarnings("unchecked")
        Consumer<Message> consumer = mock(Consumer.class);
        return new MessageListener.Builder()
                .consumer(consumer)
                .id("id")
                .messageClass(Message.class)
                .priority(0)
                .build();
    }

    private void givenMessageProcessor() {
        messageProcessor = messageProcessorBuilder().build();
    }

    @SuppressWarnings("unchecked")
    private MessageProcessor.Builder messageProcessorBuilder() {
        ListenersSetPartition listenersPartition = mock(ListenersSetPartition.class);
        ListenersSet partitionListenersSet = mock(ListenersSet.class);
        when(partitionListenersSet.messageListenersOf(any(Class.class))).thenReturn(listeners);
        when(listenersPartition.partitionListenersSet()).thenReturn(partitionListenersSet);
        return new MessageProcessor.Builder()
                .listenersPartition(listenersPartition)
                .messageConsumptionHandler(new DefaultConsumptionHandler());
    }

    private MessageProcessor messageProcessor;

    private void whenProcessingMessage() {
        message = mock(OriginalAndMarshaledMessage.class);
        Message original = mock(Message.class);
        when(message.original()).thenReturn(original);
        messageProcessor.processMessage(message);
    }

    private OriginalAndMarshaledMessage message;

    private void thenMessageForwardedToListenersConsumers() {
        for(MessageListener listener : listeners) {
            verify(listener.consumer()).accept(message.original());
        }
    }

    @Test(expected = FailFastException.class)
    public void consumerThrowingAndFailfastThrows() {
        givenThrowingListener();
        givenFailfastMessageProcessor();
        whenProcessingMessage();
    }

    private void givenThrowingListener() {
        listeners.add(new MessageListener.Builder()
                .consumer(message -> {throw new RuntimeException();})
                .id("id")
                .messageClass(Message.class)
                .priority(0)
                .build());
    }

    private void givenFailfastMessageProcessor() {
        messageProcessor = messageProcessorBuilder()
                .failFast(true)
                .build();
    }
}