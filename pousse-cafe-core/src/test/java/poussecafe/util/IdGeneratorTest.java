package poussecafe.util;

import org.junit.Test;
import poussecafe.util.IdGenerator;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;

public class IdGeneratorTest {

    private IdGenerator idGenerator;

    private String result;

    @Test
    public void generatorReturnsNonEmptyString() {
        givenIdGenerator();
        whenGeneratingId();
        thenResultIsNonEmpty();
    }

    private void givenIdGenerator() {
        idGenerator = new IdGenerator();
    }

    private void whenGeneratingId() {
        result = idGenerator.generateId();
    }

    private void thenResultIsNonEmpty() {
        assertThat(result, not(isEmptyString()));
    }
}
