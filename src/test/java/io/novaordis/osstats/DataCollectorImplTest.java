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
import io.novaordis.osstats.metric.source.MetricSource;
import io.novaordis.osstats.metric.MockMetricDefinition;
import io.novaordis.osstats.metric.source.MockMetricSource;
import io.novaordis.osstats.os.MockOS;
import io.novaordis.utilities.os.OS;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataCollectorImplTest extends DataCollectorTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DataCollectorImplTest.class);

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
        assertTrue(d.addSource(mos.getName(), source));
        MockMetricSource source2 = new MockMetricSource();
        assertTrue(d.addSource(mos.getName(), source2));

        MockMetricDefinition d2 = new MockMetricDefinition();
        assertTrue(d2.addSource(mos.getName(), source2));

        MockMetricSource source3 = new MockMetricSource();
        assertTrue(d2.addSource(mos.getName(), source3));

        List<MetricDefinition> metrics = new ArrayList<>(Arrays.asList(d, d2));
        Set<MetricSource> sources = DataCollectorImpl.establishSources(metrics, mos.getName());

        assertEquals(1, sources.size());
        MetricSource s = sources.iterator().next();
        assertEquals(s, source2);
    }

    @Test
    public void establishSources_NoCommonSource() throws Exception {

        MockOS mos = new MockOS();

        MockMetricDefinition d = new MockMetricDefinition();

        MockMetricSource source = new MockMetricSource();
        assertTrue(d.addSource(mos.getName(), source));
        MockMetricSource source2 = new MockMetricSource();
        assertTrue(d.addSource(mos.getName(), source2));

        MockMetricDefinition d2 = new MockMetricDefinition();
        MockMetricSource source3 = new MockMetricSource();
        assertTrue(d2.addSource(mos.getName(), source3));
        MockMetricSource source4 = new MockMetricSource();
        assertTrue(d2.addSource(mos.getName(), source4));

        List<MetricDefinition> metrics = new ArrayList<>(Arrays.asList(d, d2));
        Set<MetricSource> sources = DataCollectorImpl.establishSources(metrics, mos.getName());

        assertEquals(2, sources.size());
        assertTrue(sources.contains(source));
        assertTrue(sources.contains(source3));
    }

    @Test
    public void establishSources_MetricHasNoSource() throws Exception {

        MockOS mos = new MockOS();

        MockMetricDefinition d = new MockMetricDefinition();

        MockMetricSource source = new MockMetricSource();
        assertTrue(d.addSource(mos.getName(), source));

        // this metric has no source
        MockMetricDefinition d2 = new MockMetricDefinition();

        List<MetricDefinition> metrics = new ArrayList<>(Arrays.asList(d, d2));

        try {
            DataCollectorImpl.establishSources(metrics, mos.getName());
            fail("should throw exception");
        }
        catch(DataCollectionException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.contains("has no declared sources"));
        }
    }

    // readMetrics() ---------------------------------------------------------------------------------------------------

    @Test
    public void readMetrics() throws Exception {

        MockOS mos = new MockOS();

        DataCollectorImpl dc = new DataCollectorImpl(mos);

        MockMetricDefinition mmd = new MockMetricDefinition();
        mmd.setName("TEST");

        MockMetricSource mms = new MockMetricSource();

        mmd.addSource(mos.getName(), mms);

        MockProperty mp = new MockProperty();
        mp.setName("TEST");

        mms.mockMetricGeneration(mos, mp);

        List<Property> properties = dc.readMetrics(Collections.singletonList(mmd));

        assertEquals(1, properties.size());

        Property p = properties.get(0);
        assertEquals(mp, p);
    }

    @Test
    public void readMetrics_aMetricSourceBreaksOnCollect() throws Exception {

        MockOS mos = new MockOS();

        DataCollectorImpl dc = new DataCollectorImpl(mos);

        MockMetricDefinition mmd = new MockMetricDefinition();
        MockMetricSource mms = new MockMetricSource();
        mmd.addSource(mos.getName(), mms);

        mms.breakOnCollectMetrics();

        try {
            dc.readMetrics(Collections.singletonList(mmd));
            fail("should throw exception");
        }
        catch(DataCollectionException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("SYNTHETIC", msg);
        }
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
