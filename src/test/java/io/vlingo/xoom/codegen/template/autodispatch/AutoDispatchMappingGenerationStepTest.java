// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.codegen.template.autodispatch;

import io.vlingo.xoom.OperatingSystem;
import io.vlingo.xoom.codegen.CodeGenerationContext;
import io.vlingo.xoom.codegen.content.Content;
import io.vlingo.xoom.codegen.parameter.CodeGenerationParameters;
import io.vlingo.xoom.codegen.template.TemplateFile;
import io.vlingo.xoom.codegen.template.storage.QueriesTemplateDataFactory;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static io.vlingo.xoom.codegen.parameter.Label.*;
import static io.vlingo.xoom.codegen.template.TemplateParameter.PACKAGE_NAME;
import static io.vlingo.xoom.codegen.template.TemplateStandard.QUERIES_ACTOR;
import static io.vlingo.xoom.codegen.template.TemplateStandard.*;

public class AutoDispatchMappingGenerationStepTest {

    @Test
    public void testThatAutoDispatchMappingsAreGenerated() {
        final String basePackage = "io.vlingo.xoomapp";
        final String persistencePackage = basePackage + ".infrastructure.persistence";

        final CodeGenerationParameters parameters =
                CodeGenerationParameters.from(PACKAGE, basePackage)
                        .add(REST_RESOURCES, "Author;Book").add(CQRS, true);

        final CodeGenerationContext context =
                CodeGenerationContext.with(parameters).contents(contents());

        QueriesTemplateDataFactory.from(persistencePackage, true, Arrays.asList(contents())).stream()
                .forEach(data -> {
                    final String packageName = data.parameters().find(PACKAGE_NAME);
                    context.registerTemplateProcessing(data, "package " + packageName + ";");
                });

        new AutoDispatchMappingGenerationStep().process(context);

        Assert.assertEquals(20, context.contents().size());

        final Content authorMappingContent =
                context.contents().stream().filter(content -> content.retrieveClassName().equals("AuthorResource"))
                        .findFirst().get();

        Assert.assertTrue(authorMappingContent.contains("@Queries(protocol = AuthorQueries.class, actor = AuthorQueriesActor.class)"));

        final Content authorHandlersMappingContent =
                context.contents().stream().filter(content -> content.retrieveClassName().equals("AuthorResourceHandlers"))
                        .findFirst().get();

        Assert.assertTrue(authorHandlersMappingContent.contains("public static final HandlerEntry<Three<Completes<AuthorState>, Stage, AuthorData>> defineWithHandler ="));
        Assert.assertTrue(authorHandlersMappingContent.contains("HandlerEntry.of(DEFINE_PLACEHOLDER, (stage, data) -> Author.definePlaceholder(stage, data.placeholderValue));"));
        Assert.assertTrue(authorHandlersMappingContent.contains("public static final HandlerEntry<Two<AuthorData, AuthorState>> adaptStateHandler ="));
        Assert.assertTrue(authorHandlersMappingContent.contains("HandlerEntry.of(ADAPT_STATE, AuthorData::from);"));
        Assert.assertTrue(authorHandlersMappingContent.contains("public static final HandlerEntry<Two<Completes<Collection<AuthorData>>, AuthorQueries>> queryAllHandler ="));
        Assert.assertTrue(authorHandlersMappingContent.contains("HandlerEntry.of(QUERY_ALL, AuthorQueries::authors);"));
    }

    private Content[] contents() {
        return new Content[]{
                Content.with(AGGREGATE_PROTOCOL, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "author").toString(), "Author.java"), null, null, AUTHOR_CONTENT_TEXT),
                Content.with(AGGREGATE_PROTOCOL, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "book").toString(), "Book.java"), null, null, BOOK_CONTENT_TEXT),
                Content.with(AGGREGATE, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "author").toString(), "AuthorEntity.java"), null, null, AUTHOR_AGGREGATE_CONTENT_TEXT),
                Content.with(AGGREGATE, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "book").toString(), "BookEntity.java"), null, null, BOOK_AGGREGATE_CONTENT_TEXT),
                Content.with(STATE, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "author").toString(), "AuthorState.java"), null, null, AUTHOR_STATE_CONTENT_TEXT),
                Content.with(STATE, new TemplateFile(Paths.get(MODEL_PACKAGE_PATH, "book").toString(), "BookState.java"), null, null, BOOK_STATE_CONTENT_TEXT),
                Content.with(ENTITY_DATA, new TemplateFile(Paths.get(INFRASTRUCTURE_PACKAGE_PATH).toString(), "AuthorData.java"), null, null, AUTHOR_DATA_CONTENT_TEXT),
                Content.with(ENTITY_DATA, new TemplateFile(Paths.get(INFRASTRUCTURE_PACKAGE_PATH).toString(), "BookData.java"), null, null, BOOK_DATA_CONTENT_TEXT),
                Content.with(QUERIES, new TemplateFile(Paths.get(PERSISTENCE_PACKAGE_PATH).toString(), "AuthorQueries.java"), null, null, AUTHOR_QUERIES_CONTENT_TEXT),
                Content.with(QUERIES, new TemplateFile(Paths.get(PERSISTENCE_PACKAGE_PATH).toString(), "BookQueries.java"), null, null, BOOK_QUERIES_CONTENT_TEXT),
                Content.with(QUERIES_ACTOR, new TemplateFile(Paths.get(PERSISTENCE_PACKAGE_PATH).toString(), "AuthorQueriesActor.java"), null, null, AUTHOR_QUERIES_ACTOR_CONTENT_TEXT),
                Content.with(QUERIES_ACTOR, new TemplateFile(Paths.get(PERSISTENCE_PACKAGE_PATH).toString(), "BookQueriesActor.java"), null, null, BOOK_QUERIES_ACTOR_CONTENT_TEXT)
        };
    }

    private static final String PROJECT_PATH =
            OperatingSystem.detect().isWindows() ?
                    Paths.get("D:\\projects", "xoom-app").toString() :
                    Paths.get("/home", "xoom-app").toString();

    private static final String MODEL_PACKAGE_PATH =
            Paths.get(PROJECT_PATH, "src", "main", "java",
                    "io", "vlingo", "xoomapp", "model").toString();

    private static final String INFRASTRUCTURE_PACKAGE_PATH =
            Paths.get(PROJECT_PATH, "src", "main", "java",
                    "io", "vlingo", "xoomapp", "infrastructure").toString();

    private static final String PERSISTENCE_PACKAGE_PATH =
            Paths.get(INFRASTRUCTURE_PACKAGE_PATH, "persistence").toString();

    private static final String AUTHOR_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model.author; \\n" +
                    "public interface Author { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model.book; \\n" +
                    "public interface Book { \\n" +
                    "... \\n" +
                    "}";

    private static final String AUTHOR_STATE_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model.author; \\n" +
                    "public class AuthorState { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_STATE_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model.book; \\n" +
                    "public class BookState { \\n" +
                    "... \\n" +
                    "}";

    private static final String AUTHOR_AGGREGATE_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model.author; \\n" +
                    "public class AuthorEntity { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_AGGREGATE_CONTENT_TEXT =
            "package io.vlingo.xoomapp.model.book; \\n" +
                    "public class BookEntity { \\n" +
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

    private static final String AUTHOR_QUERIES_CONTENT_TEXT =
            "package io.vlingo.xoomapp.infrastructure.persistence; \\n" +
                    "public interface AuthorQueries { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_QUERIES_CONTENT_TEXT =
            "package io.vlingo.xoomapp.infrastructure.persistence; \\n" +
                    "public interface BookQueries { \\n" +
                    "... \\n" +
                    "}";

    private static final String AUTHOR_QUERIES_ACTOR_CONTENT_TEXT =
            "package io.vlingo.xoomapp.infrastructure.persistence; \\n" +
                    "public class AuthorQueriesActor { \\n" +
                    "... \\n" +
                    "}";

    private static final String BOOK_QUERIES_ACTOR_CONTENT_TEXT =
            "package io.vlingo.xoomapp.infrastructure.persistence; \\n" +
                    "public class BookQueriesActor { \\n" +
                    "... \\n" +
                    "}";
}
