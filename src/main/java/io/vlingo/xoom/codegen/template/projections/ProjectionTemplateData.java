// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.codegen.template.projections;

import io.vlingo.xoom.codegen.content.Content;
import io.vlingo.xoom.codegen.content.ContentQuery;
import io.vlingo.xoom.codegen.parameter.ImportParameter;
import io.vlingo.xoom.codegen.template.TemplateData;
import io.vlingo.xoom.codegen.template.TemplateParameters;
import io.vlingo.xoom.codegen.template.TemplateStandard;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vlingo.xoom.codegen.template.TemplateParameter.*;
import static io.vlingo.xoom.codegen.template.TemplateStandard.*;
import static io.vlingo.xoom.codegen.template.storage.Model.QUERY;
import static io.vlingo.xoom.codegen.template.storage.StorageType.STATE_STORE;

public class ProjectionTemplateData extends TemplateData {

    private static final String PACKAGE_PATTERN = "%s.%s.%s";
    private static final String PARENT_PACKAGE_NAME = "infrastructure";
    private static final String PERSISTENCE_PACKAGE_NAME = "persistence";

    private final String protocolName;
    private final TemplateParameters parameters;

    public static ProjectionTemplateData from(final String basePackage,
                                              final String protocolName,
                                              final List<Content> contents,
                                              final ProjectionType projectionType,
                                              final List<TemplateData> templatesData) {
        return new ProjectionTemplateData(basePackage, protocolName,
                contents, projectionType, templatesData);
    }

    private ProjectionTemplateData (final String basePackage,
                                    final String protocolName,
                                    final List<Content> contents,
                                    final ProjectionType projectionType,
                                    final List<TemplateData> templatesData) {
        this.parameters =
                loadParameters(resolvePackage(basePackage), protocolName,
                        contents, projectionType, templatesData);

        this.protocolName = protocolName;
    }

    private TemplateParameters loadParameters(final String packageName,
                                              final String protocolName,
                                              final List<Content> contents,
                                              final ProjectionType projectionType,
                                              final List<TemplateData> templatesData) {
        final String stateName = STATE.resolveClassname(protocolName);
        final String projectionName = PROJECTION.resolveClassname(protocolName);
        final String entityDataName = ENTITY_DATA.resolveClassname(protocolName);
        final String modelPackage = ContentQuery.findPackage(STATE, stateName, contents);

        final Set<ImportParameter> imports =
                resolveImports(stateName, entityDataName, contents,
                        projectionType, templatesData);

        return TemplateParameters.with(PACKAGE_NAME, packageName).and(IMPORTS, imports)
                .and(PROJECTION_NAME, projectionName).and(STATE_NAME, stateName)
                .and(MODEL, QUERY).and(STORAGE_TYPE, STATE_STORE)
                .and(EVENT_TYPES_NAME, EVENT_TYPES.resolveClassname())
                .and(ENTITY_DATA_NAME, entityDataName).and(PROJECTION_TYPE, projectionType)
                .and(EVENTS_NAMES, ContentQuery.findClassNames(DOMAIN_EVENT, modelPackage, contents))
                .andResolve(STORE_PROVIDER_NAME, param -> STORE_PROVIDER.resolveClassname(param));
    }

    private Set<ImportParameter> resolveImports(final String stateName,
                                                final String entityDataName,
                                                final List<Content> contents,
                                                final ProjectionType projectionType,
                                                final List<TemplateData> templatesData) {
        final String stateQualifiedName =
                ContentQuery.findFullyQualifiedClassName(STATE, stateName, contents);

        final String entityDataQualifiedName =
                ContentQuery.findFullyQualifiedClassName(ENTITY_DATA, entityDataName, contents);

        if(projectionType.isOperationBased()) {
            return ImportParameter.of(stateQualifiedName, entityDataQualifiedName);
        }

        return templatesData.stream().filter(data -> data.hasStandard(EVENT_TYPES))
                .map(data -> data.parameters().<String>find(EVENT_TYPES_QUALIFIED_NAME))
                .map(qualifiedName -> ImportParameter.of(entityDataQualifiedName, qualifiedName))
                .flatMap(imports -> imports.stream()).collect(Collectors.toSet());
    }

    private String resolvePackage(final String basePackage) {
        return String.format(PACKAGE_PATTERN, basePackage, PARENT_PACKAGE_NAME,
                PERSISTENCE_PACKAGE_NAME).toLowerCase();
    }

    @Override
    public TemplateParameters parameters() {
        return parameters;
    }

    @Override
    public TemplateStandard standard() {
        return PROJECTION;
    }

    @Override
    public String filename() {
        return standard().resolveFilename(protocolName, parameters);
    }

}
