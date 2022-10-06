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

package io.micrometer.docs.conventions;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import io.micrometer.common.util.internal.logging.InternalLogger;
import io.micrometer.common.util.internal.logging.InternalLoggerFactory;
import io.micrometer.docs.conventions.ObservationConventionEntry.Type;
import io.micrometer.docs.commons.templates.HandlebarsUtils;

public class ObservationConventionsDocGenerator {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ObservationConventionsDocGenerator.class);

    private final File projectRoot;

    private final Pattern inclusionPattern;

    private final File outputDir;

    public ObservationConventionsDocGenerator(File projectRoot, Pattern inclusionPattern, File outputDir) {
        this.projectRoot = projectRoot;
        this.inclusionPattern = inclusionPattern;
        this.outputDir = outputDir;
    }

    public void generate() {
        Path path = this.projectRoot.toPath();
        logger.debug("Path is [" + this.projectRoot.getAbsolutePath() + "]. Inclusion pattern is [" + this.inclusionPattern + "]");
        TreeSet<ObservationConventionEntry> observationConventionEntries = new TreeSet<>();
        FileVisitor<Path> fv = new ObservationConventionSearchingFileVisitor(this.inclusionPattern, observationConventionEntries);
        try {
            Files.walkFileTree(path, fv);
            printObservationConventionsAdoc(observationConventionEntries);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void printObservationConventionsAdoc(TreeSet<ObservationConventionEntry> entries) throws IOException {
        List<ObservationConventionEntry> globals = entries.stream().filter(e -> e.getType() == Type.GLOBAL).collect(Collectors.toList());
        List<ObservationConventionEntry> locals = entries.stream().filter(e -> e.getType() == Type.LOCAL).collect(Collectors.toList());

        String location = "templates/conventions.adoc.hbs";
        Handlebars handlebars = HandlebarsUtils.createHandlebars();
        Template template = handlebars.compile(location);

        Map<String, Object> map = new HashMap<>();
        map.put("globals", globals);
        map.put("locals", locals);
        String result = template.apply(map);

        Path output = new File(this.outputDir, "_conventions.adoc").toPath();
        Files.write(output, result.getBytes());
    }

}
