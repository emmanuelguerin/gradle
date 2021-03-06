/*
 * Copyright 2017 the original author or authors.
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
package org.gradle.internal.component.external.model;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.attributes.DisambiguationRule;
import org.gradle.api.internal.attributes.EmptySchema;
import org.gradle.api.internal.attributes.MultipleCandidatesResult;
import org.gradle.api.internal.model.NamedObjectInstantiator;
import org.gradle.internal.Cast;

import java.util.Set;

/**
 * When no consumer attributes are provided, prefer the Java runtime variant over the API variant.
 *
 * Gradle has long assumed that, by default, consumers of a maven repository require the _runtime_ variant
 * of the published library.
 * The following disambiguation rule encodes this assumption for the case where a java library is published
 * with variants using Gradle module metadata. This will allow us to migrate to consuming the new module
 * metadata format by default without breaking a bunch of consumers that depend on this assumption,
 * declaring no preference for a particular variant.
 */
class PreferJavaRuntimeVariant extends EmptySchema {
    private static final NamedObjectInstantiator INSTANTIATOR = NamedObjectInstantiator.INSTANCE;
    private static final Usage JAVA_API = INSTANTIATOR.named(Usage.class, Usage.JAVA_API);
    private static final Usage JAVA_RUNTIME = INSTANTIATOR.named(Usage.class, Usage.JAVA_RUNTIME);
    private static final Set<Usage> DEFAULT_JAVA_USAGES = ImmutableSet.of(JAVA_API, JAVA_RUNTIME);

    private static final PreferJavaRuntimeVariant SCHEMA_DEFAULT_JAVA_VARIANTS = new PreferJavaRuntimeVariant();

    static PreferJavaRuntimeVariant schema() {
        return SCHEMA_DEFAULT_JAVA_VARIANTS;
    }

    private PreferJavaRuntimeVariant() {
    }

    @Override
    public DisambiguationRule<Object> disambiguationRules(Attribute<?> attribute) {
        if (attribute.getType().equals(Usage.class)) {
            return Cast.uncheckedCast(new DisambiguationRule<Usage>() {
                public void execute(MultipleCandidatesResult<Usage> details) {
                    if (details.getConsumerValue() == null) {
                        if (details.getCandidateValues().equals(DEFAULT_JAVA_USAGES)) {
                            details.closestMatch(JAVA_RUNTIME);
                        }
                    }
                }
            });
        }
        return super.disambiguationRules(attribute);
    }
}
