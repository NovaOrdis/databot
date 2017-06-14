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

import io.novaordis.databot.configuration.Configuration;
import io.novaordis.databot.failure.DataBotException;
import io.novaordis.databot.failure.EventQueueFullException;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.TimedEvent;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.utilities.address.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    private volatile long successfulExecutionCount;

    //
    // we collect the last cause of data run failure
    //
    private volatile Throwable causeOfLastFailure;

    // Static ----------------------------------------------------------------------------------------------------------

    public static String toLogMessage(Throwable t) {

        if (t == null) {

            return null;
        }

        String msg = t.getMessage();

        if (msg == null) {

            return t.getClass().getSimpleName() + " with no message, see stack trace below for more details";
        }

        return msg + " (" + t.getClass().getSimpleName() + ")";

    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private DataBot dataBot;

    // Constructors ----------------------------------------------------------------------------------------------------

    public DataBotTimerTask(DataBot dataBot) {

        setDataBot(dataBot);
    }

    // TimerTask overrides ---------------------------------------------------------------------------------------------

    @Override
    public void run() {

        executionCount ++;

        try {

            dataCollectionRun();

            successfulExecutionCount ++;

        }
        catch (Throwable t) {

            causeOfLastFailure = t;

            //
            // no matter of what happens during a data collection run, do not exit - keep going until explicitely
            // stopped; report the errors, though. The exceptions must not bubble up because an unchecked exception
            // cancels the timer.
            //

            log.error("data collection run failed: " + toLogMessage(t), t);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public DataBot getDataBot() {

        return dataBot;
    }

    /**
     * @return the cause of the last data run failure, if any. May return null if we did not experience any
     * failure so far.
     */
    public Throwable getCauseOfLastFailure() {

        return causeOfLastFailure;
    }

    public String toString() {

        return dataBot == null ? "uninitialized" : "" + dataBot.getId();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @return the number of times the data collection run was executed since this instance was created. Not all
     *  runs are necessarily successful. To get the number of successful runs, use getSuccessfulExecutionCount()
     *
     *  @see DataBotTimerTask#getSuccessfulExecutionCount()
     */
    long getExecutionCount() {

        return executionCount;
    }

    /**
     * @return the number of successful data collection runs since this instance was created.
     *
     *  @see DataBotTimerTask#getExecutionCount()
     */
    long getSuccessfulExecutionCount() {

        return successfulExecutionCount;
    }

    void setDataBot(DataBot dataBot) {

        this.dataBot = dataBot;
    }

    /**
     * The method collects all declared metrics, consolidates them in a TimeEvent and places the event on the internal
     * event queue. Even if the method throws unchecked exceptions, the calling layer will correctly handle those.
     *
     * @exception DataBotException exceptional conditions during the data collection run. The upper layer will
     *  handle appropriately.
     */
    void dataCollectionRun() throws DataBotException {

        log.debug(this + " executing data run ...");

        TimedEvent event = collectMetrics();

        BlockingQueue<Event> eventQueue = dataBot.getEventQueue();

        log.debug("placing the event " + event + " on the event queue");

        boolean sent = eventQueue.offer(event);

        if (sent) {

            log.debug("event successfully placed on the queue");
        }
        else {

            //
            // we will just drop the event and notify the upper layer
            //

            throw new EventQueueFullException();
        }
    }

    TimedEvent collectMetrics() {

        log.debug(this + " collecting metrics ...");

        Configuration configuration = dataBot.getConfiguration();

        Set<Address> sourceAddresses = configuration.getMetricSourceAddresses();

        List<Future<List<Property>>> futures = new ArrayList<>();

        long collectionStartTimestamp = System.currentTimeMillis();

        for(Address a: sourceAddresses) {

            //
            // dispatch an internal thread per source to collect metrics
            //

            List<MetricDefinition> metricsForSource = configuration.getMetricDefinitions(a);
            SourceQueryTask q = new SourceQueryTask(metricsForSource);
            Future<List<Property>> future = dataBot.getSourceExecutor().submit(q);
            futures.add(future);
        }

        //
        // wait for metric values of source failure
        //

        List<Property> allProperties = new ArrayList<>();

        for(Future<List<Property>> f: futures) {

            try {

                List<Property> properties = f.get();
                allProperties.addAll(properties);
            }
            catch (InterruptedException e) {

                throw new RuntimeException("NOT YET IMPLEMENTED");
            }
            catch (ExecutionException e) {

                Throwable cause = e.getCause();
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }
        }

        long collectionEndTimestamp = System.currentTimeMillis();

        log.debug("collection done in " + (collectionEndTimestamp - collectionStartTimestamp) + " ms");

        //
        // create the timed event
        //

        //
        // TODO: come up with more precise timing
        //

        long t = collectionStartTimestamp + (collectionEndTimestamp - collectionStartTimestamp) / 2;

        // It is possible to get an empty property list. This happens when the underlying layer fails to take a
        // reading. The underlying layer warned already, so we just generate an empty event, it'll show up in the
        // data set.

        //noinspection UnnecessaryLocalVariable
        TimedEvent te = new GenericTimedEvent(t, allProperties);

        return te;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
