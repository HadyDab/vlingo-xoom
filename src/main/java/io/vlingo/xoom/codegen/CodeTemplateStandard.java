package io.vlingo.xoom.codegen;


import io.vlingo.xoom.codegen.storage.ModelClassification;
import io.vlingo.xoom.codegen.storage.StorageType;

import java.util.function.BiFunction;
import java.util.function.Function;

import static io.vlingo.xoom.codegen.CodeGenerationSetup.*;
import static io.vlingo.xoom.codegen.CodeTemplateFile.ANNOTATED_BOOTSTRAP;
import static io.vlingo.xoom.codegen.CodeTemplateFile.DEFAULT_BOOTSTRAP;
import static io.vlingo.xoom.codegen.CodeTemplateParameter.*;
import static io.vlingo.xoom.codegen.storage.StorageType.STATE_STORE;

public enum CodeTemplateStandard {

    AGGREGATE_PROTOCOL(parameters -> CodeTemplateFile.AGGREGATE_PROTOCOL.filename),

    AGGREGATE(parameters -> AGGREGATE_TEMPLATES.get(parameters.find(STORAGE_TYPE)),
            (name, parameters) -> name + "Entity"),

    STATE(parameters -> CodeGenerationSetup.STATE_TEMPLATES.get(parameters.find(STORAGE_TYPE)),
            (name, parameters) -> name + "State"),

    ENTITY_DATA(parameters -> CodeTemplateFile.ENTITY_DATA.filename,
            (name, parameters) -> name + "Data"),

    REST_RESOURCE(parameters -> CodeTemplateFile.REST_RESOURCE.filename,
            (name, parameters) -> name + "Resource"),

    ADAPTER(parameters -> ADAPTER_TEMPLATES.get(parameters.find(STORAGE_TYPE)),
            (name, parameters) -> name + "Adapter"),

    PROJECTION(parameters -> CodeGenerationSetup.PROJECTION_TEMPLATES.get(parameters.find(PROJECTION_TYPE)),
            (name, parameters) -> name + "ProjectionActor"),

    PROJECTION_DISPATCHER_PROVIDER(parameters -> CodeTemplateFile.PROJECTION_DISPATCHER_PROVIDER.filename,
            (name, parameters) -> "ProjectionDispatcherProvider"),

    BOOTSTRAP(parameters -> parameters.find(USE_ANNOTATIONS) ?
            ANNOTATED_BOOTSTRAP.filename : DEFAULT_BOOTSTRAP.filename,
            (name, parameters) -> "Bootstrap"),

    DOMAIN_EVENT(parameters -> {
        if (parameters.find(PLACEHOLDER_EVENT)) {
            return CodeTemplateFile.PLACEHOLDER_DOMAIN_EVENT.filename;
        }
        return CodeTemplateFile.DOMAIN_EVENT.filename;
    }, (name, parameters) -> parameters.find(PLACEHOLDER_EVENT) ? name + "PlaceholderDefined" : name),

    STORE_PROVIDER(parameters -> {
        return storeProviderTemplatesFrom(parameters.find(MODEL_CLASSIFICATION))
                .get(parameters.find(STORAGE_TYPE));
    }, (name, parameters) -> {
        final StorageType storageType = parameters.find(STORAGE_TYPE);
        final ModelClassification modelClassification = parameters.find(MODEL_CLASSIFICATION);
        if(modelClassification.isQueryModel()) {
            return STATE_STORE.resolveProviderNameFrom(modelClassification);
        }
        return storageType.resolveProviderNameFrom(modelClassification);
    });

    private static final String FILE_EXTENSION = ".java";

    private final Function<CodeTemplateParameters, String> templateFileRetriever;
    private final BiFunction<String, CodeTemplateParameters, String> nameResolver;

    CodeTemplateStandard(final Function<CodeTemplateParameters, String> templateFileRetriever) {
        this(templateFileRetriever, (name, parameters) -> name);
    }

    CodeTemplateStandard(final Function<CodeTemplateParameters, String> templateFileRetriever,
                         final BiFunction<String, CodeTemplateParameters, String> nameResolver) {
        this.templateFileRetriever = templateFileRetriever;
        this.nameResolver = nameResolver;
    }

    public String retrieveTemplateFilename(final CodeTemplateParameters parameters) {
        return templateFileRetriever.apply(parameters);
    }

    public String resolveClassname(final String name) {
        return resolveClassname(name, null);
    }

    public String resolveClassname(final CodeTemplateParameters parameters) {
        return resolveClassname(null, parameters);
    }

    public String resolveClassname(final String name, final CodeTemplateParameters parameters) {
        return this.nameResolver.apply(name, parameters);
    }

    public String resolveFilename(final CodeTemplateParameters parameters) {
        return resolveFilename(null, parameters);
    }

    public String resolveFilename(final String name, final CodeTemplateParameters parameters) {
        return this.nameResolver.apply(name, parameters) + FILE_EXTENSION;
    }

    public boolean isProjectionDispatcherProvider() {
        return equals(PROJECTION_DISPATCHER_PROVIDER);
    }
}