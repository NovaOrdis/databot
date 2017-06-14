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

package io.novaordis.databot.consumer;

import io.novaordis.events.api.event.Event;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/13/17
 */
public abstract class ActiveDataConsumerTest extends DataConsumerTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor_QueueAccessor() throws Exception {

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        ActiveDataConsumerBase c = getActiveDataConsumerToTest(queue);

        assertEquals(queue, c.getEventQueue());
    }

    @Test
    public void setEventQueue_getEventQueue() throws Exception {

        ActiveDataConsumerBase c = getActiveDataConsumerToTest(null);

        assertNull(c.getEventQueue());

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        c.setEventQueue(queue);

        assertEquals(queue, c.getEventQueue());
    }

    @Test
    public void lifecycle() throws Exception {

        BlockingQueue<Event> events = new ArrayBlockingQueue<>(10);

        ActiveDataConsumerBase c = getActiveDataConsumerToTest(events);

        assertFalse(c.isStarted());

        c.start();

        assertTrue(c.isStarted());

        //
        // start() is idempotent
        //

        c.start();

        assertTrue(c.isStarted());

        c.stop();

        //
        // wait until the shutdown event propagates
        //

        int waitTimeSecs = 3;
        long t0 = System.currentTimeMillis();

        while (c.isStarted()) {

            if (System.currentTimeMillis() - t0 > waitTimeSecs * 1000L) {

                fail(c + " did not propagate the shudown event in more than " + waitTimeSecs);
            }

            Thread.sleep(100L);
        }

        //
        // stop is idempotent
        //

        c.stop();

        assertFalse(c.isStarted());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected ActiveDataConsumerBase getDataConsumerToTest(BlockingQueue<Event> events) throws Exception {

        return getActiveDataConsumerToTest(events);
    }

    /**
     * @param events null is acceptable, will return an instance with a null event queue.
     */
    protected abstract ActiveDataConsumerBase getActiveDataConsumerToTest(BlockingQueue<Event> events) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
