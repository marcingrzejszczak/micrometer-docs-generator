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

package io.micrometer.docs.spans.test3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

import io.micrometer.docs.spans.SpansDocGenerator;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

class TagsFromParentWithOverridingDocsFromSourcesTests {

    @Test
    void should_append_tag_keys_to_parent_ones() throws IOException {
        File root = new File("./src/test/java/io/micrometer/docs/spans/test3");
        File output = new File(".", "build/test3");
        output.mkdirs();

        new SpansDocGenerator(root, Pattern.compile(".*"), output).generate();

        BDDAssertions.then(new String(Files.readAllBytes(new File(output, "_spans.adoc").toPath())))
                .doesNotContain("==== Parent Span") // this should be overridden
                .contains("==== Should Append Additional Tag Keys To Parent Sample Span").contains("> Span.")
                .contains("|`class`|Class name where a method got annotated with a annotation.")
                .contains("|`class2`|Class name where a method got annotated with a annotation.")
                .contains("|`foooooo`|Test foo")
                .contains("|`method`|Method name that got annotated with annotation.")
                .contains("|`method2`|Method name that got annotated with annotation.");
    }

}
