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

package io.novaordis.databot.consumer;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.MockEvent;
import io.novaordis.databot.MockMetricDefinition;
import io.novaordis.databot.MockPrintStream;
import io.novaordis.databot.MockProperty;
import io.novaordis.databot.MockTimedEvent;
import io.novaordis.databot.event.MultiSourceReadingEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.PropertyFactory;
import io.novaordis.events.api.event.ShutdownEvent;
import io.novaordis.events.csv.Constants;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.address.AddressImpl;

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
public class AsynchronousCsvLineWriterTest extends DataConsumerTest {

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
    public void constructor_NullFileName_Foreground() throws Exception {

        AsynchronousCsvLineWriter aw = new AsynchronousCsvLineWriter(null, null, null);
        assertEquals(System.out, aw.getPrintStream());
    }

    @Test
    public void constructor_OutputFileDoesNotExist() throws Exception {

        File dir = new File(System.getProperty("basedir"), "target/test-scratch");
        assertTrue(dir.isDirectory());
        File file = new File(dir, "test.csv");
        assertFalse(file.isFile());

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(file.getPath(), null, null);

        BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(1);
        w.setEventQueue(eventQueue);

        w.start();

        //
        // make sure the file is created
        //

        assertTrue(file.isFile());

        //
        // the file must be empty
        //
        assertEquals("", Files.read(file));

        w.getEventQueue().put(new ShutdownEvent());

        //
        // busy wait until the event gets processed
        //
        long t0 = System.currentTimeMillis();
        long timeout = 2000L;

        while(w.isStarted() && System.currentTimeMillis() - t0 < timeout) {

            Thread.sleep(100L);
        }

        //
        // make sure the file is still there
        //

        assertTrue(file.isFile());

        //
        // the file must be empty
        //
        assertEquals("", Files.read(file));
    }

    // lifecycle -------------------------------------------------------------------------------------------------------

    @Test
    public void lifecycle() throws Exception {

        MockPrintStream pseudoOut = new MockPrintStream();
        System.setOut(pseudoOut);

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(10);

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(null, null, false);
        w.setEventQueue(queue);

        assertFalse(w.isStarted());
        assertFalse(w.isHeaderOn());

        w.start();

        assertTrue(w.isStarted());

        //
        // send a timed event to the queue, this will make the writer to eventually write a line containing a timestamp
        //

        long time = 1L;

        GenericTimedEvent te = new GenericTimedEvent(time);

        queue.put(te);

        //
        // busy wait until the event gets processed
        //
        String line = null;
        long timeout = 5000L;
        long t0 = System.currentTimeMillis();

        while(line == null && System.currentTimeMillis() - t0 < timeout) {

            Thread.sleep(100L);
            line = pseudoOut.getLine();
        }

        if (line == null) {

            fail("the asynchronous writer failed to process the event and send it to 'stdout' in " + timeout + " ms");
        }

        String expected = new SimpleDateFormat(Constants.DEFAULT_TIMESTAMP_FORMAT_LITERAL).format(time);
        assertEquals(expected, line);

        //
        // send a shutdown event
        //

        ShutdownEvent se = new ShutdownEvent();
        queue.put(se);

        //
        // busy wait until the event gets processed
        //
        timeout = 2000L;
        t0 = System.currentTimeMillis();

        while(w.isStarted() && System.currentTimeMillis() - t0 < timeout) {

            Thread.sleep(100L);
        }

        if (w.isStarted()) {

            fail("the asynchronous writer failed to shutdown in " + timeout + " ms");
        }
    }

    @Test
    public void lifecycle_InsureWeCloseThePrintStreamOnExit() throws Exception {

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(null, null, null);

        w.setEventQueue(queue);

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
        long timeout = 2000L;
        long t0 = System.currentTimeMillis();

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

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        File dir = new File(System.getProperty("basedir"), "target/test-scratch");
        assertTrue(dir.isDirectory());
        File file = new File(dir, "test.csv");
        assertFalse(file.isFile());

        //
        // create the file and add some content to it
        //

        assertTrue(Files.write(file, "test\n"));
        assertTrue(file.isFile());
        assertEquals("test\n", Files.read(file));

        //
        // do append to file
        //
        final boolean append = true;

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(file.getPath(), append, false);

        w.setEventQueue(queue);

        w.start();

        //
        // the file has content
        //

        assertEquals("test\n", Files.read(file));

        //
        // write an event
        //

        w.getEventQueue().put(new MockTimedEvent());

        //
        // shutdown
        //

        w.getEventQueue().put(new ShutdownEvent());

        //
        // busy wait until the event gets processed
        //
        long timeout = 2000L;
        long t = System.currentTimeMillis();

        while(w.isStarted() && System.currentTimeMillis() - t < timeout) {

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

        // must be a valid timestamp
        Date date = new SimpleDateFormat(Constants.DEFAULT_TIMESTAMP_FORMAT_LITERAL).parse(line);
        long time = date.getTime();

        assertTrue(time > 0);
    }

    @Test
    public void lifecycle_OverwriteFile() throws Exception {

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(1);

        File dir = new File(System.getProperty("basedir"), "target/test-scratch");
        assertTrue(dir.isDirectory());
        File file = new File(dir, "test.csv");
        assertFalse(file.isFile());

        //
        // create the file
        //

        assertTrue(Files.write(file, "test\n"));
        assertEquals("test\n", Files.read(file));
        assertTrue(file.isFile());

        //
        // do not append to file
        //
        final boolean append = false;

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(file.getPath(), append, false);

        w.setEventQueue(queue);

        w.start();

        //
        // the file has been overwritten already
        //

        assertTrue(Files.read(file).isEmpty());

        //
        // write an event
        //

        w.getEventQueue().put(new MockTimedEvent());

        //
        // shutdown
        //

        w.getEventQueue().put(new ShutdownEvent());

        //
        // busy wait until the event gets processed
        //
        long timeout = 2000L;
        long t0 = System.currentTimeMillis();

        while(w.isStarted() && System.currentTimeMillis() - t0 < timeout) {

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

    @Test
    public void lifecycle_AsynchronousStop() throws Exception {

        MockPrintStream pseudoOut = new MockPrintStream();
        System.setOut(pseudoOut);

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(10);

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(null, null, false);
        w.setEventQueue(queue);

        assertFalse(w.isStarted());
        assertFalse(w.isHeaderOn());

        w.start();

        assertTrue(w.isStarted());

        //
        // send a timed event to the queue, this will make the writer to eventually write a line containing a timestamp
        //

        long time = 1L;

        GenericTimedEvent te = new GenericTimedEvent(time);

        queue.put(te);

        //
        // busy wait until the event gets processed
        //
        String line = null;
        long timeout = 5000L;
        long t0 = System.currentTimeMillis();

        while(line == null && System.currentTimeMillis() - t0 < timeout) {

            Thread.sleep(100L);
            line = pseudoOut.getLine();
        }

        if (line == null) {

            fail("the asynchronous writer failed to process the event and send it to 'stdout' in " + timeout + " ms");
        }

        String expected = new SimpleDateFormat(Constants.DEFAULT_TIMESTAMP_FORMAT_LITERAL).format(time);
        assertEquals(expected, line);

        //
        // stop the writer asynchronously
        //

        w.stop();

        //
        // busy wait until the event gets processed
        //
        timeout = 2000L;
        t0 = System.currentTimeMillis();

        while(w.isStarted() && System.currentTimeMillis() - t0 < timeout) {

            Thread.sleep(100L);
        }

        if (w.isStarted()) {

            fail("the asynchronous writer failed to shutdown in " + timeout + " ms");
        }
    }

    // run() -----------------------------------------------------------------------------------------------------------

    @Test
    public void run_notATimedEvent() throws Exception {

        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(2);

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(null, null, null);

        w.setEventQueue(queue);

        // add an un-timed event, run() must not fail. It'll warn and continue

        MockEvent e = new MockEvent();

        queue.put(e);

        //
        // this will stop the test
        //

        queue.put(new ShutdownEvent());

        w.run();

        log.info("all good");

        //
        // busy wait until stopped
        //
        long timeout = 2000L;
        long t0 = System.currentTimeMillis();

        while(w.isStarted() && System.currentTimeMillis() - t0 < timeout) {

            Thread.sleep(100L);
        }
    }

    // write() ---------------------------------------------------------------------------------------------------------

    @Test
    public void write() throws Exception {

        PropertyFactory pf = new PropertyFactory();

        AddressImpl sourceAddress = new AddressImpl("mock-host");

        String metricDefinitionId = "Z";
        String metricDefinitionId2 = "A";

        MockMetricDefinition md = new MockMetricDefinition(pf, sourceAddress, metricDefinitionId);
        MockMetricDefinition md2 = new MockMetricDefinition(pf, sourceAddress, metricDefinitionId2);

        MockPrintStream mps = new MockPrintStream();

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(null, null, true);

        w.setFieldOrder(Arrays.asList(md, md2));

        w.setPrintStream(mps);

        assertTrue(w.isHeaderOn());

        MockProperty mp = new MockProperty(metricDefinitionId, "some value");
        MockProperty mp2 = new MockProperty(metricDefinitionId2, "some other value");

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        e.addSourceReading(sourceAddress, Arrays.asList(mp, mp2));

        w.write(e);

        String header = mps.getLine();

        assertEquals("# time, Mock Metric Z, Mock Metric A", header);

        String line = mps.getLine();

        assertTrue(line.contains(", some value, some other value"));

        line = mps.getLine();

        assertNull(line);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected DataConsumer getDataConsumerToTest(BlockingQueue<Event> events) throws Exception {

        AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(null, null, null);

        if (events != null) {

            w.setEventQueue(events);
        }
        return w;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
