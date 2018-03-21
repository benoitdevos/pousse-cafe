package poussecafe.messaging.internal;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import poussecafe.messaging.Message;
import poussecafe.messaging.MessageReceiver;
import poussecafe.messaging.MessageSender;

public class InternalMessageQueue extends MessageReceiver implements MessageSender {

    private Queue<Message> queue;

    private Semaphore available = new Semaphore(0);

    private Semaphore mutex = new Semaphore(1);

    public InternalMessageQueue() {
        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void sendMessage(Message message) {
        logger.info("Sending message {}", message);
        queue.add(message);
        available.release();
    }

    @Override
    public void actuallyStartReceiving() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    available.acquire();
                    mutex.acquire();
                    onMessage(queue.poll());
                    mutex.release();
                }
            } catch (InterruptedException e) {
                return;
            }
        });
        t.setDaemon(true);
        t.setName("in-memory message queue");
        t.start();
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
