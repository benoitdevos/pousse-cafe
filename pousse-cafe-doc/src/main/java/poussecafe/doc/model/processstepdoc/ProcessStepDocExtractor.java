package poussecafe.doc.model.processstepdoc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import jdk.javadoc.doclet.DocletEnvironment;
import poussecafe.doc.ClassDocPredicates;
import poussecafe.doc.Logger;
import poussecafe.doc.model.AnnotationsResolver;
import poussecafe.doc.model.ComponentDoc;
import poussecafe.doc.model.ComponentDocFactory;
import poussecafe.doc.model.DocletAccess;
import poussecafe.doc.model.ModuleComponentDoc;
import poussecafe.doc.model.aggregatedoc.AggregateDocFactory;
import poussecafe.doc.model.aggregatedoc.AggregateDocId;
import poussecafe.doc.model.domainprocessdoc.ComponentMethodName;
import poussecafe.doc.model.domainprocessdoc.DomainProcessDocFactory;
import poussecafe.doc.model.moduledoc.ModuleDocId;
import poussecafe.domain.DomainEvent;
import poussecafe.domain.Service;
import poussecafe.exception.PousseCafeException;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class ProcessStepDocExtractor implements Service {

    public List<ProcessStepDoc> extractProcessStepDocs(ModuleDocId moduleDocId, TypeElement classDoc) {
        List<ProcessStepDoc> stepDocs = new ArrayList<>();
        for(ExecutableElement methodDoc : docletAccess.methods(classDoc)) {
            if(isProcessStep(methodDoc)) {
                List<String> customStepSignatures = annotationsResolver.step(methodDoc);
                if(!customStepSignatures.isEmpty()) {
                    stepDocs.addAll(extractCustomSteps(moduleDocId, methodDoc));
                } else {
                    stepDocs.add(extractDeclaredStep(moduleDocId, methodDoc));
                }
            }
        }
        return stepDocs;
    }

    private DocletAccess docletAccess;

    private boolean isProcessStep(ExecutableElement methodDoc) {
        if(domainProcessDocFactory.isDomainProcessDoc((TypeElement) methodDoc.getEnclosingElement())) {
            return annotationsResolver.isStep(methodDoc);
        } else {
            Optional<String> consumedMessage = consumedMessageExtractor.consumedMessage(methodDoc);
            List<String> producedEvents = extractProducedEvents(methodDoc);
            return annotationsResolver.isStep(methodDoc) ||
                    (docletAccess.isPublic(methodDoc) && (consumedMessage.isPresent() || !producedEvents.isEmpty()));
        }
    }

    private DomainProcessDocFactory domainProcessDocFactory;

    private AnnotationsResolver annotationsResolver;

    private ConsumedMessageExtractor consumedMessageExtractor;

    private List<String> extractProducedEvents(ExecutableElement methodDoc) {
        return annotationsResolver.event(methodDoc);
    }

    private List<ProcessStepDoc> extractCustomSteps(ModuleDocId moduleDocId,
            ExecutableElement methodDoc) {
        List<ProcessStepDoc> stepDocs = new ArrayList<>();
        List<StepMethodSignature> methodSignatures = customStepsSignatures(methodDoc);
        for(StepMethodSignature signature : methodSignatures) {
            Logger.info("Extracting custom step " + signature);
            ProcessStepDocId messageListenerDocId = new ProcessStepDocId(signature.toString());
            ModuleComponentDoc moduleComponentDoc = new ModuleComponentDoc.Builder()
                    .moduleDocId(moduleDocId)
                    .componentDoc(new ComponentDoc.Builder()
                            .name(signature.toString())
                            .description(annotationsResolver.renderCommentBody(methodDoc))
                            .build())
                    .build();
            ProcessStepDoc processStepDoc = messageListenerDocFactory.createMessageListenerDoc(messageListenerDocId,
                    moduleComponentDoc);
            processStepDoc.attributes().processName().value(processName(methodDoc));
            processStepDoc.attributes().stepMethodSignature().nonOptionalValue(signature);
            processStepDoc.attributes().producedEvents().value(new HashSet<>(annotationsResolver.event(methodDoc)));
            processStepDoc.attributes().fromExternals().value(new HashSet<>(annotationsResolver.fromExternal(methodDoc)));
            processStepDoc.attributes().toExternals().value(new HashSet<>(annotationsResolver.toExternal(methodDoc)));
            stepDocs.add(processStepDoc);
        }
        return stepDocs;
    }

    private List<StepMethodSignature> customStepsSignatures(ExecutableElement methodDoc) {
        List<String> customStepSignatures = annotationsResolver.step(methodDoc);
        if(domainProcessDocFactory.isDomainProcessDoc((TypeElement) methodDoc.getEnclosingElement())) {
            if(customStepSignatures.size() != 1) {
                throw new PousseCafeException("Domain processes listeners must be tagged with a single step");
            }
            Optional<String> consumedEvent = consumedEvent(methodDoc);
            ComponentMethodName componentMethodName = ComponentMethodName.parse(customStepSignatures.get(0));
            return asList(new StepMethodSignature.Builder()
                    .componentMethodName(componentMethodName)
                    .consumedMessageName(consumedEvent)
                    .build());
        } else {
            return customStepSignatures.stream().map(StepMethodSignature::parse).collect(toList());
        }
    }

    private Optional<String> consumedEvent(ExecutableElement methodDoc) {
        List<? extends VariableElement> parameters = methodDoc.getParameters();
        if(parameters.isEmpty()) {
            return Optional.empty();
        }

        TypeMirror firstParameterType = parameters.get(0).asType();
        Element firstParameterElement = docletEnvironment.getTypeUtils().asElement(firstParameterType);
        if(firstParameterElement instanceof TypeElement) {
            TypeElement firstParameterTypeElement = (TypeElement) firstParameterElement;
            if(classDocPredicates.documentsWithSuperinterface(firstParameterTypeElement, DomainEvent.class)) {
                return Optional.of(firstParameterTypeElement.getQualifiedName().toString());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private DocletEnvironment docletEnvironment;

    private ClassDocPredicates classDocPredicates;

    private ProcessStepDocFactory messageListenerDocFactory;

    private Optional<String> processName(ExecutableElement methodDoc) {
        TypeElement containingClass = (TypeElement) methodDoc.getEnclosingElement();
        List<String> processNames = annotationsResolver.process(methodDoc);
        if(domainProcessDocFactory.isDomainProcessDoc(containingClass)) {
            return Optional.of(domainProcessDocFactory.name(containingClass));
        } else if(!processNames.isEmpty()) {
            return Optional.of(processNames.get(0));
        } else {
            return Optional.empty();
        }
    }

    private ProcessStepDoc extractDeclaredStep(ModuleDocId moduleDocId,
            ExecutableElement methodDoc) {
        Logger.info("Extracting declared step from method " + methodDoc.getSimpleName().toString());

        Optional<String> consumedMessage = consumedMessageExtractor.consumedMessage(methodDoc);
        TypeElement enclosingType = (TypeElement) methodDoc.getEnclosingElement();
        StepMethodSignature stepMethodSignature = new StepMethodSignature.Builder()
                .componentMethodName(new ComponentMethodName.Builder()
                        .componentName(enclosingType.getSimpleName().toString())
                        .methodName(methodDoc.getSimpleName().toString())
                        .build())
                .consumedMessageName(consumedMessage)
                .build();
        ProcessStepDocId id = new ProcessStepDocId(stepMethodSignature);
        ModuleComponentDoc moduleComponentDoc = new ModuleComponentDoc.Builder()
                .moduleDocId(moduleDocId)
                .componentDoc(componentDocFactory.buildDoc(id.stringValue(), methodDoc))
                .build();

        AggregateDocId aggregate = declaringAggregate(methodDoc);
        ProcessStepDoc processStepDoc = messageListenerDocFactory.createMessageListenerDoc(id,
                moduleComponentDoc);
        processStepDoc.attributes().processName().value(processName(methodDoc));
        processStepDoc.attributes().stepMethodSignature().nonOptionalValue(stepMethodSignature);
        processStepDoc.attributes().producedEvents().value(new HashSet<>(annotationsResolver.event(methodDoc)));
        processStepDoc.attributes().fromExternals().value(new HashSet<>(annotationsResolver.fromExternal(methodDoc)));
        processStepDoc.attributes().toExternals().value(new HashSet<>(annotationsResolver.toExternal(methodDoc)));
        processStepDoc.attributes().aggregate().value(Optional.of(aggregate));

        if(aggregateDocFactory.isFactoryDoc(enclosingType)) {
            TypeElement aggregateTypeElement = aggregateDocFactory.aggregateTypeElementOfFactory(enclosingType);
            List<ExecutableElement> methods = ElementFilter.methodsIn(aggregateTypeElement.getEnclosedElements());
            Optional<ExecutableElement> onAddMethod = methods.stream()
                    .filter(element -> element.getSimpleName().toString().equals("onAdd"))
                    .filter(element -> element.getParameters().isEmpty())
                    .findFirst();
            if(onAddMethod.isPresent()) {
                ExecutableElement presentOnAddMethod = onAddMethod.get();
                processStepDoc.attributes().producedEvents().addAll(annotationsResolver.event(presentOnAddMethod));
                processStepDoc.attributes().toExternals().addAll(annotationsResolver.toExternal(presentOnAddMethod));
            }
        }

        return processStepDoc;
    }

    private AggregateDocId declaringAggregate(ExecutableElement methodDoc) {
        TypeElement enclosingType = (TypeElement) methodDoc.getEnclosingElement();
        TypeElement aggregateType;
        if(aggregateDocFactory.isFactoryDoc(enclosingType)) {
            aggregateType = aggregateDocFactory.aggregateTypeElementOfFactory(enclosingType);
        } else if(aggregateDocFactory.isRepositoryDoc(enclosingType)) {
            aggregateType = aggregateDocFactory.aggregateTypeElementOfRepository(enclosingType);
        } else {
            aggregateType = enclosingType;
        }
        return AggregateDocId.ofClassName(aggregateType.getQualifiedName().toString());
    }

    private ComponentDocFactory componentDocFactory;

    private AggregateDocFactory aggregateDocFactory;
}
