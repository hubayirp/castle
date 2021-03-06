/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.castle.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.confluent.castle.common.JsonUtil.JSON_SERDE;
import static org.junit.Assert.assertEquals;

public class StringExpanderTest {
    @Rule
    final public Timeout globalTimeout = Timeout.millis(120000);

    private final static FixedMapStringExpander EXPANDER =
        new FixedMapStringExpander(new HashMap<String, String>() {{
                put("foo", "bar");
                put("baz", "quux");
                put("blah", "");
            }});

    @Test
    public void testTransformString() throws Exception {
        assertEquals("", EXPANDER.expand(""));
        assertEquals("foo", EXPANDER.expand("foo"));
        assertEquals("bar", EXPANDER.expand("%{foo}"));
        assertEquals("%foo", EXPANDER.expand("%foo"));
        assertEquals("%{foo}", EXPANDER.expand("\\%{foo}"));
        assertEquals("barquux", EXPANDER.expand("%{foo}%{baz}%{blah}"));
        assertEquals("", EXPANDER.expand("%{blah}"));
        assertEquals("", EXPANDER.expand("%{foo"));
    }

    private static final class TestNestedJsonObject {
        private final String foo;
        private final int bar;

        @JsonCreator
        public TestNestedJsonObject(@JsonProperty("foo") String foo,
                                    @JsonProperty("bar") int bar) {
            this.foo = foo;
            this.bar = bar;
        }

        @JsonProperty
        public String foo() {
            return foo;
        }

        @JsonProperty
        public int bar() {
            return bar;
        }
    }

    private static final class TestJsonObject {
        private final TestNestedJsonObject[] foos;
        private final long quux;

        @JsonCreator
        public TestJsonObject(@JsonProperty("foos") TestNestedJsonObject[] foos,
                              @JsonProperty("quux") long quux) {
            this.foos = foos;
            this.quux = quux;
        }

        @JsonProperty
        public TestNestedJsonObject[] foos() {
            return foos;
        }

        @JsonProperty
        public long quux() {
            return quux;
        }
    }

    @Test
    public void testExpandJson() throws Exception {
        TestNestedJsonObject[] foos = new TestNestedJsonObject[] {
            new TestNestedJsonObject("%{foo}", 123),
            new TestNestedJsonObject("%foo", 456)
        };
        TestJsonObject inputObject = new TestJsonObject(foos, 123456);
        JsonNode inputNode = JSON_SERDE.valueToTree(inputObject);
        JsonNode outputNode = EXPANDER.expand(inputNode);
        TestJsonObject outputObject = JSON_SERDE.treeToValue(outputNode, TestJsonObject.class);
        assertEquals(inputObject.quux, outputObject.quux);
        assertEquals("bar", outputObject.foos[0].foo);
        assertEquals(inputObject.foos[0].bar, outputObject.foos[0].bar);
        assertEquals(inputObject.foos[1].foo, outputObject.foos[1].foo);
        assertEquals(inputObject.foos[1].bar, outputObject.foos[1].bar);
    }

    @Test
    public void testExpandMap() throws Exception {
        HashMap<String, String> inputMap = new HashMap<>();
        inputMap.put("foo", "%{foo}");
        inputMap.put("foo2", "%{baz}");
        inputMap.put("%{foo}", "barValue");
        inputMap.put("abc", "123");
        Map<String, String> outputMap = EXPANDER.expand(inputMap);
        assertEquals("bar", outputMap.get("foo"));
        assertEquals("quux", outputMap.get("foo2"));
        assertEquals("barValue", outputMap.get("bar"));
        assertEquals("123", outputMap.get("abc"));
        assertEquals(4, outputMap.size());
    }
};
