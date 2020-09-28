// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.annotation.autodispatch;

import com.sun.source.util.Trees;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HandlerResolver {

    private static final String HANDLER_ENTRY_CLASSNAME = HandlerEntry.class.getCanonicalName();

    private final Trees sourceCode;
    private final List<? extends Element> configMembers;
    private final List<HandlerInvocation> handlerInvocations = new ArrayList<>();

    public static HandlerResolver with(final TypeElement handlersConfig,
                                       final ProcessingEnvironment environment) {
        return new HandlerResolver(handlersConfig, environment);
    }

    private HandlerResolver(final TypeElement handlersConfig,
                            final ProcessingEnvironment environment) {
        this.sourceCode = Trees.instance(environment);
        this.configMembers = handlersConfig.getEnclosedElements();
        this.handlerInvocations.addAll(resolveInvocations());
    }

    public HandlerInvocation find(final int index) {
        return handlerInvocations.stream().filter(invocation -> invocation.index == index).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Handler Invocation with index " + index + "not found"));
    }

    private List<HandlerInvocation> resolveInvocations() {
        final Predicate<Element> onlyHandlerEntries =
                element -> element.asType().toString().startsWith(HANDLER_ENTRY_CLASSNAME);

        return configMembers.stream().filter(onlyHandlerEntries).map(handlerEntry ->
                new HandlerInvocation(sourceCode, handlerEntry, configMembers))
                .collect(Collectors.toList());
    }

}