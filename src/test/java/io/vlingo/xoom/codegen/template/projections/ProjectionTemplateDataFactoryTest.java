// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.codegen.template.projections;

import io.vlingo.xoom.OperatingSystem;
import io.vlingo.xoom.codegen.CodeGenerationContext;
import io.vlingo.xoom.codegen.parameter.Label;
import io.vlingo.xoom.codegen.template.TemplateData;
import io.vlingo.xoom.codegen.template.TemplateFile;
import io.vlingo.xoom.codegen.template.TemplateParameters;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.vlingo.xoom.codegen.parameter.Label.PROJECTION_TYPE;
import static io.vlingo.xoom.codegen.parameter.Label.*;
import static io.vlingo.xoom.codegen.template.TemplateParameter.*;
import static io.vlingo.xoom.codegen.template.TemplateStandard.*;

public class ProjectionTemplateDataFactoryTest {

    @Test
    public void testCustomProjectionTemplateDataBuild() {
        final Map<Label, String> parameters =
                new HashMap<Label, String>() {{
                    put(PACKAGE, "io.vlingo.xoomapp");
                    put(PROJECTION_TYPE, ProjectionType.CUSTOM.name());
                    put(PROJECTABLES, "\"AuthorRegistered\", \"AuthorRated\";\"BookSoldOut\", \"BookPurchased\"");
                }};

        final CodeGenerationContext context =
                CodeGenerationContext.using(Mockito.mock(Filer.class), Mockito.mock(Element.class)).on(parameters)
                        .addContent(PROJECTION, new TemplateFile(Paths.get(PERSISTENCE_PACKAGE_PATH).toString(), "AuthorProjectionActor.java"),  AUTHOR_PROJECTION_ACTOR_CONTENT_TEXT)
                        .addContent(PROJECTION, new TemplateFile(Paths.get(PERSISTENCE_PACKAGE_PATH).toString(), "BookProjectionActor.java"),  BOOK_PROJECTION_ACTOR_CONTENT_TEXT);

        loadContents(context);

        final TemplateParameters providerTemplateDataParameters =
                ProjectionTemplateDataFactory.build(context).get(0).parameters();

        Assert.assertEquals(EXPECTED_PERSISTENCE_PACKAGE, providerTemplateDataParameters.find(PACKAGE_NAME));
        Assert.assertEquals("ProjectToDescription.with(AuthorProjectionActor.class, \"AuthorRegistered\", \"AuthorRated\"),", providerTemplateDataParameters.<List<ProjectToDescriptionParameter>>find(PROJECTION_TO_DESCRIPTION).get(0).getInitializationCommand());
        Assert.assertEquals("ProjectToDescription.with(BookProjectionActor.class, \"BookSoldOut\", \"BookPurchased\")", providerTemplateDataParameters.<List<ProjectToDescriptionParameter>>find(PROJECTION_TO_DESCRIPTION).get(1).getInitializationCommand());
    }

    @Test
    public void testEventBasedProjectionTemplateDataBuild() {
        final Map<Label, String> parameters =
                new HashMap<Label, String>() {{
                    put(PACKAGE, "io.vlingo.xoomapp");
                    put(PROJECTION_TYPE, ProjectionType.EVENT_BASED.name());
                }};

        final CodeGenerationContext context =
                CodeGenerationContext.with(parameters);

        loadContents(context);

        final List<TemplateData> allTemplatesData =
                ProjectionTemplateDataFactory.build(context);

        //General Assert

        Assert.assertEquals(4, allTemplatesData.size());
        Assert.assertEquals(1, allTemplatesData.stream().filter(data -> data.hasStandard(PROJECTION_DISPATCHER_PROVIDER)).count());
        Assert.assertEquals(1, allTemplatesData.stream().filter(data -> data.hasStandard(EVENT_TYPES)).count());
        Assert.assertEquals(2, allTemplatesData.stream().filter(data -> data.hasStandard(PROJECTION)).count());

        //Assert for ProjectionDispatcherProvider

        final TemplateData providerTemplateData = allTemplatesData.stream().filter(data -> data.hasStandard(PROJECTION_DISPATCHER_PROVIDER)).findFirst().get();
        final TemplateParameters providerTemplateDataParameters = providerTemplateData.parameters();

        Assert.assertEquals(EXPECTED_PERSISTENCE_PACKAGE, providerTemplateDataParameters.find(PACKAGE_NAME));
        Assert.assertEquals("ProjectToDescription.with(BookProjectionActor.class, \"Event name here\", \"Another Event name here\"),", providerTemplateDataParameters.<List<ProjectToDescriptionParameter>>find(PROJECTION_TO_DESCRIPTION).get(0).getInitializationCommand());
        Assert.assertEquals("ProjectToDescription.with(AuthorProjectionActor.class, \"Event name here\", \"Another Event name here\")", providerTemplateDataParameters.<List<ProjectToDescriptionParameter>>find(PROJECTION_TO_DESCRIPTION).get(1).getInitializationCommand());

        //Assert for Projections

        final List<TemplateData> projectionsTemplatesData =
                allTemplatesData.stream()
                        .filter(data -> data.hasStandard(PROJECTION))
                        .collect(Collectors.toList());

        IntStream.range(0, 1).forEach(templateIndex -> {
            final String rootName = templateIndex == 0 ? "Book" : "Author";
            final String expectedName = rootName + "ProjectionActor";
            final String expectedStateName = rootName + "State";
            final String expectedStaticEventTypes = "io.vlingo.xoomapp.infrastructure.EventTypes";
            final TemplateData projectionTemplateData = projectionsTemplatesData.get(templateIndex);
            final TemplateParameters projectionTemplateDataParameters = projectionTemplateData.parameters();
            Assert.assertEquals(EXPECTED_PERSISTENCE_PACKAGE, projectionTemplateDataParameters.find(PACKAGE_NAME));
            Assert.assertEquals(expectedName, projectionTemplateDataParameters.find(PROJECTION_NAME));
            Assert.assertEquals(expectedStateName, projectionTemplateDataParameters.find(STATE_NAME));
            Assert.assertTrue(projectionTemplateDataParameters.hasImport(expectedStaticEventTypes));
            Assert.assertEquals(expectedName + ".java", projectionTemplateData.filename());
        });
    }

    @Test
    public void testOperationBasedProjectionTemplateDataBuild() {
        final Map<Label, String> parameters =
                new HashMap<Label, String>() {{
                    put(PACKAGE, "io.vlingo.xoomapp");
                    put(PROJECTION_TYPE, ProjectionType.OPERATION_BASED.name());
                }};

        final CodeGenerationContext context =
                CodeGenerationContext.with(parameters);

        loadContents(context);

        final List<TemplateData> allTemplatesData =
                ProjectionTemplateDataFactory.build(context);

        //General Assert

        Assert.assertEquals(3, allTemplatesData.size());
        Assert.assertEquals(1, allTemplatesData.stream().filter(data -> data.hasStandard(PROJECTION_DISPATCHER_PROVIDER)).count());
        Assert.assertEquals(2, allTemplatesData.stream().filter(data -> data.hasStandard(PROJECTION)).count());

        //Assert for ProjectionDispatcherProvider

        final TemplateData providerTemplateData = allTemplatesData.stream().filter(data -> data.hasStandard(PROJECTION_DISPATCHER_PROVIDER)).findFirst().get();
        final TemplateParameters providerTemplateDataParameters = providerTemplateData.parameters();
        Assert.assertEquals(EXPECTED_PERSISTENCE_PACKAGE, providerTemplateDataParameters.find(PACKAGE_NAME));
        Assert.assertEquals("ProjectToDescription.with(BookProjectionActor.class, \"Operation name here\", \"Another Operation name here\"),", providerTemplateDataParameters.<List<ProjectToDescriptionParameter>>find(PROJECTION_TO_DESCRIPTION).get(0).getInitializationCommand());
        Assert.assertEquals("ProjectToDescription.with(AuthorProjectionActor.class, \"Operation name here\", \"Another Operation name here\")", providerTemplateDataParameters.<List<ProjectToDescriptionParameter>>find(PROJECTION_TO_DESCRIPTION).get(1).getInitializationCommand());
    }

    private void loadContents(final CodeGenerationContext context) {
        context.addContent(STATE, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "author").toString(), "AuthorState.java"), AUTHOR_STATE_CONTENT_TEXT);
        context.addContent(STATE, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "book").toString(), "BookState.java"), BOOK_STATE_CONTENT_TEXT);
        context.addContent(AGGREGATE_PROTOCOL, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "author").toString(), "Author.java"),  AUTHOR_CONTENT_TEXT);
        context.addContent(AGGREGATE_PROTOCOL, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "book").toString(), "Book.java"),  BOOK_CONTENT_TEXT);
        context.addContent(ENTITY_DATA, new TemplateFile(Paths.get(INFRASTRUCTURE_PACKAGE_PATH).toString(), "AuthorData.java"), AUTHOR_DATA_CONTENT_TEXT);
        context.addContent(ENTITY_DATA, new TemplateFile(Paths.get(INFRASTRUCTURE_PACKAGE_PATH).toString(), "BookData.java"), BOOK_DATA_CONTENT_TEXT);
    }

    private static final String EXPECTED_INFRA_PACKAGE = "io.vlingo.xoomapp.infrastructure";
    private static final String EXPECTED_PERSISTENCE_PACKAGE = "io.vlingo.xoomapp.infrastructure.persistence";

    private static final String PROJECT_PATH =
            OperatingSystem.detect().isWindows() ?
                    Paths.get("D:\\projects", "xoom-app").toString() :
                    Paths.get("/home", "xoom-app").toString();

    private static final String MODEL_PACKAGE_PATH =
            Paths.get(PROJECT_PATH, "src", "main", "java",
                    "io", "vlingo", "xoomapp", "model").toString();

    private static final String PERSISTENCE_PACKAGE_PATH =
            Paths.get(PROJECT_PATH, "src", "main", "java",
                    "io", "vlingo", "xoomapp", "infrastructure", "persistence").toString();

    private static final String AUTHOR_PROJECTION_ACTOR_CONTENT_TEXT =
            "package io.vlingo.xoomapp.infrastructure.persistence; \\n" +
                    "public class AuthorProjectionActor { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_PROJECTION_ACTOR_CONTENT_TEXT =
            "package io.vlingo.xoomapp.infrastructure.persistence; \\n" +
                    "public class BookProjectionActor { \\n" +
                    "... \\n" +
                    "}";

    private static final String INFRASTRUCTURE_PACKAGE_PATH =
            Paths.get(PROJECT_PATH, "src", "main", "java",
                    "io", "vlingo", "xoomapp", "infrastructure").toString();

    private static final String AUTHOR_STATE_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model; \\n" +
                    "public class AuthorState { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_STATE_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model; \\n" +
                    "public class BookState { \\n" +
                    "... \\n" +
                    "}";

    private static final String AUTHOR_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model; \\n" +
                    "public interface Author { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model; \\n" +
                    "public interface Book { \\n" +
                    "... \\n" +
                    "}";

    private static final String AUTHOR_DATA_CONTENT_TEXT =
            "package io.vlingo.xoomapp.infrastructure; \\n" +
                    "public class AuthorData { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_DATA_CONTENT_TEXT =
            "package io.vlingo.xoomapp.infrastructure; \\n" +
                    "public class BookData { \\n" +
                    "... \\n" +
                    "}";

}
