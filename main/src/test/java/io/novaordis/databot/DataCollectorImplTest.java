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

package io.novaordis.databot;

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
import static org.junit.Assert.assertNull;
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

//    @Test
//    public void establishSources_AllDefinitionsHaveACommonSource() throws Exception {
//
//        MockOS mos = new MockOS();
//
//        MockMetricDefinition d = new MockMetricDefinition();
//
//        MockMetricSource source = new MockMetricSource();
//        assertTrue(d.addSource(mos.getName(), source));
//        MockMetricSource source2 = new MockMetricSource();
//        assertTrue(d.addSource(mos.getName(), source2));
//
//        MockMetricDefinition d2 = new MockMetricDefinition();
//        assertTrue(d2.addSource(mos.getName(), source2));
//
//        MockMetricSource source3 = new MockMetricSource();
//        assertTrue(d2.addSource(mos.getName(), source3));
//
//        List<MetricDefinition> metrics = new ArrayList<>(Arrays.asList(d, d2));
//        Set<MetricSource> sources = DataCollectorImpl.establishSources(metrics, mos.getName());
//
//        assertEquals(1, sources.size());
//        MetricSource s = sources.iterator().next();
//        assertEquals(s, source2);
//    }
//
//    @Test
//    public void establishSources_NoCommonSource() throws Exception {
//
//        MockOS mos = new MockOS();
//
//        MockMetricDefinition d = new MockMetricDefinition();
//
//        MockMetricSource source = new MockMetricSource();
//        assertTrue(d.addSource(mos.getName(), source));
//        MockMetricSource source2 = new MockMetricSource();
//        assertTrue(d.addSource(mos.getName(), source2));
//
//        MockMetricDefinition d2 = new MockMetricDefinition();
//        MockMetricSource source3 = new MockMetricSource();
//        assertTrue(d2.addSource(mos.getName(), source3));
//        MockMetricSource source4 = new MockMetricSource();
//        assertTrue(d2.addSource(mos.getName(), source4));
//
//        List<MetricDefinition> metrics = new ArrayList<>(Arrays.asList(d, d2));
//        Set<MetricSource> sources = DataCollectorImpl.establishSources(metrics, mos.getName());
//
//        assertEquals(2, sources.size());
//        assertTrue(sources.contains(source));
//        assertTrue(sources.contains(source3));
//    }
//
//    @Test
//    public void establishSources_MetricHasNoSource() throws Exception {
//
//        MockOS mos = new MockOS();
//
//        MockMetricDefinition d = new MockMetricDefinition();
//
//        MockMetricSource source = new MockMetricSource();
//        assertTrue(d.addSource(mos.getName(), source));
//
//        // this metric has no source
//        MockMetricDefinition d2 = new MockMetricDefinition();
//
//        List<MetricDefinition> metrics = new ArrayList<>(Arrays.asList(d, d2));
//
//        try {
//            DataCollectorImpl.establishSources(metrics, mos.getName());
//            fail("should throw exception");
//        }
//        catch(DataCollectionException e) {
//            String msg = e.getMessage();
//            log.info(msg);
//            assertTrue(msg.contains("has no declared sources"));
//        }
//    }
//
//    @Test
//    public void establishSources_OneMetricThatHasNoSource() throws Exception {
//
//        MockOS mos = new MockOS();
//
//        MockMetricDefinition d = new MockMetricDefinition();
//
//        List<MetricDefinition> metrics = new ArrayList<>(Collections.singletonList(d));
//
//        try {
//            DataCollectorImpl.establishSources(metrics, mos.getName());
//            fail("should throw exception");
//        }
//        catch(DataCollectionException e) {
//            String msg = e.getMessage();
//            log.info(msg);
//            assertTrue(msg.contains("has no declared sources"));
//        }
//    }
//
//    // readMetrics() ---------------------------------------------------------------------------------------------------
//
//    @Test
//    public void readMetrics() throws Exception {
//
//        MockOS mos = new MockOS();
//
//        DataCollectorImpl dc = new DataCollectorImpl(mos);
//
//        MockMetricDefinition mmd = new MockMetricDefinition();
//        mmd.setName("TEST");
//
//        MockMetricSource mms = new MockMetricSource();
//
//        mmd.addSource(mos.getName(), mms);
//
//        MockProperty mp = new MockProperty();
//        mp.setName("TEST");
//
//        mms.addBulkReading(mos, mp);
//
//        List<Property> properties = dc.readMetrics(Collections.singletonList(mmd));
//
//        assertEquals(1, properties.size());
//
//        Property p = properties.get(0);
//        assertEquals(mp, p);
//    }
//
//    @Test
//    public void readMetrics_aMetricSourceBreaksOnCollect() throws Exception {
//
//        MockOS mos = new MockOS();
//
//        DataCollectorImpl dc = new DataCollectorImpl(mos);
//
//        MockMetricDefinition mmd = new MockMetricDefinition();
//        MockMetricSource mms = new MockMetricSource();
//        mmd.addSource(mos.getName(), mms);
//
//        mms.breakOnCollectMetrics();
//
//        try {
//            dc.readMetrics(Collections.singletonList(mmd));
//            fail("should throw exception");
//        }
//        catch(DataCollectionException e) {
//
//            assertNull(e.getMessage());
//
//            MetricCollectionException cause = (MetricCollectionException)e.getCause();
//
//            String msg = cause.getMessage();
//            log.info(msg);
//            assertEquals("SYNTHETIC", msg);
//        }
//    }
//
//    @Test
//    public void readMetrics_bulkCollectionReturnsNoMetrics() throws Exception {
//
//        MockOS mockOs = new MockOS();
//
//        DataCollectorImpl dc = getDataCollectorToTest(mockOs);
//
//
//        MockMetricDefinition md = new MockMetricDefinition();
//        MockMetricSource ms = new MockMetricSource();
//        md.addSource(mockOs.getName(), ms);
//
//        MockProperty mockProperty = new MockProperty("test");
//
//        ms.addReadingForMetric(md, mockProperty);
//
//        //
//        // make sure that bulk collection does not return anything
//        //
//
//        List<MetricSource> sources = md.getSources(mockOs.getName());
//        assertEquals(1, sources.size());
//        MetricSource source = sources.get(0);
//
//        List<Property> bulkCollection = source.collectMetrics(null);
//        assertTrue(bulkCollection.isEmpty());
//
//        List<Property> targetedCollection = dc.readMetrics(Collections.singletonList(md));
//
//        assertEquals(1, targetedCollection.size());
//        Property p = targetedCollection.get(0);
//        assertEquals(mockProperty, p);
//    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected DataCollectorImpl getDataCollectorToTest(OS os) throws Exception {

        return new DataCollectorImpl();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
