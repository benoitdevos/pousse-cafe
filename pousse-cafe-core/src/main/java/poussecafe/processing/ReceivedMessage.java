package poussecafe.processing;

import java.util.Objects;
import poussecafe.runtime.OriginalAndMarshaledMessage;

public class ReceivedMessage {

    public static class Builder {

        private ReceivedMessage receivedMessage = new ReceivedMessage();

        public Builder payload(OriginalAndMarshaledMessage payload) {
            receivedMessage.payload = payload;
            return this;
        }

        public Builder acker(Runnable acker) {
            receivedMessage.acker = acker;
            return this;
        }

        public ReceivedMessage build() {
            Objects.requireNonNull(receivedMessage.payload);
            Objects.requireNonNull(receivedMessage.acker);
            return receivedMessage;
        }
    }

    private ReceivedMessage() {

    }

    private OriginalAndMarshaledMessage payload;

    public OriginalAndMarshaledMessage message() {
        return payload;
    }

    private Runnable acker;

    public void ack() {
        acker.run();
    }
}
