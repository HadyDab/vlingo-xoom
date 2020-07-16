// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.codegen.resource;

import io.vlingo.xoom.codegen.CodeTemplateParameters;
import io.vlingo.xoom.codegen.CodeTemplateStandard;
import io.vlingo.xoom.codegen.TemplateData;

import java.io.File;

import static io.vlingo.xoom.codegen.CodeTemplateParameter.PACKAGE_NAME;
import static io.vlingo.xoom.codegen.CodeTemplateParameter.REST_RESOURCE_NAME;
import static io.vlingo.xoom.codegen.CodeTemplateStandard.REST_RESOURCE;

public class RestResourceTemplateData extends TemplateData {

    private final static String PACKAGE_PATTERN = "%s.%s";
    private final static String PARENT_PACKAGE_NAME = "resource";

    private final String aggregateName;
    private final String packageName;
    private final String absolutePath;
    private final CodeTemplateParameters parameters;

    public RestResourceTemplateData(final String aggregateName,
                                    final String basePackage,
                                    final String projectPath) {
        this.aggregateName = aggregateName;
        this.packageName = resolvePackage(basePackage);
        this.absolutePath = resolveAbsolutePath(basePackage, projectPath, PARENT_PACKAGE_NAME);
        this.parameters = loadParameters();
    }

    private CodeTemplateParameters loadParameters() {
        return CodeTemplateParameters
                .with(REST_RESOURCE_NAME, REST_RESOURCE.resolveClassname(aggregateName))
                .and(PACKAGE_NAME, packageName);
    }

    private String resolvePackage(final String basePackage) {
        return String.format(PACKAGE_PATTERN, basePackage, PARENT_PACKAGE_NAME).toLowerCase();
    }

    @Override
    public CodeTemplateStandard standard() {
        return REST_RESOURCE;
    }

    @Override
    public CodeTemplateParameters parameters() {
        return parameters;
    }

    @Override
    public File file() {
        return buildFile(absolutePath, aggregateName);
    }

}