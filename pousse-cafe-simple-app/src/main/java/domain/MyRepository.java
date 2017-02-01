package domain;

import poussecafe.domain.Repository;

/*
 * The Repository is responsible for providing a shallow (i.e. with no data nor key defined) instance of Aggregate.
 * It also interacts with configured data access which hides the used storage technology.
 */
public class MyRepository extends Repository<MyAggregate, MyAggregateKey, MyAggregate.Data> {

    @Override
    protected MyAggregate newAggregate() {
        return new MyAggregate();
    }

}
