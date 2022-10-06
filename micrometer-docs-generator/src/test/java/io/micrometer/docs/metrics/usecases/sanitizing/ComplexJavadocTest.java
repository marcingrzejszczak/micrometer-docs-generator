/*
 * Copyright 2022 the original author or authors.
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

package io.micrometer.docs.metrics.usecases.sanitizing;


import io.micrometer.docs.metrics.MetricsDocGenerator;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

class ComplexJavadocTest {

    @Test
    void should_sanitize_meter_and_tag_javadocs() {
        File sourceRoot = new File("src/test/java/io/micrometer/docs/metrics/usecases/sanitizing");
        File output = new File( "./build");

        new MetricsDocGenerator(sourceRoot, Pattern.compile(".*"), output).generate();

        BDDAssertions.then(new File(output, "_metrics.adoc"))
                .hasSameTextualContentAs(new File(getClass().getResource("/expected-sanitizing.adoc").getFile()));
    }
}
