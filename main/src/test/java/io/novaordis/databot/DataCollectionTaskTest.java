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

import io.novaordis.databot.configuration.MockConfiguration;
import io.novaordis.databot.consumer.MockActiveDataConsumer;
import io.novaordis.databot.event.MultiSourceReadingEvent;
import io.novaordis.databot.failure.EventQueueFullException;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.EventProperty;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.PropertyFactory;
import io.novaordis.events.api.event.TimedEvent;
import io.novaordis.events.api.event.TimestampProperty;
import io.novaordis.events.api.metric.MockAddress;
import io.novaordis.utilities.address.Address;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataCollectionTaskTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // identity --------------------------------------------------------------------------------------------------------

    @Test
    public void identity() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        DataBot db = new DataBot(mc);

        DataCollectionTask t = new DataCollectionTask(db);

        assertNull(t.getMaxExecutions());
    }

    // run() -----------------------------------------------------------------------------------------------------------

    @Test
    public void run_DataCollectionEncountersUnexpectedFailure_RunMustContinue() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        DataBot db = new DataBot(mc);

        DataCollectionTask t = new DataCollectionTask(db);

        assertEquals(0L, t.getExecutionCount());
        assertEquals(0L, t.getSuccessfulExecutionCount());

        //
        // trigger an NPE
        //

        t.setDataBot(null);

        assertNull(t.getDataBot());

        t.run();

        assertEquals(1L, t.getExecutionCount());
        assertEquals(0L, t.getSuccessfulExecutionCount());

        Throwable c = t.getCauseOfLastFailure();
        assertTrue(c instanceof NullPointerException);
    }

    @Test
    public void run_DataCollectionFailsBecauseTheInternalQueueIsFull_RunMustContinue() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        //
        // this queue will only accept one event then block
        //
        mc.setEventQueueSize(1);

        DataBot db = new DataBot(mc);

        DataCollectionTask t = new DataCollectionTask(db);

        assertEquals(0L, t.getExecutionCount());
        assertEquals(0L, t.getSuccessfulExecutionCount());

        //
        // fill the queue
        //

        t.run();

        assertEquals(1L, t.getExecutionCount());
        assertEquals(1L, t.getSuccessfulExecutionCount());
        assertNull(t.getCauseOfLastFailure());

        //
        // queue is full
        //

        t.run();

        assertEquals(2L, t.getExecutionCount());
        assertEquals(1L, t.getSuccessfulExecutionCount());
        EventQueueFullException cause = (EventQueueFullException)t.getCauseOfLastFailure();
        assertNotNull(cause);
    }

    @Test
    public void run_DataCollectionEncountersNoMetricsProducesEmptyTimedEvents() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        //
        // plug in a consumer
        //

        MockActiveDataConsumer mdc = new MockActiveDataConsumer();
        mc.setDataConsumers(Collections.singletonList(mdc));

        DataBot db = new DataBot(mc);

        assertFalse(db.isStarted());

        //
        // because we take the "control" away from the databot and we don't start it, allowing it to start its internal
        // thread and drive the collection, we need to start the data collector externally
        //

        mdc.start();

        //
        // drive the data collection externally, by simulating the internal DataBot thread
        //

        DataCollectionTask t = db.getDataCollectionTimerTask();

        assertEquals(0L, t.getExecutionCount());
        assertEquals(0L, t.getSuccessfulExecutionCount());

        //
        // this is a data run
        //

        t.run();

        assertEquals(1L, t.getExecutionCount());
        assertEquals(1L, t.getSuccessfulExecutionCount());
        assertNull(t.getCauseOfLastFailure());

        //
        // the event will eventually reach the consumer
        //

        Event event;
        int waitTimeSecs = 5;
        long t0 = System.currentTimeMillis();
        while((event = mdc.getEvent()) == null) {

            if (System.currentTimeMillis() - t0 > waitTimeSecs) {

                fail("we waited for more than " + waitTimeSecs + " secs for the event to propagate to consumer");

            }

            Thread.sleep(200L);
        }

        assertNotNull(event);

        List<Property> properties = event.getProperties();

        //
        // the only property is the timestamp
        //

        assertEquals(1, properties.size());
        TimestampProperty p = (TimestampProperty)properties.get(0);
        assertNotNull(p);
    }

    // dataCollectionRun() ---------------------------------------------------------------------------------------------

    @Test
    public void dataCollectionRun() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        DataBot db = new DataBot(mc);
        DataCollectionTask t = db.getDataCollectionTimerTask();

        t.dataCollectionRun();

        //
        // no metric, no properties, expecting an empty event on the queue
        //

        BlockingQueue<Event> eventQueue = db.getEventQueue();

        long waitForEventSeconds = 3L;

        Event event = eventQueue.poll(waitForEventSeconds, TimeUnit.SECONDS);

        if (event == null) {

            fail("failed to receive event on the event queue");
        }

        List<Property> properties = event.getProperties();

        //
        // the only property is the timestamp
        //

        assertEquals(1, properties.size());
        TimestampProperty p = (TimestampProperty)properties.get(0);
        assertNotNull(p);
    }

    @Test
    public void dataCollectionRun_QueueFull() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.setEventQueueSize(1);

        DataBot db = new DataBot(mc);
        DataCollectionTask t = db.getDataCollectionTimerTask();

        //
        // pre-fill the queue
        //

        assertTrue(db.getEventQueue().offer(new MockEvent()));

        try {

            t.dataCollectionRun();
            fail("should have thrown exception");
        }
        catch(EventQueueFullException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("full"));
        }
    }

    // collectMetrics() ------------------------------------------------------------------------------------------------

    @Test
    public void collectMetrics_NoSources() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        DataBot db = new DataBot(mc);
        DataCollectionTask t = db.getDataCollectionTimerTask();

        long t0 = System.currentTimeMillis();

        TimedEvent e = t.collectMetrics();

        long t1 = System.currentTimeMillis();

        assertNotNull(e);

        assertTrue(t0 <= e.getTime());
        assertTrue(e.getTime() <= t1);

        //
        // getProperties() carries the timestamp property
        //

        assertEquals(1, e.getProperties().size());
        TimestampProperty p = (TimestampProperty)e.getProperties().get(0);
        assertEquals(e.getTime().longValue(), ((Long)p.getValue()).longValue());
    }

    @Test
    public void collectMetrics_OneSource_OneMetricDefinition_CollectionSucceeds() throws Exception {

        PropertyFactory pf = new PropertyFactory();

        Address ma = new MockAddress("mock-metric-source");
        MockMetricDefinition mmd = new MockMetricDefinition(pf, ma, "mock-metric-id");
        MockMetricSourceFactory mmsf = new MockMetricSourceFactory(new PropertyFactory());

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceFactory(mmsf);
        mc.addMetricDefinition(mmd);

        DataBot db = new DataBot(mc);

        //
        // configure the mock source with "expected" values
        //

        MockMetricSource mms = (MockMetricSource)db.getMetricSource(ma);
        mms.addReadingForMetric("mock-metric-id", "mock-value");

        DataCollectionTask t = db.getDataCollectionTimerTask();

        long t0 = System.currentTimeMillis();

        TimedEvent e = t.collectMetrics();

        long t1 = System.currentTimeMillis();

        assertNotNull(e);

        assertTrue(t0 <= e.getTime());
        assertTrue(e.getTime() <= t1);

        //
        // we don't change the semantics of getProperties(), it will return the top level properties
        //

        List<Property> properties = e.getProperties();
        assertEquals(2, properties.size());

        TimestampProperty p = (TimestampProperty)e.getProperties().get(0);
        assertEquals(e.getTime().longValue(), ((Long) p.getValue()).longValue());

        EventProperty p2 = (EventProperty)properties.get(1);

        assertEquals(ma.getLiteral(), p2.getName());

        Event secondLevelEvent = p2.getEvent();

        List<Property> secondLevelProperties = secondLevelEvent.getProperties();
        assertEquals(1, secondLevelProperties.size());

        Property p3 = secondLevelProperties.get(0);

        // the property name must be the metric definition ID
        assertEquals("mock-metric-id", p3.getName());
        assertEquals("mock-value", p3.getValue());
    }

    @Test
    public void collectMetrics_OneSource_OneMetricDefinition_CollectionFailsWithCheckedException() throws Exception {

        PropertyFactory pf = new PropertyFactory();

        Address ma = new MockAddress("mock-metric-source");
        MockMetricDefinition mmd = new MockMetricDefinition(pf, ma, "mock-metric-id");
        MockMetricSourceFactory mmsf = new MockMetricSourceFactory(new PropertyFactory());

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceFactory(mmsf);
        mc.addMetricDefinition(mmd);

        DataBot db = new DataBot(mc);

        //
        // configure the mock source to fail with checked exception
        //

        MockMetricSource mms = (MockMetricSource)db.getMetricSource(ma);
        mms.breakOnCollectWithMetricSourceException("SYNTHETIC CHECKED");

        DataCollectionTask t = db.getDataCollectionTimerTask();

        long t0 = System.currentTimeMillis();

        TimedEvent e = t.collectMetrics();

        long t1 = System.currentTimeMillis();

        assertNotNull(e);

        assertTrue(t0 <= e.getTime());
        assertTrue(e.getTime() <= t1);

        List<Property> properties = e.getProperties();

        //
        // getProperties() has the original semantics, there's a EventProperty corresponding to the collection
        //

        assertEquals(2, properties.size());

        TimestampProperty p = (TimestampProperty)e.getProperties().get(0);
        assertEquals(e.getTime().longValue(), ((Long) p.getValue()).longValue());

        assertTrue(((EventProperty) properties.get(1)).getEvent().getProperties().isEmpty());

        //
        // special semantics
        //

        MultiSourceReadingEvent msre = (MultiSourceReadingEvent)e;

        //
        // make sure the address was recorded, though
        //

        List<Address> addresses = msre.getSourceAddresses();
        assertEquals(1, addresses.size());
        assertEquals(ma, addresses.get(0));
        assertEquals(0, msre.getAllPropertiesCount());
        assertTrue(msre.getPropertiesForSource(ma).isEmpty());
    }

    @Test
    public void collectMetrics_OneSource_OneMetricDefinition_CollectionFailsWithUncheckedException() throws Exception {

        PropertyFactory pf = new PropertyFactory();

        Address ma = new MockAddress("mock-metric-source");
        MockMetricDefinition mmd = new MockMetricDefinition(pf, ma, "mock-metric-id");
        MockMetricSourceFactory mmsf = new MockMetricSourceFactory(new PropertyFactory());

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceFactory(mmsf);
        mc.addMetricDefinition(mmd);

        DataBot db = new DataBot(mc);

        //
        // configure the mock source to fail with unchecked exception - this means the source will generate zero
        // readings, but nothing should break
        //

        MockMetricSource mms = (MockMetricSource)db.getMetricSource(ma);
        mms.breakOnCollectWithUncheckedException("SYNTHETIC UNCHECKED");

        DataCollectionTask t = db.getDataCollectionTimerTask();

        long t0 = System.currentTimeMillis();

        TimedEvent e = t.collectMetrics();

        long t1 = System.currentTimeMillis();

        assertNotNull(e);

        assertTrue(t0 <= e.getTime());
        assertTrue(e.getTime() <= t1);

        List<Property> properties = e.getProperties();

        //
        // getProperties() has the original semantics, there's a TimestampProperty and an EventProperty corresponding
        // to the collection
        //

        assertEquals(2, properties.size());
        TimestampProperty p = (TimestampProperty)e.getProperties().get(0);
        assertEquals(e.getTime().longValue(), ((Long)p.getValue()).longValue());
        assertTrue(((EventProperty) properties.get(1)).getEvent().getProperties().isEmpty());

        //
        // special semantics
        //

        MultiSourceReadingEvent msre = (MultiSourceReadingEvent)e;

        //
        // make sure the address was recorded, though
        //

        List<Address> addresses = msre.getSourceAddresses();
        assertEquals(1, addresses.size());
        assertEquals(ma, addresses.get(0));
        assertEquals(0, msre.getAllPropertiesCount());
        assertTrue(msre.getPropertiesForSource(ma).isEmpty());
    }

    @Test
    public void collectMetrics_TwoSources_OneSharedMetricDefinition_CollectionSucceeds() throws Exception {

        PropertyFactory pf = new PropertyFactory();

        Address ma = new MockAddress("mock-metric-source-1");
        MockMetricDefinition mmd = new MockMetricDefinition(pf, ma, "shared-mock-metric-id");

        Address ma2 = new MockAddress("mock-metric-source-2");
        MockMetricDefinition mmd2 = new MockMetricDefinition(pf, ma2, "shared-mock-metric-id");

        //
        // the metric definition is shared among two distinct sources
        //

        MockMetricSourceFactory mmsf = new MockMetricSourceFactory(new PropertyFactory());

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceFactory(mmsf);
        mc.addMetricDefinition(mmd);
        mc.addMetricDefinition(mmd2);

        DataBot db = new DataBot(mc);

        //
        // configure the mock source with "expected" values
        //

        MockMetricSource mms = (MockMetricSource)db.getMetricSource(ma);
        mms.addReadingForMetric("shared-mock-metric-id", "mock-value-for-source-1");

        MockMetricSource mms2 = (MockMetricSource)db.getMetricSource(ma2);
        mms2.addReadingForMetric("shared-mock-metric-id", "mock-value-for-source-2");

        DataCollectionTask t = db.getDataCollectionTimerTask();

        TimedEvent e = t.collectMetrics();

        assertNotNull(e);

        //
        // we preserve the original semantics of the Event's methods:
        //

        List<Property> properties = e.getProperties();
        assertEquals(3, properties.size());

        TimestampProperty p = (TimestampProperty)e.getProperties().get(0);
        assertEquals(e.getTime().longValue(), ((Long)p.getValue()).longValue());

        EventProperty ep = (EventProperty)properties.get(1);
        assertEquals("mock://mock-metric-source-1", ep.getName());
        List<Property> secondLevelProperties = ep.getEvent().getProperties();

        EventProperty ep2 = (EventProperty)properties.get(2);
        assertEquals("mock://mock-metric-source-2", ep2.getName());
        List<Property> secondLevelProperties2 = ep2.getEvent().getProperties();

        assertEquals("shared-mock-metric-id", secondLevelProperties.get(0).getName());
        assertEquals("mock-value-for-source-1", secondLevelProperties.get(0).getValue());
        assertEquals("shared-mock-metric-id", secondLevelProperties2.get(0).getName());
        assertEquals("mock-value-for-source-2", secondLevelProperties2.get(0).getValue());
    }

    // toLogMessage() --------------------------------------------------------------------------------------------------

    @Test
    public void toLogMessage_null() throws Exception {

        assertNull(DataCollectionTask.toLogMessage(null));
    }

    @Test
    public void toLogMessage_nullMessage() throws Exception {

        RuntimeException e = new RuntimeException();
        assertEquals(null, e.getMessage());

        String s = DataCollectionTask.toLogMessage(e);
        assertEquals("RuntimeException with no message, see stack trace below for more details", s);
    }

    @Test
    public void toLogMessage() throws Exception {

        RuntimeException e = new RuntimeException("some thing");
        assertEquals("some thing", e.getMessage());

        String s = DataCollectionTask.toLogMessage(e);
        assertEquals("some thing (RuntimeException)", s);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
