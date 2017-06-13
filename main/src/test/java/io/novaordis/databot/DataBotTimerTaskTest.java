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
import io.novaordis.databot.failure.EventQueueFullException;
import io.novaordis.events.api.event.Event;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
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

    private static final Logger log = LoggerFactory.getLogger(DataBotTimerTaskTest.class);

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
    public void run_NoMetricsProducesEmptyTimedEvents() throws Exception {

        fail("RETURN HERE");

    }

    @Test
    public void theTimerTaskDoesNotThrowUncheckedExceptions() throws Exception {

        fail("RETURN HERE");

//        MockConfiguration mc = new MockConfiguration();
//        DataBot d = new DataBot(mc);
//
//
//
//        MockDataCollector mdc = new MockDataCollector();
//        mdc.setBroken(true);
//
//        MockMetricSource mms = new MockMetricSource();
//        List<MetricDefinition> metrics = Collections.singletonList(new MockMetricDefinition(mms, "mock-metric-definition"));
//
//
//        DataBotTimerTask t = new DataBotTimerTask(d);
//
//        // this MUST NOT throw any exception
//        t.run();
//
//        log.info("we're good");
    }

    @Test
    public void lifecycle() throws Exception {

        fail("RETURN HERE");

//        MockConfiguration mc = new MockConfiguration();
//        DataBot d = new DataBot(mc);
//
//
//        MockDataCollector mdc = new MockDataCollector();
//        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);
//        MockMetricSource mms = new MockMetricSource();
//        List<MetricDefinition> metrics = Collections.singletonList(new MockMetricDefinition(mms, "mock-metric-definition"));
//
//        DataBotTimerTask t = new DataBotTimerTask(d);
//
//        t.run();
//
//        // pick the mock event from the queue
//
//        MockTimedEvent mte = (MockTimedEvent)queue.take();
//
//        assertNotNull(mte);
    }

    @Test
    public void failureToOfferTheEventToTheQueue() throws Exception {

        fail("RETURN HERE");

//        MockConfiguration mc = new MockConfiguration();
//        DataBot d = new DataBot(mc);
//
//
//        MockDataCollector mdc = new MockDataCollector();
//
//        // one element queue, the second will block
//        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);
//        assertTrue(queue.isEmpty());
//
//        MockMetricSource mms = new MockMetricSource();
//        List<MetricDefinition> metrics = Collections.singletonList(new MockMetricDefinition(mms, "mock-metric-definition"));
//
//        DataBotTimerTask t = new DataBotTimerTask(d);
//
//        t.run();
//
//        assertEquals(1, queue.size());
//
//        //
//        // the queue is full now, run() one more time so the queue won't accept the event
//        //
//
//        t.run();
//
//        log.info("we're good");
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
