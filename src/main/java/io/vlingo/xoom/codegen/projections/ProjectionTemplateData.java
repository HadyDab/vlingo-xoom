// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.codegen.projections;

import io.vlingo.xoom.codegen.*;

import java.io.File;
import java.util.List;

import static io.vlingo.xoom.codegen.CodeTemplateParameter.*;
import static io.vlingo.xoom.codegen.CodeTemplateStandard.*;
import static io.vlingo.xoom.codegen.storage.ModelClassification.QUERY;
import static io.vlingo.xoom.codegen.storage.StorageType.STATE_STORE;

public class ProjectionTemplateData extends TemplateData {

    private static final String PACKAGE_PATTERN = "%s.%s.%s";
    private static final String PARENT_PACKAGE_NAME = "infrastructure";
    private static final String PERSISTENCE_PACKAGE_NAME = "persistence";

    private final String protocolName;
    private final String absolutePath;
    private final CodeTemplateParameters templateParameters;

    public static ProjectionTemplateData from(final String basePackage,
                                              final String projectPath,
                                              final String protocolName,
                                              final List<Content> contents,
                                              final ProjectionType projectionType,
                                              final TemplateData entityDataTemplateData) {
        return new ProjectionTemplateData(basePackage, projectPath, protocolName,
                contents, projectionType, entityDataTemplateData);
    }

    private ProjectionTemplateData (final String basePackage,
                                    final String projectPath,
                                    final String protocolName,
                                    final List<Content> contents,
                                    final ProjectionType projectionType,
                                    final TemplateData entityDataTemplateData) {
        this.templateParameters =
                loadParameters(resolvePackage(basePackage), protocolName,
                        contents, projectionType, entityDataTemplateData);

        this.absolutePath =
                resolveAbsolutePath(basePackage, projectPath,
                        PARENT_PACKAGE_NAME, PERSISTENCE_PACKAGE_NAME);

        this.protocolName = protocolName;
    }

    private CodeTemplateParameters loadParameters(final String packageName,
                                                  final String protocolName,
                                                  final List<Content> contents,
                                                  final ProjectionType projectionType,
                                                  final TemplateData entityDataTemplateData) {
        final String stateName = STATE.resolveClassname(protocolName);
        final String projectionName = PROJECTION.resolveClassname(protocolName);
        final String entityDataName = ENTITY_DATA.resolveClassname(protocolName);

        final List<ImportParameter> imports =
                resolveImports(stateName, contents, entityDataTemplateData.parameters());

        return CodeTemplateParameters.with(PACKAGE_NAME, packageName).and(IMPORTS, imports)
                .and(PROJECTION_NAME, projectionName).and(STATE_NAME, stateName)
                .and(MODEL_CLASSIFICATION, QUERY).and(STORAGE_TYPE, STATE_STORE)
                .and(ENTITY_DATA_NAME, entityDataName).and(PROJECTION_TYPE, projectionType)
                .andResolve(STORE_PROVIDER_NAME, param -> STORE_PROVIDER.resolveClassname(param));
    }

    private List<ImportParameter> resolveImports(final String stateName,
                                                 final List<Content> contents,
                                                 final CodeTemplateParameters entityDataTemplateParameters) {
        final String stateQualifiedName =
                ContentQuery.findFullyQualifiedClassName(STATE, stateName, contents);

        final String entityDataQualifiedName =
                entityDataTemplateParameters.find(ENTITY_DATA_QUALIFIED_CLASS_NAME);

        return ImportParameter.of(stateQualifiedName, entityDataQualifiedName);
    }

    private String resolvePackage(final String basePackage) {
        return String.format(PACKAGE_PATTERN, basePackage, PARENT_PACKAGE_NAME,
                PERSISTENCE_PACKAGE_NAME).toLowerCase();
    }

    @Override
    public File file() {
        return buildFile(absolutePath, protocolName);
    }

    @Override
    public CodeTemplateParameters parameters() {
        return templateParameters;
    }

    @Override
    public CodeTemplateStandard standard() {
        return PROJECTION;
    }

}