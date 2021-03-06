package io.vlingo.xoom.codegen.template.storage;

import io.vlingo.xoom.codegen.content.Content;
import io.vlingo.xoom.codegen.content.ContentQuery;
import io.vlingo.xoom.codegen.template.TemplateStandard;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vlingo.xoom.codegen.template.TemplateStandard.DOMAIN_EVENT;
import static io.vlingo.xoom.codegen.template.TemplateStandard.STATE;

public enum StorageType {

    NONE(),

    JOURNAL("JOURNAL", "Journal", "SourcedTypeRegistry",
            "io.vlingo.lattice.model.sourcing",
            DOMAIN_EVENT),

    OBJECT_STORE("OBJECT_STORE", "ObjectStore", "ObjectTypeRegistry",
            "io.vlingo.lattice.model.object",
            STATE),

    STATE_STORE("STATE_STORE", "StateStore", "StatefulTypeRegistry",
            "io.vlingo.lattice.model.stateful",
            STATE);

    private static final String STORE_PROVIDER_NAME_SUFFIX = "Provider";

    public final String key;
    public final String title;
    public final String typeRegistryClassName;
    private final String typeRegistryPackage;
    public final TemplateStandard adapterSourceClassStandard;

    StorageType() {
        this(null, null, null, null, null);
    }

    StorageType(final String key,
                final String title,
                final String typeRegistryClassName,
                final String typeRegistryPackage,
                final TemplateStandard adapterSourceClassStandard) {
        this.key = key;
        this.title = title;
        this.typeRegistryClassName = typeRegistryClassName;
        this.typeRegistryPackage = typeRegistryPackage;
        this.adapterSourceClassStandard = adapterSourceClassStandard;
    }

    public static StorageType of(final String storage) {
        return valueOf(storage.toUpperCase());
    }

    public String resolveProviderNameFrom(final Model model) {
        final String prefix = model.isDomainModel() ? title : model.title + title;
        return prefix + STORE_PROVIDER_NAME_SUFFIX;
    }

    public Set<String> resolveTypeRegistryQualifiedNames(final Boolean useCQRS) {
        return findRelatedStorageTypes(useCQRS)
                .map(storageType -> storageType.typeRegistryQualifiedClassName())
                .collect(Collectors.toSet());
    }

    public String resolveTypeRegistryObjectName(final Model model) {
        if(!model.isQueryModel()) {
            return typeRegistryObjectName();
        }
        return STATE_STORE.typeRegistryObjectName();
    }

    public Stream<StorageType> findRelatedStorageTypes(final Boolean useCQRS) {
        if(!isEnabled()) {
            return Stream.empty();
        }
        if(!useCQRS || isStateful()) {
            return Stream.of(this);
        }
        return Stream.of(this, STATE_STORE);
    }

    public Boolean requireAdapters(final Model model) {
        return !model.isQueryModel() || isStateful();
    }

    public Set<String> resolveAdaptersQualifiedName(final Model model, final List<Content> contents) {
        if(requireAdapters(model)) {
            return ContentQuery.findFullyQualifiedClassNames(adapterSourceClassStandard, contents);
        }
        return Collections.emptySet();
    }

    public boolean isEnabled() {
        return !equals(NONE);
    }

    public Boolean isStateful() {
        return equals(STATE_STORE);
    }

    public String typeRegistryObjectName() {
        return StringUtils.uncapitalize(typeRegistryClassName);
    }

    private String typeRegistryQualifiedClassName() {
        return typeRegistryPackage + "." + typeRegistryClassName;
    }

}