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

package org.gradle.testkit.functional.internal;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.testkit.functional.internal.dist.GradleDistribution;
import org.gradle.testkit.functional.internal.dist.InstalledGradleDistribution;
import org.gradle.testkit.functional.internal.dist.URILocatedGradleDistribution;
import org.gradle.testkit.functional.internal.dist.VersionBasedGradleDistribution;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ToolingApiGradleExecutor implements GradleExecutor {
    private final Logger logger = Logging.getLogger(ToolingApiGradleExecutor.class);
    private final GradleDistribution gradleDistribution;
    private final File workingDirectory;
    private File gradleUserHomeDir;
    private List<String> taskNames;
    private List<String> arguments;

    public ToolingApiGradleExecutor(GradleDistribution gradleDistribution, File workingDirectory) {
        this.gradleDistribution = gradleDistribution;
        this.workingDirectory = workingDirectory;
    }

    public void withGradleUserHomeDir(File gradleUserHomeDir) {
        this.gradleUserHomeDir = gradleUserHomeDir;
    }

    public void withTasks(List<String> taskNames) {
        this.taskNames = taskNames;
    }

    public void withArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public GradleExecutionResult run() {
        final ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        final ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        final GradleExecutionResult gradleExecutionResult = new GradleExecutionResult(standardOutput, standardError);

        GradleConnector gradleConnector = buildConnector();
        ProjectConnection connection = gradleConnector.connect();

        try {
            BuildLauncher launcher = connection.newBuild();
            launcher.setStandardOutput(standardOutput);
            launcher.setStandardError(standardError);

            String[] argumentArray = new String[arguments.size()];
            arguments.toArray(argumentArray);
            launcher.withArguments(argumentArray);

            String[] tasksArray = new String[taskNames.size()];
            taskNames.toArray(tasksArray);
            launcher.forTasks(tasksArray);

            launcher.run();
        } catch(RuntimeException t) {
            gradleExecutionResult.setThrowable(t);
        } finally {
            if(connection != null) {
                connection.close();
            }
        }

        return gradleExecutionResult;
    }

    private GradleConnector buildConnector() {
        DefaultGradleConnector gradleConnector = (DefaultGradleConnector)GradleConnector.newConnector();

        if(gradleUserHomeDir != null) {
            gradleConnector.useGradleUserHomeDir(gradleUserHomeDir);
        }

        gradleConnector.forProjectDirectory(workingDirectory);
        gradleConnector.searchUpwards(false);
        gradleConnector.daemonMaxIdleTime(120, TimeUnit.SECONDS);
        useGradleDistribution(gradleConnector);
        return gradleConnector;
    }

    private void useGradleDistribution(GradleConnector gradleConnector) {
        if(logger.isDebugEnabled()) {
            logger.debug("Using %s", gradleDistribution.getDisplayName());
        }

        if(gradleDistribution instanceof VersionBasedGradleDistribution) {
            gradleConnector.useGradleVersion(((VersionBasedGradleDistribution) gradleDistribution).getVersion());
        } else if(gradleDistribution instanceof InstalledGradleDistribution) {
            gradleConnector.useInstallation(((InstalledGradleDistribution) gradleDistribution).getGradleHomeDir());
        } else if(gradleDistribution instanceof URILocatedGradleDistribution) {
            gradleConnector.useDistribution(((URILocatedGradleDistribution) gradleDistribution).getURI());
        }
    }
}
