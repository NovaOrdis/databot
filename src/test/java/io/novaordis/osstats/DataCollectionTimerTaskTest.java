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

import io.novaordis.events.core.event.Event;
import io.novaordis.events.metric.MetricDefinition;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataCollectionTimerTaskTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DataCollectionTimerTaskTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void theTimerTaskDoesNotThrowUncheckedExceptions() throws Exception {

        MockDataCollector mdc = new MockDataCollector();
        mdc.setBroken(true);
        List<MetricDefinition> metrics = Collections.singletonList(new MockMetricDefinition());

        DataCollectionTimerTask t = new DataCollectionTimerTask(null, mdc, metrics);

        // this MUST NOT throw any exception
        t.run();

        log.info("we're good");
    }

    @Test
    public void lifecycle() throws Exception {

        MockDataCollector mdc = new MockDataCollector();
        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);
        List<MetricDefinition> metrics = Collections.singletonList(new MockMetricDefinition());

        DataCollectionTimerTask t = new DataCollectionTimerTask(queue, mdc, metrics);

        t.run();

        // pick the mock event from the queue

        MockTimedEvent mte = (MockTimedEvent)queue.take();

        assertNotNull(mte);
    }

    @Test
    public void failureToOfferTheEventToTheQueue() throws Exception {

        MockDataCollector mdc = new MockDataCollector();

        // one element queue, the second will block
        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);
        assertTrue(queue.isEmpty());

        List<MetricDefinition> metrics = Collections.singletonList(new MockMetricDefinition());

        DataCollectionTimerTask t = new DataCollectionTimerTask(queue, mdc, metrics);

        t.run();

        assertEquals(1, queue.size());

        //
        // the queue is full now, run() one more time so the queue won't accept the event
        //

        t.run();

        log.info("we're good");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
