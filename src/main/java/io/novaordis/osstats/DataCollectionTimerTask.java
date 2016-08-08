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
import io.novaordis.events.core.event.TimedEvent;
import io.novaordis.osstats.metric.MetricDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * A timer task that collect metrics, wraps them into a TimedEvent instance and puts the event on the event queue.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataCollectionTimerTask extends TimerTask {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DataCollectionTimerTask.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> eventQueue;
    private DataCollector dataCollector;
    private List<MetricDefinition> metrics;

    // Constructors ----------------------------------------------------------------------------------------------------

    public DataCollectionTimerTask(BlockingQueue<Event> eq, DataCollector dc, List<MetricDefinition> metrics) {

        this.eventQueue = eq;
        this.dataCollector = dc;
        this.metrics = new ArrayList<>(metrics);
    }

    // TimerTask overrides ---------------------------------------------------------------------------------------------

    @Override
    public void run() {

        try {

            TimedEvent te = dataCollector.read(metrics);
            boolean sent = eventQueue.offer(te);

            if (!sent) {

                log.warn("os-stats internal queue is full, which means events are not flushed to their destination");

                //
                // ... and just drop the event on the floor
                //
            }
        }
        catch(Throwable t) {

            //
            // IMPORTANT: an unchecked exception cancels the timer, and we don't want that, so log it and swallow it
            //
            String message = t.getMessage();
            message = message != null ? message : t.getClass().getSimpleName();
            message = "failed to collect data: " + message;
            log.warn(message);
            log.debug(message, t);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
