package process;

import org.junit.Test;
import poussecafe.mymodule.ACommand;
import poussecafe.mymodule.domain.myaggregate.MyAggregate;
import poussecafe.mymodule.domain.myaggregate.MyAggregateId;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/*
 * Verifies that MyProcess domain process behaves as expected.
 */
public class MyProcessTest extends MyModuleTest {

    /*
     * Test the creation and update of a MyAggregate instance via MyProcess.
     */
    @Test
    public void myCommandUpdatesAggregate() {
        givenAvailableAggregate(); // Let's create an aggregate to execute a command against
        givenCommand(); // Let's create a command with the purpose of modifying the existing aggregate
        whenProcessingCommand(); // Now, let's to the actual execution of the command
        thenAggregateUpdated(); // Finally, let's check that the aggregate was properly updated
    }

    private void givenAvailableAggregate() {
        loadDataFile("/existingMyAggregate.json");
    }

    private void givenCommand() {
        command = newCommand(ACommand.class);
        command.id().value(new MyAggregateId("aggregate-id")); // aggregate-id is the ID in loaded JSON file
        command.x().value(10);
    }

    private ACommand command;

    private void whenProcessingCommand() {
        submitCommand(command);
    }

    private void thenAggregateUpdated() {
        MyAggregate aggregate = getOptional(MyAggregate.class, command.id().value()).orElseThrow();
        assertThat(aggregate.attributes().x().value(), equalTo(command.x().value()));
    }
}
