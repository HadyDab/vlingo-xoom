// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.annotation.model;

import io.vlingo.symbio.store.object.StateObject;

public class DummyState extends StateObject {

    public final String id;
    public final String name;

    public DummyState(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
