/*
 * Copyright (c) 2017 Nova Ordis LLC
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

import io.novaordis.databot.configuration.Configuration;
import io.novaordis.databot.configuration.DefaultConfiguration;
import io.novaordis.databot.configuration.MockConfiguration;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.events.api.metric.MetricSourceRepository;
import io.novaordis.events.api.metric.MetricSourceRepositoryImpl;
import io.novaordis.events.api.metric.jboss.JBossController;
import io.novaordis.events.api.metric.jmx.JmxBus;
import io.novaordis.events.api.metric.os.LocalOS;
import io.novaordis.events.api.metric.os.RemoteOS;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/11/17
 */
public class DataBotTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor_NullConfiguration() throws Exception {

        try {

            new DataBot(null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertEquals("null configuration", msg);
        }
    }

    @Test
    public void constructorAndInitialization_Defaults() throws Exception {

        Configuration c = new DefaultConfiguration();

        //
        // construct and initialize
        //

        DataBot d = new DataBot(c);

        assertEquals(c.getEventQueueSize(), d.getEventQueueSize());
        assertEquals(0, d.getEventCount());

        assertFalse(d.isStarted());

        List<MetricSource> sources = d.getMetricSources();
        assertTrue(sources.isEmpty());

        List<DataConsumer> consumers = d.getDataConsumers();
        assertTrue(consumers.isEmpty());

        BlockingQueue<Event> eventQueue = d.getEventQueue();
        assertEquals(c.getEventQueueSize(), eventQueue.remainingCapacity());

    }

    @Test
    public void constructorAndInitialization_ComplexSimulation() throws Exception {

        int eventQueueSize = 7;

        MetricSourceRepository r = new MetricSourceRepositoryImpl();

        r.add(new LocalOS());
        r.add(new RemoteOS("ssh://mock-remote-ssh-server/"));
        r.add(new JmxBus("jmx://mock-remote-jmx-bus/"));
        r.add(new JBossController("jbosscli://mock-remote-jboss-controller/"));

        MockDataConsumer mdc = new MockDataConsumer();
        MockDataConsumer mdc2 = new MockDataConsumer();
        List<DataConsumer> dataConsumers = new ArrayList<>();
        dataConsumers.add(mdc);
        dataConsumers.add(mdc2);

        MockConfiguration mc = new MockConfiguration();
        mc.setEventQueueSize(eventQueueSize);
        mc.setMetricSourceRepository(r);
        mc.setDataConsumers(dataConsumers);

        //
        // construct and initialize
        //

        DataBot d = new DataBot(mc);

        assertEquals(eventQueueSize, d.getEventQueueSize());
        assertEquals(0, d.getEventCount());

        assertFalse(d.isStarted());

        List<MetricSource> sources = d.getMetricSources();

        assertEquals(4, sources.size());

        MetricSource expected;

        expected = new LocalOS();
        assertTrue(sources.contains(expected));

        expected = new RemoteOS("ssh://mock-remote-ssh-server/");
        assertTrue(sources.contains(expected));

        expected = new JmxBus("jmx://mock-remote-jmx-bus/");
        assertTrue(sources.contains(expected));

        expected = new JBossController("jbosscli://mock-remote-jboss-controller/");
        assertTrue(sources.contains(expected));

        List<DataConsumer> consumers = d.getDataConsumers();

        assertEquals(2, consumers.size());

        assertEquals(mdc, consumers.get(0));
        assertFalse(mdc.isStarted());
        assertEquals(mdc2, consumers.get(1));
        assertFalse(mdc2.isStarted());
    }

    // lifecycle -------------------------------------------------------------------------------------------------------

    @Test
    public void lifecycle() throws Exception {

        MockMetricSource ms = new MockMetricSource();
        MockDataConsumer mdc = new MockDataConsumer();

        MetricSourceRepository r = new MetricSourceRepositoryImpl();
        r.add(ms);

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceRepository(r);
        mc.setDataConsumers(Collections.singletonList(mdc));
        mc.setSamplingIntervalSec(1);

        DataBot d = new DataBot(mc);

        assertFalse(d.isStarted());

        List<DataConsumer> consumers = d.getDataConsumers();
        assertEquals(1, consumers.size());
        assertEquals(mdc, consumers.get(0));
        assertFalse(consumers.get(0).isStarted());
        assertEquals(0, d.getTimerTaskExecutionCount());

        d.start();

        assertTrue(d.isStarted());

        List<MetricSource> sources = d.getMetricSources();

        //
        // sources are not started at this time, they will be started on the next timer run
        //

        for(MetricSource s: sources) {

            assertFalse(s.isStarted());
        }

        consumers = d.getDataConsumers();

        for(DataConsumer c: consumers) {

            assertTrue(c.isStarted());
        }

        //
        // wait twice the sampling interval to make sure that the timer task is scheduled as planned
        //

        long waitTimeMs = 1000L * mc.getSamplingIntervalSec();
        long t0 = System.currentTimeMillis();

        while(System.currentTimeMillis() - t0 < waitTimeMs) {

            Thread.sleep(200L);
        }

        long timerTaskExecutionCount = d.getTimerTaskExecutionCount();
        assertTrue(timerTaskExecutionCount > 0);

        d.stop();

        //
        // make sure all is stopped
        //

        //
        // wait twice the sampling interval to make sure that the timer task has time to shut down
        //

        waitTimeMs = 1000L * mc.getSamplingIntervalSec();
        t0 = System.currentTimeMillis();

        while(System.currentTimeMillis() - t0 < waitTimeMs) {

            Thread.sleep(200L);
        }
    }

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

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
