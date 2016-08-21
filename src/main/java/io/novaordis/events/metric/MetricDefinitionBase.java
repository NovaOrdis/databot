/*
 * Copyright (c) 2016 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.events.metric;

import io.novaordis.events.core.event.MeasureUnit;
import io.novaordis.events.metric.source.MetricSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Not thread-safe, access synchronization must be implemented externally.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public abstract class MetricDefinitionBase implements MetricDefinition {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // <String (os name), List<MetricSource>>
    private Map<String, List<MetricSource>> sources;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MetricDefinitionBase() {
        this.sources = new HashMap<>();
    }

    // MetricDefinition implementation ---------------------------------------------------------------------------------

    @Override
    public String getName() {

        return getClass().getSimpleName();
    }

    @Override
    public final String getLabel() {

        MeasureUnit mu = getMeasureUnit();
        String simpleLabel = getSimpleLabel();

        if (mu == null) {
            return simpleLabel;
        }

        return simpleLabel + " (" + mu.getLabel() + ")";
    }

    /**
     * The implementation returns a copy of the internal list.
     */
    @Override
    public List<MetricSource> getSources(String  osName) {

        if (osName == null) {
            throw new IllegalArgumentException("null OS name");
        }

        List<MetricSource> sl = sources.get(osName);

        if (sl == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(sl);
    }

    /**
     * Not thread safe.
     */
    @Override
    public boolean addSource(String osName, MetricSource source) {

        List<MetricSource> sl = sources.get(osName);

        if (sl == null) {

            sl = new ArrayList<>();
            sources.put(osName, sl);
        }

        if (sl.contains(source)) {
            return false;
        }

        sl.add(source);

        return true;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return getName();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
