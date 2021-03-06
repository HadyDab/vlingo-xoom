// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.storage;

import io.vlingo.actors.Stage;
import io.vlingo.symbio.store.common.jdbc.Configuration;
import io.vlingo.symbio.store.dispatch.Dispatcher;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.inmemory.InMemoryStateStoreActor;
import io.vlingo.xoom.annotation.persistence.Persistence.StorageType;

import java.util.Arrays;

public class InMemoryStateStoreActorBuilder implements StoreActorBuilder {

    @Override
    public Object build(final Stage stage, final Dispatcher dispatcher, final Configuration configuration) {
        return stage.actorFor(StateStore.class, InMemoryStateStoreActor.class, Arrays.asList(dispatcher));
    }

    @Override
    public boolean support(final StorageType storageType, final DatabaseType databaseType) {
        return storageType.isStateStore() && databaseType.isInMemory();
    }

}
