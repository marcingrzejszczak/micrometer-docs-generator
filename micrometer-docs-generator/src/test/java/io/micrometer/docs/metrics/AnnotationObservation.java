/*
 * Copyright 2013-2021 the original author or authors.
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

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.docs.ObservationDocumentation;

;

enum AnnotationObservation implements ObservationDocumentation {

    /**
     * Observation that wraps annotations.
     */
    ANNOTATION_NEW_OR_CONTINUE {
        @Override
        public String getName() {
            return "%s";
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return Tags.values();
        }

        @Override
        public KeyName[] getHighCardinalityKeyNames() {
            return Tags2.values();
        }

    };

    /**
     * Low cardinality tags.
     */
    enum Tags implements KeyName {

        /**
         * Class name where a method got annotated.
         */
        CLASS {
            @Override
            public String asString() {
                return "class";
            }
        },

        /**
         * Method name that got annotated with annotation.
         */
        METHOD {
            @Override
            public String asString() {
                return "method";
            }
        }

    }

    /**
     * High cardinality tags.
     */
    enum Tags2 implements KeyName {

        /**
         * Class name where a method got annotated.
         */
        CLASS2 {
            @Override
            public String asString() {
                return "class2";
            }
        },

        /**
         * Method name that got annotated.
         */
        METHOD2 {
            @Override
            public String asString() {
                return "method2";
            }
        }

    }

}
