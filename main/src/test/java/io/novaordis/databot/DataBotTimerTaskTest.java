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
import io.novaordis.databot.failure.EventQueueFullException;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.TimedEvent;
import io.novaordis.events.api.metric.MockAddress;
import io.novaordis.utilities.address.Address;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
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
public class DataBotTimerTaskTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // run() -----------------------------------------------------------------------------------------------------------

    @Test
    public void run_DataCollectionEncountersUnexpectedFailure_RunMustContinue() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        DataBot db = new DataBot(mc);

        DataBotTimerTask t = new DataBotTimerTask(db);

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

        DataBotTimerTask t = new DataBotTimerTask(db);

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

        DataBotTimerTask t = db.getTimerTask();

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
        assertTrue(event.getProperties().isEmpty());
    }

    // dataCollectionRun() ---------------------------------------------------------------------------------------------

    @Test
    public void dataCollectionRun() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        DataBot db = new DataBot(mc);
        DataBotTimerTask t = db.getTimerTask();

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

        assertTrue(event.getProperties().isEmpty());
    }

    @Test
    public void dataCollectionRun_QueueFull() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.setEventQueueSize(1);

        DataBot db = new DataBot(mc);
        DataBotTimerTask t = db.getTimerTask();

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
        DataBotTimerTask t = db.getTimerTask();

        long t0 = System.currentTimeMillis();

        TimedEvent e = t.collectMetrics();

        long t1 = System.currentTimeMillis();

        assertNotNull(e);

        assertTrue(t0 <= e.getTime());
        assertTrue(e.getTime() <= t1);
        assertTrue(e.getProperties().isEmpty());
    }

    @Test
    public void collectMetrics_OneSource_OneMetricDefinition_CollectionSucceeds() throws Exception {

        Address ma = new MockAddress("mock-metric-source");
        MockMetricDefinition mmd = new MockMetricDefinition(ma, "mock-metric-id");
        MockMetricSourceFactory mmsf = new MockMetricSourceFactory();

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceFactory(mmsf);
        mc.addMetricDefinition(mmd);

        DataBot db = new DataBot(mc);

        //
        // configure the mock source with "expected" values
        //

        MockMetricSource mms = (MockMetricSource)db.getMetricSource(ma);
        mms.addReadingForMetric("mock-metric-id", new MockProperty("mock-name", "mock-value"));

        DataBotTimerTask t = db.getTimerTask();

        long t0 = System.currentTimeMillis();

        TimedEvent e = t.collectMetrics();

        long t1 = System.currentTimeMillis();

        assertNotNull(e);

        assertTrue(t0 <= e.getTime());
        assertTrue(e.getTime() <= t1);

        Set<Property> properties = e.getProperties();
        assertEquals(1, properties.size());
        MockProperty p = (MockProperty)properties.iterator().next();
        assertEquals("mock-name", p.getName());
        assertEquals("mock-value", p.getValue());
    }

    @Test
    public void collectMetrics_OneSource_OneMetricDefinition_CollectionFailsWithCheckedException() throws Exception {

        Address ma = new MockAddress("mock-metric-source");
        MockMetricDefinition mmd = new MockMetricDefinition(ma, "mock-metric-id");
        MockMetricSourceFactory mmsf = new MockMetricSourceFactory();

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceFactory(mmsf);
        mc.addMetricDefinition(mmd);

        DataBot db = new DataBot(mc);

        //
        // configure the mock source to fail with checked exception
        //

        MockMetricSource mms = (MockMetricSource)db.getMetricSource(ma);
        mms.breakOnCollectWithMetricSourceException("SYNTHETIC CHECKED");

        DataBotTimerTask t = db.getTimerTask();

        long t0 = System.currentTimeMillis();

        TimedEvent e = t.collectMetrics();

        long t1 = System.currentTimeMillis();

        assertNotNull(e);

        assertTrue(t0 <= e.getTime());
        assertTrue(e.getTime() <= t1);

        Set<Property> properties = e.getProperties();
        assertTrue(properties.isEmpty());
    }

    @Test
    public void collectMetrics_OneSource_OneMetricDefinition_CollectionFailsWithUncheckedException() throws Exception {

        Address ma = new MockAddress("mock-metric-source");
        MockMetricDefinition mmd = new MockMetricDefinition(ma, "mock-metric-id");
        MockMetricSourceFactory mmsf = new MockMetricSourceFactory();

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceFactory(mmsf);
        mc.addMetricDefinition(mmd);

        DataBot db = new DataBot(mc);

        //
        // configure the mock source to fail with unchecked exception
        //

        MockMetricSource mms = (MockMetricSource)db.getMetricSource(ma);
        mms.breakOnCollectWithUncheckedException("SYNTHETIC UNCHECKED");

        DataBotTimerTask t = db.getTimerTask();

        long t0 = System.currentTimeMillis();

        TimedEvent e = t.collectMetrics();

        long t1 = System.currentTimeMillis();

        assertNotNull(e);

        assertTrue(t0 <= e.getTime());
        assertTrue(e.getTime() <= t1);

        Set<Property> properties = e.getProperties();
        assertTrue(properties.isEmpty());
    }

    // toLogMessage() --------------------------------------------------------------------------------------------------

    @Test
    public void toLogMessage_null() throws Exception {

        assertNull(DataBotTimerTask.toLogMessage(null));
    }

    @Test
    public void toLogMessage_nullMessage() throws Exception {

        RuntimeException e = new RuntimeException();
        assertEquals(null, e.getMessage());

        String s = DataBotTimerTask.toLogMessage(e);
        assertEquals("RuntimeException with no message, see stack trace below for more details", s);
    }

    @Test
    public void toLogMessage() throws Exception {

        RuntimeException e = new RuntimeException("some thing");
        assertEquals("some thing", e.getMessage());

        String s = DataBotTimerTask.toLogMessage(e);
        assertEquals("some thing (RuntimeException)", s);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
