// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.codegen.template;

public abstract class TemplateData {

    public abstract TemplateParameters parameters();

    public abstract TemplateStandard standard();

    public String filename() {
        return standard().resolveFilename(parameters());
    }

    public boolean hasStandard(final TemplateStandard standard) {
        return standard().equals(standard);
    }

}
