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

package io.novaordis.osstats;

import io.novaordis.events.core.event.Property;
import io.novaordis.osstats.metric.MetricDefinition;
import io.novaordis.osstats.metric.MetricSource;
import io.novaordis.osstats.metric.MockMetricDefinition;
import io.novaordis.osstats.metric.MockMetricSource;
import io.novaordis.osstats.os.MockOS;
import io.novaordis.utilities.os.OS;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataCollectorImplTest extends DataCollectorTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // establishSources() ----------------------------------------------------------------------------------------------

    @Test
    public void establishSources_AllDefinitionsHaveACommonSource() throws Exception {

        MockOS mos = new MockOS();

        MockMetricDefinition d = new MockMetricDefinition();

        MockMetricSource source = new MockMetricSource();
        assertTrue(d.addSource(mos, source));
        MockMetricSource source2 = new MockMetricSource();
        assertTrue(d.addSource(mos, source2));

        MockMetricDefinition d2 = new MockMetricDefinition();
        assertTrue(d2.addSource(mos, source2));

        MockMetricSource source3 = new MockMetricSource();
        assertTrue(d2.addSource(mos, source3));

        List<MetricDefinition> metrics = new ArrayList<>(Arrays.asList(d, d2));
        Set<MetricSource> sources = DataCollectorImpl.establishSources(metrics, mos);

        assertEquals(1, sources.size());
        MetricSource s = sources.iterator().next();
        assertEquals(s, source2);
    }

    @Test
    public void establishSources_NoCommonSource() throws Exception {

        MockOS mos = new MockOS();

        MockMetricDefinition d = new MockMetricDefinition();

        MockMetricSource source = new MockMetricSource();
        assertTrue(d.addSource(mos, source));
        MockMetricSource source2 = new MockMetricSource();
        assertTrue(d.addSource(mos, source2));

        MockMetricDefinition d2 = new MockMetricDefinition();
        MockMetricSource source3 = new MockMetricSource();
        assertTrue(d2.addSource(mos, source3));
        MockMetricSource source4 = new MockMetricSource();
        assertTrue(d2.addSource(mos, source4));

        List<MetricDefinition> metrics = new ArrayList<>(Arrays.asList(d, d2));
        Set<MetricSource> sources = DataCollectorImpl.establishSources(metrics, mos);

        assertEquals(2, sources.size());
        assertTrue(sources.contains(source));
        assertTrue(sources.contains(source3));
    }

    // readMetrics() ---------------------------------------------------------------------------------------------------

    @Test
    public void readMetrics() throws Exception {

        MockOS mos = new MockOS();

        DataCollectorImpl dc = new DataCollectorImpl(mos);

        MockMetricDefinition mmd = new MockMetricDefinition();

        List<Property> properties = dc.readMetrics(Collections.singletonList(mmd));

        assertFalse(properties.isEmpty());
    }

    @Test
    public void readMetrics_NativeCallThrowsNativeExecutionException() throws Exception {

        MockOS mos = new MockOS();

        //
        // configure MockOS to throw native execution exception on any command
        //

        mos.breakOnAnyCommand("SYNTHETIC NativeExecutionException message", new RuntimeException("SYNTHETIC RUNTIME"));

        DataCollectorImpl dc = new DataCollectorImpl(mos);

        List<Property> properties = dc.readMetrics(null);

        assertTrue(properties.isEmpty());
    }

    @Test
    public void readMetrics_NativeCallFailsWithNonZero() throws Exception {

        MockOS mos = new MockOS();

        //
        // configure MockOS to fail on any command
        //

        mos.failOnAnyCommand("SYNTHETIC stderr CONTENT", "SYNTHETIC stdout CONTENT");

        DataCollectorImpl dc = new DataCollectorImpl(mos);

        List<Property> properties = dc.readMetrics(null);

        assertTrue(properties.isEmpty());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected DataCollectorImpl getDataCollectorToTest(OS os) throws Exception {

        return new DataCollectorImpl(os);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
