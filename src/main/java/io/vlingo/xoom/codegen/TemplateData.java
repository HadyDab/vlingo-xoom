// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.codegen;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.nio.file.Paths;

public abstract class TemplateData {

    private static final String[] SOURCE_FOLDER = {"src", "main", "java"};

    public abstract File file();

    public abstract CodeTemplateParameters parameters();

    public abstract CodeTemplateStandard standard();

    protected String resolveAbsolutePath(final String basePackage, final String projectPath, final String ...additionalFolders) {
        final String[] partialPath = ArrayUtils.addAll(SOURCE_FOLDER, basePackage.split("\\."));
        final String[] relativePath = ArrayUtils.addAll(partialPath, additionalFolders);
        return Paths.get(projectPath, relativePath).toString();
    }

    protected File buildFile(final String absolutePath) {
        return buildFile(absolutePath, standard().resolveFilename(parameters()));
    }

    protected File buildFile(final String absolutePath, final String filename) {
        return fileWith(absolutePath, standard().resolveFilename(filename, parameters()));
    }

    private File fileWith(final String absolutePath, final String filename) {
        return new File(Paths.get(absolutePath, filename).toString());
    }

}