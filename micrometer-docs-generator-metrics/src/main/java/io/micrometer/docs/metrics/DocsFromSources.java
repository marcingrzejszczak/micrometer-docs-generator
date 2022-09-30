/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micrometer.docs.metrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Pattern;

import io.micrometer.common.util.internal.logging.InternalLogger;
import io.micrometer.common.util.internal.logging.InternalLoggerFactory;
import io.micrometer.docs.commons.ObservationConventionEntry;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

// TODO: Assert on prefixes
public class DocsFromSources {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DocsFromSources.class);

    private final File projectRoot;

    private final Pattern inclusionPattern;

    private final File outputDir;

    public DocsFromSources(File projectRoot, Pattern inclusionPattern, File outputDir) {
        this.projectRoot = projectRoot;
        this.inclusionPattern = inclusionPattern;
        this.outputDir = outputDir;
    }

    public static void main(String... args) {
        String projectRoot = args[0];
        String inclusionPattern = args[1];
        inclusionPattern = inclusionPattern.replace("/", File.separator);
        String output = args[2];
        new DocsFromSources(new File(projectRoot), Pattern.compile(inclusionPattern), new File(output)).generate();
    }

    public void generate() {
        Path path = this.projectRoot.toPath();
        logger.debug("Path is [" + this.projectRoot.getAbsolutePath() + "]. Inclusion pattern is [" + this.inclusionPattern + "]");
        Collection<MetricEntry> entries = new TreeSet<>();
        Collection<ObservationConventionEntry> observationConventionEntries = new TreeSet<>();
        FileVisitor<Path> fv = new MetricSearchingFileVisitor(this.inclusionPattern, entries, observationConventionEntries);
        try {
            Files.walkFileTree(path, fv);
            MetricEntry.assertThatProperlyPrefixed(entries);
            printMetricsAdoc(entries);
            ObservationConventionEntry.saveEntriesAsAdocTableInAFile(observationConventionEntries, new File(this.outputDir, "_conventions.adoc"));
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void printMetricsAdoc(Collection<MetricEntry> entries) throws IOException {
        File file = new File(this.outputDir, "_metrics.adoc");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        logger.debug("Will create files under [" + file + "]");
        StringBuilder stringBuilder = new StringBuilder();
        Path output = file.toPath();
        logger.debug("======================================");
        logger.debug("Summary of sources analysis");
        logger.debug("Found [" + entries.size() + "] samples");
        logger.debug(
                "Found [" + entries.stream().flatMap(e -> e.lowCardinalityKeyNames.stream()).distinct().count() + "] low cardinality tags");
        logger.debug(
                "Found [" + entries.stream().flatMap(e -> e.highCardinalityKeyNames.stream()).distinct().count() + "] high cardinality tags");
        stringBuilder.append("[[observability-metrics]]\n=== Observability - Metrics\n\nBelow you can find a list of all samples declared by this project.\n\n");
        entries.forEach(metricEntry -> stringBuilder.append(metricEntry.toString()).append("\n\n"));

        //// tying thymeleaf - start
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCheckExistence(true);

        ClassLoaderTemplateResolver classLoaderTemplateResolver = new ClassLoaderTemplateResolver();
        classLoaderTemplateResolver.setTemplateMode(TemplateMode.TEXT);
        classLoaderTemplateResolver.setCheckExistence(true);

        Context context = new Context();
        context.setVariable("entries", entries);

        String template = "templates/metrics.adoc";

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addTemplateResolver(classLoaderTemplateResolver);
        String result = templateEngine.process(template, context);

        Files.write(new File(this.outputDir, "_metrics2.adoc").toPath(), result.getBytes());
        Files.write(new File(this.outputDir, "_metrics-org.adoc").toPath(), stringBuilder.toString().getBytes());

        Files.write(output, result.getBytes());
        //// tying thymeleaf - end

//        Files.write(output, stringBuilder.toString().getBytes());
    }
}
