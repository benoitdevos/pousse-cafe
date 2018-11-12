package poussecafe.maven;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import poussecafe.storage.memory.InMemoryStorage;

import static poussecafe.collection.Collections.asSet;

@Mojo(name = "add-aggregate")
public class AddAggregateMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        AddAggregateExecutor executor = new AddAggregateExecutor.Builder()
                .sourceDirectory(sourceDirectory)
                .packageName(aggregatePackage)
                .name(aggregateName)
                .storageAdapters(asSet(storageAdapters))
                .missingAdaptersOnly(missingAdaptersOnly)
                .build();
        executor.execute();
    }

    @Parameter(defaultValue = "${project.build.sourceDirectory}", property = "sourceDirectory", required = true)
    private File sourceDirectory;

    @Parameter(property = "aggregatePackage", required = true)
    private String aggregatePackage;

    @Parameter(property = "aggregateName", required = true)
    private String aggregateName;

    @Parameter(defaultValue = InMemoryStorage.NAME, property = "storageAdapters", required = true)
    private String[] storageAdapters;

    @Parameter(defaultValue = "false", property = "missingAdaptersOnly", required = true)
    private boolean missingAdaptersOnly;
}