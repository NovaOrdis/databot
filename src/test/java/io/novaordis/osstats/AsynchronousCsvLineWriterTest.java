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
import io.novaordis.events.core.event.GenericTimedEvent;
import io.novaordis.events.core.event.ShutdownEvent;
import io.novaordis.osstats.configuration.MockConfiguration;
import org.junit.After;
import org.junit.Test;

import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class AsynchronousCsvLineWriterTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private PrintStream originalOut;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @After
    public void setUp() {

        this.originalOut = System.out;
    }

    @After
    public void cleanUp() {

        System.setOut(originalOut);
    }

    @Test
    public void lifecycle() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        //
        // set foreground true so we can test that the event was sent to stdout
        //
        mc.setForeground(true);

        MockPrintStream pseudoOut = new MockPrintStream();
        System.setOut(pseudoOut);

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(10);
        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(queue, mc);
        assertFalse(w.isStarted());

        w.start();

        assertTrue(w.isStarted());

        //
        // send a timed event to the queue
        //

        GenericTimedEvent te = new GenericTimedEvent(0L);
        queue.add(te);

        //
        // wait until the event gets processed, in a busy loop
        //

        long t0 = System.currentTimeMillis();
        long timeout = 5000L;
        String line = null;
        while(line == null && System.currentTimeMillis() - t0 < timeout) {
            Thread.sleep(100L);
            line = pseudoOut.getLine();
        }

        if (line == null) {
            fail("the asynchronous CSV line writer failed to process the event and send it to 'stdout' in " +
                    timeout + " ms");
        }

        //
        // send a shutdown event
        //

        ShutdownEvent se = new ShutdownEvent();
        queue.add(se);

        //
        // wait until the event gets in a busy loop
        //
        t0 = System.currentTimeMillis();
        timeout = 2000L;
        while(w.isStarted() && System.currentTimeMillis() - t0 < timeout) {
            Thread.sleep(100L);
        }

        if (w.isStarted()) {
            fail("the asynchronous CSV line writer failed to shutdown in " + timeout + " ms");
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
