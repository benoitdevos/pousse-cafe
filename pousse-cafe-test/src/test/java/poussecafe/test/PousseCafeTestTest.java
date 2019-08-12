package poussecafe.test;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import poussecafe.discovery.BoundedContextConfigurer;
import poussecafe.discovery.MessageListener;
import poussecafe.environment.BoundedContext;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PousseCafeTestTest extends PousseCafeTest {

    @Override
    protected List<BoundedContext> boundedContexts() {
        return asList(new BoundedContextConfigurer.Builder()
                .packagePrefix("poussecafe.test")
                .build()
                .defineAndImplementDefault()
                .build());
    }

    @Test
    public void waitUntilEmptyOrInterruptedReturnsTrueWhenAllMessagesIssued() {
        givenMessages();
        whenEmitted();
        thenAllConsumedAfterWait();
    }

    private void givenMessages() {
        for(int i = 0; i < 1000; ++i) {
            messages.add(new SampleMessage());
        }
    }

    private List<SampleMessage> messages = new ArrayList<>();

    private void whenEmitted() {
        for(SampleMessage message : messages) {
            submitCommand(message);
        }
    }

    @MessageListener
    public void recordMessage(SampleMessage message) {
        recordedMessages.add(message);
    }

    private List<SampleMessage> recordedMessages = new ArrayList<>();

    private void thenAllConsumedAfterWait() {
        waitUntilAllMessageQueuesEmpty();
        assertThat(recordedMessages.size(), is(messages.size()));
    }
}