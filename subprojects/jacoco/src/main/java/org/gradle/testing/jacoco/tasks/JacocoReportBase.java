/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.testing.jacoco.tasks;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.project.IsolatedAntBuilder;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension;
import org.gradle.util.DeprecationLogger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Base class for Jacoco report tasks.
 *
 * @since 3.4
 */
public abstract class JacocoReportBase extends JacocoBase {

    private final ConfigurableFileCollection executionData = getProject().files();
    private final ConfigurableFileCollection sourceDirectories = getProject().files();
    private final ConfigurableFileCollection classDirectories = getProject().files();
    private final ConfigurableFileCollection additionalClassDirs = getProject().files();
    private final ConfigurableFileCollection additionalSourceDirs = getProject().files();

    public JacocoReportBase() {
        onlyIf(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task element) {
                return Iterables.any(getExecutionData(), new Predicate<File>() {
                    @Override
                    public boolean apply(File file) {
                        return file.exists();
                    }
                });
            }
        });
    }

    @Inject
    protected Instantiator getInstantiator() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected IsolatedAntBuilder getAntBuilder() {
        throw new UnsupportedOperationException();
    }

    /**
     * Collection of execution data files to analyze.
     */
    @PathSensitive(PathSensitivity.NONE)
    @InputFiles
    public ConfigurableFileCollection getExecutionData() {
        return executionData;
    }

    /**
     * Collection of execution data files to analyze.
     *
     * @deprecated Use {@code getExecutionData().setFrom(...)}
     */
    @Deprecated
    public void setExecutionData(FileCollection executionData) {
        DeprecationLogger.nagUserOfDiscontinuedMethod("JacocoReportBase.setExecutionData(FileCollection)", "Use getExecutionData().from(...)");
        this.executionData.setFrom(executionData);
    }

    /**
     * Source sets that coverage should be reported for.
     */
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    public ConfigurableFileCollection getSourceDirectories() {
        return sourceDirectories;
    }

    /**
     * Source sets that coverage should be reported for.
     * @deprecated Use {@code getSourceDirectories().setFrom(...)}
     */
    @Deprecated
    public void setSourceDirectories(FileCollection sourceDirectories) {
        DeprecationLogger.nagUserOfDiscontinuedMethod("JacocoReportBase.setSourceDirectories(FileCollection)", "Use getSourceDirectories().from(...)");
        this.sourceDirectories.setFrom(sourceDirectories);
    }

    /**
     * Source sets that coverage should be reported for.
     */
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    public ConfigurableFileCollection getClassDirectories() {
        return classDirectories;
    }

    /**
     * Classes that coverage should be reported for.
     * @deprecated Use {@code getClassDirectories().setFrom(...)}
     */
    @Deprecated
    public void setClassDirectories(FileCollection classDirectories) {
        DeprecationLogger.nagUserOfDiscontinuedMethod("JacocoReportBase.setClassDirectories(FileCollection)", "Use getClassDirectories().from(...)");
        this.classDirectories.setFrom(classDirectories);
    }

    /**
     * Additional class dirs that coverage data should be reported for.
     */
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    public ConfigurableFileCollection getAdditionalClassDirs() {
        return additionalClassDirs;
    }

    /**
     * Additional class dirs that coverage data should be reported for.
     *
     * @deprecated Use {@code getAdditionalClassDirs().setFrom(...)}
     */
    @Deprecated
    public void setAdditionalClassDirs(@Nullable FileCollection additionalClassDirs) {
        DeprecationLogger.nagUserOfDiscontinuedMethod("JacocoReportBase.setAdditionalClassDirs(FileCollection)", "Use getAdditionalClassDirs().from(...)");
        if (additionalClassDirs!=null) {
            this.additionalClassDirs.setFrom(additionalClassDirs);
        } else {
            this.additionalClassDirs.setFrom();
        }
    }

    /**
     * Additional source dirs for the classes coverage data is being reported for.
     */
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    public ConfigurableFileCollection getAdditionalSourceDirs() {
        return additionalSourceDirs;
    }

    /**
     * Additional source dirs for the classes coverage data is being reported for.
     *
     * @deprecated Use {@code getAdditionalSourceDirs().setFrom(...)}
     */
    @Deprecated
    public void setAdditionalSourceDirs(@Nullable FileCollection additionalSourceDirs) {
        DeprecationLogger.nagUserOfDiscontinuedMethod("JacocoReportBase.setAdditionalSourceDirs(FileCollection)", "Use getAdditionalSourceDirs().from(...)");
        if (additionalSourceDirs!=null) {
            this.additionalSourceDirs.setFrom(additionalSourceDirs);
        } else {
            this.additionalSourceDirs.setFrom();
        }
    }

    /**
     * Adds execution data files to be used during coverage analysis.
     *
     * @param files one or more files to add
     */
    public void executionData(Object... files) {
        executionData.from(files);
    }

    /**
     * Adds execution data generated by a task to the list of those used during coverage analysis. Only tasks with a {@link JacocoTaskExtension} will be included; all others will be ignored.
     *
     * @param tasks one or more tasks to add
     */
    public void executionData(Task... tasks) {
        for (Task task : tasks) {
            final JacocoTaskExtension extension = task.getExtensions().findByType(JacocoTaskExtension.class);
            if (extension != null) {
                executionData(new Callable<File>() {
                    @Override
                    public File call() {
                        return extension.getDestinationFile();
                    }
                });
                mustRunAfter(task);
            }
        }
    }

    /**
     * Adds execution data generated by the given tasks to the list of those used during coverage analysis. Only tasks with a {@link JacocoTaskExtension} will be included; all others will be ignored.
     *
     * @param tasks one or more tasks to add
     */
    public void executionData(TaskCollection tasks) {
        tasks.all(new Action<Task>() {
            @Override
            public void execute(Task task) {
                executionData(task);
            }
        });
    }

    /**
     * Gets the class directories that coverage will be reported for. All classes in these directories will be included in the report.
     *
     * @return class dirs to report coverage of
     */
    @Internal
    public FileCollection getAllClassDirs() {
        return classDirectories.plus(getAdditionalClassDirs());
    }

    /**
     * Gets the source directories for the classes that will be reported on. Source will be obtained from these directories only for the classes included in the report.
     *
     * @return source directories for the classes reported on
     * @see #getAllClassDirs()
     */
    @Internal
    public FileCollection getAllSourceDirs() {
        return sourceDirectories.plus(getAdditionalSourceDirs());
    }

    /**
     * Adds a source set to the list to be reported on. The output of this source set will be used as classes to include in the report. The source for this source set will be used for any classes
     * included in the report.
     *
     * @param sourceSets one or more source sets to report on
     */
    public void sourceSets(final SourceSet... sourceSets) {
        for (final SourceSet sourceSet : sourceSets) {
            sourceDirectories.from(new Callable<Set<File>>() {
                @Override
                public Set<File> call() throws Exception {
                    return sourceSet.getAllJava().getSrcDirs();
                }
            });
            classDirectories.from(sourceSet.getOutput());
        }
    }

    /**
     * Adds additional class directories to those that will be included in the report.
     *
     * @param dirs one or more directories containing classes to report coverage of
     */
    public void additionalClassDirs(File... dirs) {
        additionalClassDirs(getProject().files(Arrays.asList(dirs)));
    }

    /**
     * Adds additional class directories to those that will be included in the report.
     *
     * @param dirs a {@code FileCollection} of directories containing classes to report coverage of
     */
    public void additionalClassDirs(FileCollection dirs) {
        additionalClassDirs.from(dirs);
    }

    /**
     * Adds additional source directories to be used for any classes included in the report.
     *
     * @param dirs one or more directories containing source files for the classes included in the report
     */
    public void additionalSourceDirs(File... dirs) {
        additionalSourceDirs(getProject().files(Arrays.asList(dirs)));
    }

    /**
     * Adds additional source directories to be used for any classes included in the report.
     *
     * @param dirs a {@code FileCollection} of directories containing source files for the classes included in the report
     */
    public void additionalSourceDirs(FileCollection dirs) {
        additionalSourceDirs.from(dirs);
    }
}
