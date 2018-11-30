package poussecafe.sample;

import poussecafe.context.BoundedContextConfigurer;

public class SampleMetaAppBoundedContextDefinition {

    private SampleMetaAppBoundedContextDefinition() {

    }

    public static BoundedContextConfigurer configure() {
        return new BoundedContextConfigurer.Builder()
                .packagePrefix("poussecafe.sample")
                .build();
    }
}