package poussecafe.journal;

import java.util.Set;
import poussecafe.context.BoundedContext;
import poussecafe.domain.EntityDefinition;
import poussecafe.domain.EntityImplementation;
import poussecafe.domain.Service;
import poussecafe.journal.data.JournalEntryData;
import poussecafe.journal.data.memory.InternalJournalEntryDataAccess;
import poussecafe.journal.domain.ConsumptionFailureRepository;
import poussecafe.journal.domain.JournalEntry;
import poussecafe.journal.domain.JournalEntryFactory;
import poussecafe.journal.domain.JournalEntryRepository;
import poussecafe.journal.domain.MessageReplayer;
import poussecafe.messaging.Message;
import poussecafe.messaging.MessageImplementationConfiguration;
import poussecafe.process.DomainProcess;
import poussecafe.storage.internal.InternalStorage;
import poussecafe.util.IdGenerator;

public class JournalBoundedContext extends BoundedContext {

    @Override
    protected void loadDefinitions(Set<EntityDefinition> definitions) {
        definitions.add(new EntityDefinition.Builder()
                .withEntityClass(JournalEntry.class)
                .withFactoryClass(JournalEntryFactory.class)
                .withRepositoryClass(JournalEntryRepository.class)
                .build());
    }

    @Override
    protected void loadProcesses(Set<Class<? extends DomainProcess>> processes) {
        // None
    }

    @Override
    protected void loadServices(Set<Class<? extends Service>> services) {
        services.add(IdGenerator.class);
        services.add(MessageReplayer.class);
        services.add(ConsumptionFailureRepository.class);
    }

    @Override
    protected void loadEntityImplementations(Set<EntityImplementation> implementations) {
        implementations.add(new EntityImplementation.Builder()
                .withEntityClass(JournalEntry.class)
                .withDataFactory(JournalEntryData::new)
                .withDataAccessFactory(InternalJournalEntryDataAccess::new)
                .withStorage(InternalStorage.instance())
                .build());
    }

    @Override
    protected void loadMessageImplementations(Set<MessageImplementationConfiguration> implementations) {
        // None
    }

    @Override
    protected void loadMessages(Set<Class<? extends Message>> messages) {
        // None
    }
}