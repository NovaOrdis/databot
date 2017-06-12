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

import io.novaordis.events.api.metric.MetricSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TimerTask;

/**
 * A timer task that insure the sources are started, starts them if they're not, collects the required metrics, wraps
 * them into a TimedEvent instance and puts the event on the event queue.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataBotTimerTask extends TimerTask {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DataBotTimerTask.class);

    //
    // counts how many executions were triggered since this task was created
    //
    private volatile long executionCount;

    // Static ----------------------------------------------------------------------------------------------------------

    static void handleSource(MetricSource source) {

        throw new RuntimeException("NYE");

//        List<MetricDefinition> metrics = conf.getMetricDefinitions();
//
//        BlockingQueue<Event> eventQueue = dataBot.getEventQueue();
//
//        try {
//
//            TimedEvent te = dataCollector.read(metrics);
//
//            boolean sent = eventQueue.offer(te);
//
//            if (!sent) {
//
//                log.warn("os-stats internal queue is full, which means events are not flushed to their destination");
//
//                //
//                // ... and just drop the event on the floor
//                //
//            }
//        }
//        catch(Throwable t) {
//
//            //
//            // IMPORTANT: an unchecked exception cancels the timer, and we don't want that, so log it and swallow it
//            //
//            String message = t.getMessage();
//            message = message != null ? message : t.getClass().getSimpleName();
//            message = "failed to collect data: " + message;
//            log.warn(message);
//            log.debug(message, t);
//        }
//    }

    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private DataBot dataBot;

    // Constructors ----------------------------------------------------------------------------------------------------

    public DataBotTimerTask(DataBot dataBot) {

        this.dataBot = dataBot;
    }

    // TimerTask overrides ---------------------------------------------------------------------------------------------

    @Override
    public void run() {

        executionCount ++;

        //
        // insure that each metric source is started; if not, start it and then collect metrics from it
        //

        List<MetricSource> sources = dataBot.getMetricSources();

        //noinspection Convert2streamapi
        for (MetricSource s : sources) {

            //
            // interact with source
            //

            handleSource(s);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    long getExecutionCount() {

        return executionCount;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
