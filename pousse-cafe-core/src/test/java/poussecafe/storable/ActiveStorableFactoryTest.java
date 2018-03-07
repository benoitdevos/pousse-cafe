package poussecafe.storable;

import org.junit.Test;
import poussecafe.exception.AssertionFailedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ActiveStorableFactoryTest<K, D extends IdentifiedStorableData<K>, A extends ActiveStorable<K, D>, F extends ActiveStorableFactory<K, A, D>> {

    private PrimitiveFactory primitiveFactory = mock(PrimitiveFactory.class);

    private K givenKey;

    private A createdStorable;

    @Test
    public void factoryAtLeastSetsKey() {
        givenKey();
        whenCreatingAggregate();
        thenCreatedAggregateHasKey();
    }

    private void givenKey() {
        givenKey = buildKey();
    }

    protected abstract K buildKey();

    private void whenCreatingAggregate() {
        F factory = factory();
        factory.setPrimitiveFactory(primitiveFactory);
        when(primitiveFactory.newPrimitive(any())).thenReturn(givenKey);
        createdStorable = factory.newStorableWithKey(givenKey);
    }

    protected abstract Class<K> keyClass();

    protected abstract F factory();

    private void thenCreatedAggregateHasKey() {
        assertThat(createdStorable.getKey(), is(givenKey));
    }

    @Test(expected = AssertionFailedException.class)
    public void creationFailsIfNoKey() {
        givenNoKey();
        whenCreatingAggregate();
    }

    private void givenNoKey() {
        givenKey = null;
    }
}
