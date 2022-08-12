/*
 * Copyright (c) 2020 François Onimus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fonimus.ssh.shell.postprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fonimus.ssh.shell.postprocess.provided.JsonPointerPostProcessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonPointerPostProcessorTest {

    private static JsonPointerPostProcessor processor;

    @BeforeAll
    static void init() {
        processor = new JsonPointerPostProcessor(new ObjectMapper());
    }

    @Test
    void process() throws Exception {
        Health health = Health.down()
                .withDetail("test", "value")
                .withDetail("map", Collections.singletonMap("key", "map-value"))
                .withDetail("list", Collections.singletonList("item"))
                .build();
        String test = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(health);

        assertAll("json pointer",
                () -> assertEquals("not-a-json", processor.process("not-a-json", Collections.singletonList("/test"))),
                () -> assertEquals(test, processor.process(test, null)),
                () -> assertEquals(test, processor.process(test, Collections.singletonList(null))),
                () -> assertEquals(test, processor.process(test, Collections.singletonList(""))),
                () -> assertEquals("Invalid input: JSON Pointer expression must start with '/': \"test\"",
                        processor.process(test, Collections.singletonList("test"))),
                () -> assertEquals("No node found with json path expression: /not-existing",
                        processor.process(test, Collections.singletonList("/not-existing"))),
                () -> assertEqualsNoLineSeparator("{  \"test\" : \"value\",  \"map\" : {    \"key\" : \"map-value\"  " +
                                "},  \"list\" : [ \"item\" ]}",
                        processor.process(test, Collections.singletonList("/details"))),
                () -> assertEquals("value", processor.process(test, Collections.singletonList("/details/test"))),
                () -> assertEquals("[ \"item\" ]", processor.process(test, Collections.singletonList("/details/list"))),
                () -> assertEquals("item", processor.process(test, Collections.singletonList("/details/list/0"))),
                () -> assertEquals("No node found with json path expression: /details/list/1",
                        processor.process(test, Collections.singletonList("/details/list/1"))),
                () -> assertEqualsNoLineSeparator("{  \"key\" : \"map-value\"}", processor.process(test,
                        Collections.singletonList("/details/map"))),
                () -> assertEquals("map-value", processor.process(test, Collections.singletonList("/details/map/key"))),
                () -> assertEquals("map-value", processor.process(test, Arrays.asList("/details/map/key",
                        "dont-care"))),
                () -> assertEquals("No node found with json path expression: /details/map/not-a-key",
                        processor.process(test, Collections.singletonList("/details/map/not-a-key")))
        );

    }

    private static void assertEqualsNoLineSeparator(String expected, String actual) {
        assertEquals(clean(expected), clean(actual));
    }

    private static String clean(String toClean) {
        return toClean == null ? null : toClean.replaceAll("[\\r\\n]+", "");
    }
}
