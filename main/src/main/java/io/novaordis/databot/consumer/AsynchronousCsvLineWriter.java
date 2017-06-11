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

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.DataConsumerException;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.ShutdownEvent;
import io.novaordis.events.api.event.TimedEvent;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.core.ClosedException;
import io.novaordis.events.core.CsvOutputFormatter;
import io.novaordis.databot.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * An instance takes events from an in-memory blocking queue and writes them, asynchronously on its own thread, as CSV
 * lines to an output stream.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class AsynchronousCsvLineWriter implements Runnable, DataConsumer {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(AsynchronousCsvLineWriter.class);
    private static final boolean debug = log.isDebugEnabled();

    private static final String DEFAULT_THREAD_NAME = "os-stats Data Writer";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> eventQueue;
    private Thread thread;
    private PrintStream printStream;

    // may be null if the statistic collector was configured to run in foreground
    private String outputFileName;

    private CsvOutputFormatter csvFormatter;

    // Constructors ----------------------------------------------------------------------------------------------------

    public AsynchronousCsvLineWriter(BlockingQueue<Event> eq, Configuration configuration)
            throws DataConsumerException {

        if (configuration == null) {

            throw new IllegalArgumentException("null configuration");
        }

        this.eventQueue = eq;

        if (configuration.isForeground()) {

            printStream = System.out;
        }
        else if ((outputFileName = configuration.getOutputFileName()) != null) {

            boolean append = configuration.isOutputFileAppend();

            FileOutputStream fos;

            try {

                fos = new FileOutputStream(outputFileName, append);
            }
            catch(Exception e) {

                throw new DataConsumerException(e);
            }
            printStream = new PrintStream(fos);
        }
        else {

            //
            // not foreground and output file name is null - allow for the possibility that the print stream will
            // be later installed with setPrintStream()
            //

            log.debug("print stream must be installed later");
        }

        csvFormatter = new CsvOutputFormatter();
        csvFormatter.setHeaderOn();

        //
        // we want to preserve the relative order of the metrics as declared in the configuration file, so we set the
        // output format here, where we have access to that order
        //

        String outputFormat = "timestamp";

        List<MetricDefinition> metrics = configuration.getMetricDefinitions();

        if (!metrics.isEmpty()) {

            outputFormat += ", ";
            for(Iterator<MetricDefinition> i = metrics.iterator(); i.hasNext(); ) {

                MetricDefinition md = i.next();
                outputFormat += md.getId();

                if (i.hasNext()) {

                    outputFormat += ", ";
                }

            }
        }
        csvFormatter.setOutputFormat(outputFormat);
    }

    // DataConsumer implementation -------------------------------------------------------------------------------------

    @Override
    public synchronized void start() {

        if (thread != null) {

            log.debug(this + " already started");
            return;
        }

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

    public PrintStream getPrintStream() {

        return printStream;
    }

    public BlockingQueue<Event> getEventQueue() {
        return eventQueue;
    }

    /**
     * @return whether the line writer will generate a header on the first event or not.
     *
     * @see CsvOutputFormatter#isHeaderOn()
     */
    public boolean isHeaderOn() {

        return csvFormatter.isHeaderOn();
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
