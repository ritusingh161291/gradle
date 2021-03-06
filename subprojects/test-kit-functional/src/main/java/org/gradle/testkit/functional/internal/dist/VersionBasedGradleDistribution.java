/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.testkit.functional.internal.dist;

import org.gradle.util.GradleVersion;

public final class VersionBasedGradleDistribution implements GradleDistribution {
    public final static VersionBasedGradleDistribution CURRENT = new VersionBasedGradleDistribution(GradleVersion.current().getVersion());

    private final String version;

    public VersionBasedGradleDistribution(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getDisplayName() {
        return String.format("Gradle distribution with version %s", version);
    }
}
