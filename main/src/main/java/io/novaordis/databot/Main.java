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
import io.novaordis.databot.configuration.Configuration;
import io.novaordis.databot.configuration.ConfigurationFactory;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.logging.StderrVerboseLogging;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class Main {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final int DEFAULT_EVENT_QUEUE_SIZE = 10;

    // Static ----------------------------------------------------------------------------------------------------------

    public static void main(String[] args) {

        try {

            StderrVerboseLogging.init();

            Configuration conf = ConfigurationFactory.buildInstance(args);

            CountDownLatch exitLatch = new CountDownLatch(1);

            //
            // the in-memory event queue. The data collector will read and collect readings into events, while
            // the writer will pick up events from the queue and write them wherever
            //

            final BlockingQueue<Event> events = new ArrayBlockingQueue<>(DEFAULT_EVENT_QUEUE_SIZE);

            //
            // the event writer
            //

            AsynchronousCsvLineWriter aw = new AsynchronousCsvLineWriter(events, conf);

            aw.start();

            //
            // the data collector - maintains the state between the collections
            //

            DataCollector dataCollector = new DataCollectorImpl();

            Timer timer = new Timer();

            List<MetricDefinition> metrics = conf.getMetricDefinitions();

            DataCollectionTimerTask t = new DataCollectionTimerTask(events, dataCollector, metrics);

            //
            // periodically read metrics for the known metric definitions
            //

            timer.scheduleAtFixedRate(t, 0, conf.getSamplingIntervalSec() * 1000L);

            exitLatch.await();
        }
        catch(UserErrorException e) {

            //
            // we know about this failure, it is supposed to go to stderr
            //

            Console.error(e.getMessage());
        }
        catch(Throwable t) {

            //
            // we don't expect that, provide more information
            //

            String msg = "internal failure: " + t.getClass().getSimpleName();
            if (t.getMessage() != null) {
                msg += ": " + t.getMessage();
            }
            msg += " (consult logs for more details)";
            Console.error(msg);
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
