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

import io.novaordis.databot.DataConsumerBase;
import io.novaordis.databot.DataConsumerException;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.ShutdownEvent;
import io.novaordis.events.api.event.TimedEvent;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.core.ClosedException;
import io.novaordis.events.core.CsvOutputFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * An instance that takes events from an in-memory blocking queue and writes them asynchronously on its own thread,
 * as CSV lines to an output stream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class AsynchronousCsvLineWriter extends DataConsumerBase implements Runnable {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final boolean DEFAULT_APPEND = true;
    public static final boolean DEFAULT_PRINT_HEADER = true;

    private static final Logger log = LoggerFactory.getLogger(AsynchronousCsvLineWriter.class);
    private static final boolean debug = log.isDebugEnabled();

    private static final String DEFAULT_THREAD_NAME = "DataBot Data Writer";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Thread thread;

    private PrintStream printStream;

    //
    // may be null if the statistics collector was configured to run in foreground
    //
    private String outputFileName;

    private boolean append;

    private CsvOutputFormatter csvFormatter;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param outputFileName may be null, which means the output should be stdout.
     *
     * @param append may be null, which will determine the writer instance to fall back to default behavior.
     *
     * @see AsynchronousCsvLineWriter#DEFAULT_APPEND

     * @param printHeader may be null, which will determine the writer instance to fall back to default behavior.
     *
     * @see AsynchronousCsvLineWriter#DEFAULT_PRINT_HEADER
     */
    public AsynchronousCsvLineWriter(String outputFileName, Boolean append, Boolean printHeader)
            throws DataConsumerException {

        this.outputFileName = outputFileName;
        this.append = append == null ? DEFAULT_APPEND : append;
        boolean doPrintHeader = printHeader == null ? DEFAULT_PRINT_HEADER : printHeader;

        if (outputFileName == null) {

            printStream = System.out;
        }
        else {

            FileOutputStream fos;

            try {

                fos = new FileOutputStream(this.outputFileName, this.append);
            }
            catch(Exception e) {

                throw new DataConsumerException(e);
            }

            printStream = new PrintStream(fos);
        }

        csvFormatter = new CsvOutputFormatter();

        if (doPrintHeader) {

            csvFormatter.setHeaderOn();
        }
    }

    // DataConsumer implementation -------------------------------------------------------------------------------------

    @Override
    public synchronized void start() throws DataConsumerException {

        if (thread != null) {

            log.debug(this + " already started");
            return;
        }

        super.start();

        thread = new Thread(this, DEFAULT_THREAD_NAME);
        thread.start();

        log.debug(this + " started");
    }

    /**
     * @return true if there is a running thread actively processing queue events, of false if the instance was not
     * started or it was shut down
     */
    @Override
    public synchronized boolean isStarted() {

        return thread != null;
    }

    @Override
    public void stop() {

        throw new RuntimeException("stop() NOT YET IMPLEMENTED");
    }

    // Runnable implementation -----------------------------------------------------------------------------------------

    /**
     * Loops continuously, pulling events from the queue and turning them into CSV lines, until explicitly stopped.
     */
    @Override
    public void run() {

        log.debug(this + " running");

        while(true) {

            Event event;

            BlockingQueue<Event> eventQueue = getEventQueue();

            try {

                event = eventQueue.take();
            }
            catch(InterruptedException e) {

                //
                // log but otherwise ignore
                //
                log.debug("interrupted while waiting on queue");
                continue;
            }

            if (event instanceof ShutdownEvent) {

                //
                // clean up and shutdown
                //

                log.debug(this + " shutting down");

                synchronized (this) {

                    thread = null;
                    cleanup();
                    return;
                }

            }
            else if (event instanceof TimedEvent) {

                write((TimedEvent)event);
            }
            else {

                //
                // warn and continue
                //
                log.warn(this + " does not know how to handle " + event + ", ignoring it ...");
            }
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public boolean isOutputFileAppend() {

        return append;
    }

    /**
     * May return null if this writer sends content to stdout.
     */
    public String getOutputFileName() {

        return outputFileName;
    }

    public PrintStream getPrintStream() {

        return printStream;
    }

    /**
     * @return whether the line writer will generate a header on the first event or not.
     *
     * @see CsvOutputFormatter#isHeaderOn()
     */
    public boolean isHeaderOn() {

        return csvFormatter.isHeaderOn();
    }

    /**
     * In case we want to preserve the relative order of the metrics as declared in the configuration file, invoke
     * this before the instance is started. The method will set an output format that will capture the order.
     */
    public void setFieldOrder(List<MetricDefinition> metricsAsDeclaredInConfigurationFile) {

        if (metricsAsDeclaredInConfigurationFile.isEmpty()) {

            return;
        }

        String outputFormat = "timestamp, ";

        for(Iterator<MetricDefinition> i = metricsAsDeclaredInConfigurationFile.iterator(); i.hasNext(); ) {

            MetricDefinition md = i.next();
            outputFormat += md.getId();

            if (i.hasNext()) {

                outputFormat += ", ";
            }

        }
        csvFormatter.setOutputFormat(outputFormat);
    }

    @Override
    public String toString() {

        return "AsynchronousCsvLineWriter:header " +
                (isHeaderOn() ? "on" : "off") + ":" +
                (printStream == null ? "null" :
                        (printStream.equals(System.out) ? "/dev/stdout" : outputFileName));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void write(TimedEvent e) {

        if (debug) { log.debug("writing " + e); }

        try {

            if (csvFormatter.process(e)) {

                String content = new String(csvFormatter.getBytes());

                //
                // the formatter already appends a new line to the content, so there's no need we do
                //
                printStream.print(content);
            }
        }
        catch (ClosedException ce) {
            //
            // ignore
            //
            log.warn(csvFormatter + " closed");
        }
    }

    void setPrintStream(PrintStream ps) {

        this.printStream = ps;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void cleanup() {

        if (System.out.equals(printStream)) {

            return;
        }

        //
        // close the print stream - this will close the underlying stream
        //

        printStream.close();
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
