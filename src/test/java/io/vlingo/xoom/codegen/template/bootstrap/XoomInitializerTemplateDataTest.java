// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.codegen.template.bootstrap;

import io.vlingo.actors.GridAddressFactory;
import io.vlingo.common.identity.IdentityGeneratorType;
import io.vlingo.xoom.OperatingSystem;
import io.vlingo.xoom.codegen.CodeGenerationContext;
import io.vlingo.xoom.codegen.parameter.Label;
import io.vlingo.xoom.codegen.template.TemplateFile;
import io.vlingo.xoom.codegen.template.TemplateParameter;
import io.vlingo.xoom.codegen.template.TemplateParameters;
import io.vlingo.xoom.codegen.template.projections.ProjectionType;
import io.vlingo.xoom.codegen.template.storage.StorageType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.vlingo.xoom.codegen.parameter.Label.APPLICATION_NAME;
import static io.vlingo.xoom.codegen.parameter.Label.BLOCKING_MESSAGING;
import static io.vlingo.xoom.codegen.parameter.Label.STORAGE_TYPE;
import static io.vlingo.xoom.codegen.parameter.Label.*;
import static io.vlingo.xoom.codegen.template.TemplateParameter.REST_RESOURCES;
import static io.vlingo.xoom.codegen.template.TemplateParameter.*;
import static io.vlingo.xoom.codegen.template.TemplateStandard.REST_RESOURCE;
import static io.vlingo.xoom.codegen.template.TemplateStandard.STORE_PROVIDER;
import static io.vlingo.xoom.codegen.template.storage.StorageType.STATE_STORE;

public class XoomInitializerTemplateDataTest {

    @Test
    public void testXoomInitializerTemplateDataGenerationWithCQRS() {
        final Map<Label, String> codeGenerationParameters =
                new HashMap<Label, String>() {{
                    put(PACKAGE, "io.vlingo.xoomapp");
                    put(APPLICATION_NAME, "xoom-app");
                    put(CQRS, Boolean.TRUE.toString());
                    put(STORAGE_TYPE, STATE_STORE.name());
                    put(Label.PROJECTION_TYPE, ProjectionType.NONE.name());
                    put(ANNOTATIONS, Boolean.FALSE.toString());
                    put(BLOCKING_MESSAGING, Boolean.FALSE.toString());
                    put(XOOM_INITIALIZER_NAME, "AnnotatedBootstrap");
                    put(ADDRESS_FACTORY, AddressFactoryType.GRID.name());
                    put(IDENTITY_GENERATOR, IdentityGeneratorType.TIME_BASED.name());
                }};

        final CodeGenerationContext context =
                CodeGenerationContext.using(Mockito.mock(Filer.class), Mockito.mock(Element.class)).on(codeGenerationParameters)
                        .addContent(REST_RESOURCE, new TemplateFile(RESOURCE_PACKAGE_PATH, "AuthorResource.java"), AUTHOR_RESOURCE_CONTENT)
                        .addContent(REST_RESOURCE, new TemplateFile(RESOURCE_PACKAGE_PATH, "BookResource.java"), BOOK_RESOURCE_CONTENT)
                        .addContent(STORE_PROVIDER, new TemplateFile(PERSISTENCE_PACKAGE_PATH, "CommandModelStateStoreProvider.java"), COMMAND_MODEL_STORE_PROVIDER_CONTENT)
                        .addContent(STORE_PROVIDER, new TemplateFile(PERSISTENCE_PACKAGE_PATH, "QueryModelStateStoreProvider.java"), QUERY_MODEL_STORE_PROVIDER_CONTENT);

        final TemplateParameters parameters =
                BootstrapTemplateData.from(context).parameters();

        Assert.assertEquals(EXPECTED_PACKAGE, parameters.find(PACKAGE_NAME));
        Assert.assertEquals(7, parameters.<Set>find(IMPORTS).size());
        Assert.assertTrue(parameters.hasImport("io.vlingo.xoomapp.infrastructure.persistence.CommandModelStateStoreProvider"));
        Assert.assertTrue(parameters.hasImport("io.vlingo.xoomapp.infrastructure.persistence.QueryModelStateStoreProvider"));
        Assert.assertTrue(parameters.hasImport("io.vlingo.lattice.model.stateful.StatefulTypeRegistry"));
        Assert.assertTrue(parameters.hasImport(GridAddressFactory.class.getCanonicalName()));
        Assert.assertTrue(parameters.hasImport(IdentityGeneratorType.class.getCanonicalName()));
        Assert.assertTrue(parameters.hasImport("io.vlingo.xoomapp.resource.AuthorResource"));
        Assert.assertTrue(parameters.hasImport("io.vlingo.xoomapp.resource.BookResource"));

        Assert.assertEquals(2, parameters.<List>find(REST_RESOURCES).size());
        Assert.assertEquals("AuthorResource", parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(0).getClassName());
        Assert.assertEquals("authorResource", parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(0).getObjectName());
        Assert.assertEquals(false, parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(0).isLast());
        Assert.assertEquals("BookResource", parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(1).getClassName());
        Assert.assertEquals("bookResource", parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(1).getObjectName());
        Assert.assertEquals(true, parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(1).isLast());

        Assert.assertEquals(2, parameters.<List>find(PROVIDERS).size());
        Assert.assertEquals("QueryModelStateStoreProvider", parameters.<List<StoreProviderParameter>>find(PROVIDERS).get(0).getClassName());
        Assert.assertEquals("stage, statefulTypeRegistry", parameters.<List<StoreProviderParameter>>find(PROVIDERS).get(0).getArguments());
        Assert.assertEquals("CommandModelStateStoreProvider", parameters.<List<StoreProviderParameter>>find(PROVIDERS).get(1).getClassName());
        Assert.assertEquals("stage, statefulTypeRegistry", parameters.<List<StoreProviderParameter>>find(PROVIDERS).get(1).getArguments());

        Assert.assertEquals(1, parameters.<List>find(TYPE_REGISTRIES).size());
        Assert.assertEquals("StatefulTypeRegistry", parameters.<List<TypeRegistryParameter>>find(TYPE_REGISTRIES).get(0).getClassName());
        Assert.assertEquals("statefulTypeRegistry", parameters.<List<TypeRegistryParameter>>find(TYPE_REGISTRIES).get(0).getObjectName());

        Assert.assertEquals("xoom-app", parameters.find(TemplateParameter.APPLICATION_NAME));
        Assert.assertEquals(false, parameters.find(USE_PROJECTIONS));
    }

    @Test
    public void testXoomInitializerTemplateDataGenerationWithoutStorage() {
        final Map<Label, String> codeGenerationParameters =
                new HashMap<Label, String>() {{
                    put(PACKAGE, "io.vlingo.xoomapp");
                    put(APPLICATION_NAME, "xoom-app");
                    put(CQRS, Boolean.FALSE.toString());
                    put(STORAGE_TYPE, StorageType.NONE.name());
                    put(Label.PROJECTION_TYPE, ProjectionType.NONE.name());
                    put(ANNOTATIONS, Boolean.FALSE.toString());
                    put(XOOM_INITIALIZER_NAME, "XoomInitializer");
                    put(BLOCKING_MESSAGING, Boolean.FALSE.toString());
                    put(ADDRESS_FACTORY, AddressFactoryType.BASIC.name());
                    put(IDENTITY_GENERATOR, IdentityGeneratorType.TIME_BASED.name());
                }};

        final CodeGenerationContext context =
                CodeGenerationContext.using(Mockito.mock(Filer.class), Mockito.mock(Element.class)).on(codeGenerationParameters)
                        .addContent(REST_RESOURCE, new TemplateFile(RESOURCE_PACKAGE_PATH, "AuthorResource.java"), AUTHOR_RESOURCE_CONTENT);

        final TemplateParameters parameters =
                BootstrapTemplateData.from(context).parameters();

        Assert.assertEquals(false, parameters.find(USE_PROJECTIONS));
        Assert.assertEquals(1, parameters.<Set>find(IMPORTS).size());
        Assert.assertEquals(EXPECTED_PACKAGE, parameters.find(PACKAGE_NAME));
        Assert.assertEquals("xoom-app", parameters.find(TemplateParameter.APPLICATION_NAME));
        Assert.assertTrue(parameters.hasImport("io.vlingo.xoomapp.resource.AuthorResource"));

        Assert.assertEquals(1, parameters.<List>find(REST_RESOURCES).size());
        Assert.assertEquals("AuthorResource", parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(0).getClassName());
        Assert.assertEquals("authorResource", parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(0).getObjectName());
        Assert.assertEquals(true, parameters.<List<RestResourcesParameter>>find(REST_RESOURCES).get(0).isLast());

        Assert.assertTrue(parameters.<List>find(PROVIDERS).isEmpty());
        Assert.assertTrue(parameters.<List>find(TYPE_REGISTRIES).isEmpty());
    }

    private static final String EXPECTED_PACKAGE = "io.vlingo.xoomapp";

    private static final String PROJECT_PATH =
            OperatingSystem.detect().isWindows() ?
                    Paths.get("D:\\projects", "xoom-app").toString() :
                    Paths.get("/home", "xoom-app").toString();

    private static final String RESOURCE_PACKAGE_PATH =
            Paths.get(PROJECT_PATH, "src", "main", "java",
                    "io", "vlingo", "xoomapp", "resource").toString();

    private static final String INFRASTRUCTURE_PACKAGE_PATH =
            Paths.get(PROJECT_PATH, "src", "main", "java",
                    "io", "vlingo", "xoomapp", "infrastructure").toString();

    private static final String PERSISTENCE_PACKAGE_PATH =
            Paths.get(INFRASTRUCTURE_PACKAGE_PATH, "persistence").toString();

    private static final String AUTHOR_RESOURCE_CONTENT =
            "package io.vlingo.xoomapp.resource; \\n" +
                    "public class AuthorResource { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_RESOURCE_CONTENT =
            "package io.vlingo.xoomapp.resource; \\n" +
                    "public class BookResource { \\n" +
                    "... \\n" +
                    "}";

    private static final String COMMAND_MODEL_STORE_PROVIDER_CONTENT =
            "package io.vlingo.xoomapp.infrastructure.persistence; \\n" +
                    "public class CommandModelStateStoreProvider { \\n" +
                    "... \\n" +
                    "}";

    private static final String QUERY_MODEL_STORE_PROVIDER_CONTENT =
            "package io.vlingo.xoomapp.infrastructure.persistence; \\n" +
                    "public class QueryModelStateStoreProvider { \\n" +
                    "... \\n" +
                    "}";

}

