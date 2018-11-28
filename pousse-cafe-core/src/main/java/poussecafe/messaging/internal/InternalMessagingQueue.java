package poussecafe.messaging.internal;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import poussecafe.context.MessageConsumer;
import poussecafe.messaging.MessageAdapter;
import poussecafe.messaging.MessageReceiver;
import poussecafe.messaging.MessageSender;

public class InternalMessagingQueue {

    InternalMessagingQueue(MessageConsumer messageConsumer) {
        messageReceiver = new InternalMessageReceiver(messageAdapter, messageConsumer);
        messageSender = new InternalMessageSender(messageAdapter);
    }

    private MessageAdapter messageAdapter = new SerializingMessageAdapter();

    public class InternalMessageReceiver extends MessageReceiver {

        private InternalMessageReceiver(MessageAdapter messageAdapter, MessageConsumer messageConsumer) {
            super(messageAdapter, messageConsumer);
        }

        @Override
        protected void actuallyStartReceiving() {
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        available.acquire();
                        mutex.acquire();
                        Object polledObject = queue.poll();
                        if(STOP.equals(polledObject)) {
                            mutex.release();
                            break;
                        } else {
                            onMessage(polledObject);
                            mutex.release();
                        }
                    }
                } catch (InterruptedException e) {
                    return;
                }
            });
            t.setDaemon(true);
            t.setName("internal message queue");
            t.start();
        }

        @Override
        protected void actuallyStopReceiving() {
            queue.add(STOP);
            available.release();
        }

        private static final String STOP = "stop";

        public InternalMessagingQueue queue() {
            return InternalMessagingQueue.this;
        }
    }

    private Semaphore available = new Semaphore(0);

    private Semaphore mutex = new Semaphore(1);

    private Queue<Object> queue = new LinkedBlockingQueue<>();

    private InternalMessageReceiver messageReceiver;

    public MessageReceiver messageReceiver() {
        return messageReceiver;
    }

    public class InternalMessageSender extends MessageSender {

        private InternalMessageSender(MessageAdapter messageAdapter) {
            super(messageAdapter);
        }

        @Override
        protected void sendMarshalledMessage(Object marshalledMessage) {
            queue.add(marshalledMessage);
            available.release();
        }
    }

    private InternalMessageSender messageSender;

    public MessageSender messageSender() {
        return messageSender;
    }

    public void waitUntilEmpty()
            throws InterruptedException {
        boolean messagesQueued;
        do {
            mutex.acquire();
            messagesQueued = !queue.isEmpty();
            mutex.release();
        } while (messagesQueued);
    }
}