package poussecafe.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import poussecafe.apm.ApmTransaction;
import poussecafe.apm.ApmTransactionLabels;
import poussecafe.apm.ApmTransactionResults;
import poussecafe.apm.ApplicationPerformanceMonitoring;
import poussecafe.environment.MessageConsumptionReport;
import poussecafe.environment.MessageListener;
import poussecafe.environment.MessageListenerGroupConsumptionState;
import poussecafe.runtime.FailFastException;
import poussecafe.runtime.MessageConsumptionHandler;
import poussecafe.runtime.OriginalAndMarshaledMessage;

public class MessageListenerGroup {

    public static class Builder {

        private MessageListenerGroup group = new MessageListenerGroup();

        public Builder consumptionId(String consumptionId) {
            group.consumptionId = consumptionId;
            return this;
        }

        public Builder message(OriginalAndMarshaledMessage message) {
            group.message = message;
            return this;
        }

        public Builder listeners(List<MessageListener> listeners) {
            group.listeners = listeners;
            return this;
        }

        public Builder messageConsumptionHandler(MessageConsumptionHandler messageConsumptionHandler) {
            group.messageConsumptionHandler = messageConsumptionHandler;
            return this;
        }

        public Builder applicationPerformanceMonitoring(ApplicationPerformanceMonitoring applicationPerformanceMonitoring) {
            group.applicationPerformanceMonitoring = applicationPerformanceMonitoring;
            return this;
        }

        public Builder failFast(boolean failFast) {
            group.failFast = failFast;
            return this;
        }

        @SuppressWarnings("rawtypes")
        public Builder aggregateRootClass(Optional<Class> aggregateRootClass) {
            group.aggregateRootClass = aggregateRootClass;
            return this;
        }

        public MessageListenerGroup build() {
            Objects.requireNonNull(group.consumptionId);
            Objects.requireNonNull(group.message);
            Objects.requireNonNull(group.listeners);
            Objects.requireNonNull(group.messageConsumptionHandler);
            Objects.requireNonNull(group.applicationPerformanceMonitoring);
            Objects.requireNonNull(group.aggregateRootClass);
            group.listeners.sort(null);
            return group;
        }
    }

    private String consumptionId;

    private OriginalAndMarshaledMessage message;

    private List<MessageListener> listeners;

    public List<MessageConsumptionReport> consumeMessageOrRetry(MessageListenerGroupConsumptionState consumptionState) {
        List<MessageConsumptionReport> reports = new ArrayList<>();
        for(MessageListener listener : listeners) {
            if(consumptionState.mustRun(listener)) {
                MessageListenerExecutor executor = new MessageListenerExecutor.Builder()
                        .consumptionId(consumptionId)
                        .listener(listener)
                        .messageConsumptionHandler(messageConsumptionHandler)
                        .consumptionState(consumptionState)
                        .failFast(failFast)
                        .build();
                reports.add(executeInApmTransaction(executor));
            }
        }
        return reports;
    }

    private MessageConsumptionHandler messageConsumptionHandler;

    private boolean failFast;

    private MessageConsumptionReport executeInApmTransaction(MessageListenerExecutor executor) {
        String transactionName = executor.listener().shortId();
        ApmTransaction apmTransaction = applicationPerformanceMonitoring.startTransaction(transactionName);
        try {
            executor.executeListener();
            configureApmTransaction(executor, apmTransaction);
            return executor.messageConsumptionReport();
        } catch (FailFastException e) {
            apmTransaction.setResult(ApmTransactionResults.FAILURE);
            apmTransaction.captureException(e);
            throw e;
        } catch (Exception e) {
            apmTransaction.setResult(ApmTransactionResults.FAILURE);
            apmTransaction.captureException(e);
            return new MessageConsumptionReport.Builder()
                    .failure(e)
                    .build();
        } finally {
            apmTransaction.end();
        }
    }

    private void configureApmTransaction(MessageListenerExecutor executor, ApmTransaction apmTransaction) {
        MessageConsumptionReport report = executor.messageConsumptionReport();
        if(report.isSuccess()) {
            apmTransaction.setResult(ApmTransactionResults.SUCCESS);
        } else if(report.mustRetry()) {
            apmTransaction.setResult(ApmTransactionResults.SKIP);
        } else if(report.isSkipped()) {
            apmTransaction.setResult(ApmTransactionResults.SKIP);
            if(!report.failures().isEmpty()) {
                apmTransaction.addLabel(ApmTransactionLabels.SKIP_REASON, report.failures().get(0).getClass().getSimpleName());
            }
        } else if(report.isFailed()) {
            apmTransaction.setResult(ApmTransactionResults.FAILURE);
            apmTransaction.captureException(report.failures().get(0));
        } else {
            throw new IllegalArgumentException("Unsupported consumption report");
        }
    }

    private ApplicationPerformanceMonitoring applicationPerformanceMonitoring;

    @SuppressWarnings("rawtypes")
    public Optional<Class> aggregateRootClass() {
        return aggregateRootClass;
    }

    @SuppressWarnings("rawtypes")
    private Optional<Class> aggregateRootClass = Optional.empty();
}