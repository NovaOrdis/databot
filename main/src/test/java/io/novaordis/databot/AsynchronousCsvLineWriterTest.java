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

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.ShutdownEvent;
import io.novaordis.databot.configuration.MockConfiguration;
import io.novaordis.utilities.Files;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class AsynchronousCsvLineWriterTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(AsynchronousCsvLineWriterTest.class);

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
        assertTrue(Files.rmdir(new File(System.getProperty("basedir"), "target/test-scratch"), false));
    }

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_nullConfiguration() throws Exception {

        try {
            new AsynchronousCsvLineWriter(new ArrayBlockingQueue<>(1), null);
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void constructor_Foreground() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.setForeground(true);
        AsynchronousCsvLineWriter aw = new AsynchronousCsvLineWriter(new ArrayBlockingQueue<>(1), mc);

        assertEquals(System.out, aw.getPrintStream());
    }

    @Test
    public void constructor_OutputFile_DoesNotExist() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        File dir = new File(System.getProperty("basedir"), "target/test-scratch");
        assertTrue(dir.isDirectory());
        File file = new File(dir, "test.csv");
        assertFalse(file.isFile());

        mc.setOutputFileName(file.getPath());

        AsynchronousCsvLineWriter aw = new AsynchronousCsvLineWriter(new ArrayBlockingQueue<>(1), mc);

        aw.start();

        //
        // make sure the file is created
        //

        assertTrue(file.isFile());

        // the file is empty
        assertEquals("", Files.read(file));

        aw.getEventQueue().put(new ShutdownEvent());

        //
        // busy wait until the event gets processed
        //
        long t0 = System.currentTimeMillis();
        long timeout = 2000L;
        while(aw.isStarted() && System.currentTimeMillis() - t0 < timeout) {
            Thread.sleep(100L);
        }

        //
        // make sure the file is still there
        //

        assertTrue(file.isFile());

        // the file is empty
        assertEquals("", Files.read(file));
    }

    // lifecycle -------------------------------------------------------------------------------------------------------

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
        assertTrue(w.isHeaderOn());

        w.start();

        assertTrue(w.isStarted());

        //
        // send a timed event to the queue
        //

        GenericTimedEvent te = new GenericTimedEvent(0L);
        queue.put(te);

        //
        // busy wait until the event gets processed
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
        queue.put(se);

        //
        // busy wait until the event gets processed
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

    @Test
    public void lifecycle_InsureWeCloseThePrintStreamOnExit() throws Exception {

        MockConfiguration mc = new MockConfiguration();


        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);
        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(queue, mc);

        MockPrintStream mps = new MockPrintStream();
        w.setPrintStream(mps);

        w.start();

        assertFalse(mps.isClosed());

        //
        // shutdown
        //
        queue.put(new ShutdownEvent());

        //
        // busy wait until the event gets processed
        //
        long t0 = System.currentTimeMillis();
        long timeout = 2000L;
        while(w.isStarted() && System.currentTimeMillis() - t0 < timeout) {
            Thread.sleep(100L);
        }

        //
        // make sure the print stream is closed
        //

        assertTrue(mps.isClosed());
    }

    @Test
    public void lifecycle_AppendToFile() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        //
        // append
        //
        mc.setOutputFileAppend(true);

        File dir = new File(System.getProperty("basedir"), "target/test-scratch");
        assertTrue(dir.isDirectory());
        File file = new File(dir, "test.csv");
        assertFalse(file.isFile());

        //
        // create the file
        //
        Files.write(file, "test\n");
        assertEquals("test\n", Files.read(file));

        mc.setOutputFileName(file.getPath());

        AsynchronousCsvLineWriter aw = new AsynchronousCsvLineWriter(new ArrayBlockingQueue<>(1), mc);

        aw.start();

        //
        // make sure the file is created
        //

        assertTrue(file.isFile());

        // the file has content
        assertEquals("test\n", Files.read(file));

        //
        // write an event
        //
        aw.getEventQueue().put(new MockTimedEvent());

        //
        // shutdown
        //

        aw.getEventQueue().put(new ShutdownEvent());

        //
        // busy wait until the event gets processed
        //
        long t0 = System.currentTimeMillis();
        long timeout = 2000L;
        while(aw.isStarted() && System.currentTimeMillis() - t0 < timeout) {
            Thread.sleep(100L);
        }

        //
        // make sure the file is still there
        //

        assertTrue(file.isFile());

        //
        // the file must contain the original content and the event representation
        //
        String content = Files.read(file);

        StringTokenizer st = new StringTokenizer(content, "\n");

        String line = st.nextToken();
        assertEquals("test", line);

        line = st.nextToken();
        assertFalse(line.trim().isEmpty());
    }

    @Test
    public void lifecycle_OverwriteFile() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        //
        // append
        //
        mc.setOutputFileAppend(false);

        File dir = new File(System.getProperty("basedir"), "target/test-scratch");
        assertTrue(dir.isDirectory());
        File file = new File(dir, "test.csv");
        assertFalse(file.isFile());

        //
        // create the file
        //
        Files.write(file, "test\n");
        assertEquals("test\n", Files.read(file));

        mc.setOutputFileName(file.getPath());

        AsynchronousCsvLineWriter aw = new AsynchronousCsvLineWriter(new ArrayBlockingQueue<>(1), mc);

        aw.start();

        //
        // make sure the file is created
        //

        assertTrue(file.isFile());

        // the file has been overwritten already
        assertTrue(Files.read(file).isEmpty());

        //
        // write an event
        //
        aw.getEventQueue().put(new MockTimedEvent());

        //
        // shutdown
        //

        aw.getEventQueue().put(new ShutdownEvent());

        //
        // busy wait until the event gets processed
        //
        long t0 = System.currentTimeMillis();
        long timeout = 2000L;
        while(aw.isStarted() && System.currentTimeMillis() - t0 < timeout) {
            Thread.sleep(100L);
        }

        //
        // make sure the file is still there
        //

        assertTrue(file.isFile());

        //
        // the original content must be gone and the event representation must have overwritten it
        //
        String content = Files.read(file);

        StringTokenizer st = new StringTokenizer(content, "\n");

        String line = st.nextToken();
        assertNotEquals("test", line);
    }

    // run() -----------------------------------------------------------------------------------------------------------

    @Test
    public void run_notATimedEvent() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.setForeground(true);
        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(2);
        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(queue, mc);

        // add an un-timed event, run() must not fail. It'll warn and continue

        MockEvent e = new MockEvent();
        queue.put(e);

        // this will stop the test
        queue.put(new ShutdownEvent());

        w.run();

        log.info("all good");

        //
        // busy wait until stopped
        //
        long t0 = System.currentTimeMillis();
        long timeout = 2000L;
        while(w.isStarted() && System.currentTimeMillis() - t0 < timeout) {
            Thread.sleep(100L);
        }
    }

    // write() ---------------------------------------------------------------------------------------------------------

    @Test
    public void write() throws Exception {

        MockMetricSource ms = new MockMetricSource();

        String metricDefinitionId = "Z";
        String metricDefinitionId2 = "A";

        MockMetricDefinition md = new MockMetricDefinition(ms, metricDefinitionId);
        MockMetricDefinition md2 = new MockMetricDefinition(ms, metricDefinitionId2);

        MockConfiguration mc = new MockConfiguration();

        mc.addMetricDefinition(md);
        mc.addMetricDefinition(md2);

        MockPrintStream mps = new MockPrintStream();

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(null, mc);

        w.setPrintStream(mps);

        assertTrue(w.isHeaderOn());

        MockProperty mp = new MockProperty(metricDefinitionId, "some value");
        MockProperty mp2 = new MockProperty(metricDefinitionId2, "some other value");

        MockTimedEvent e = new MockTimedEvent();

        e.setProperty(mp);
        e.setProperty(mp2);

        w.write(e);

        String header = mps.getLine();

        assertEquals("# timestamp, Z, A", header);

        String line = mps.getLine();

        assertTrue(line.contains(", some value, some other value"));

        line = mps.getLine();

        assertNull(line);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
