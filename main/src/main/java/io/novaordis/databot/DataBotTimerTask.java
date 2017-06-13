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
import io.novaordis.events.api.metric.MetricSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

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
     * Even if the method throws unchecked exceptions, the calling layer will correctly handle those.
     *
     * @exception DataBotException exceptional conditions during the data collection run. The upper layer will
     *  handle appropriately.
     */
    void dataCollectionRun() throws DataBotException {

        log.debug(this + " executing data run ...");

        TimedEvent event = collectMetrics();

        BlockingQueue<Event> eventQueue = dataBot.getEventQueue();

        boolean sent = eventQueue.offer(event);

        if (!sent) {

            //
            // we will just drop the event and notify the upper layer
            //

            throw new EventQueueFullException();
        }
    }



//        //
//        // separate the metric definitions according their sources
//        //
//
//        Map<Address, Set<MetricDefinition>> metricDefinitionsPerSourceAddress = new HashMap<>();
//
//        for(MetricDefinition md: metricDefinitions) {
//
//            Address a = md.getMetricSourceAddress();
//
//            Set<MetricDefinition> mds = metricDefinitionsPerSourceAddress.get(a);
//
//            if (mds == null) {
//
//                mds = new HashSet<>();
//                metricDefinitionsPerSourceAddress.put(a, mds);
//            }
//
//            mds.add(md);
//        }
//
//        List<Property> properties = new ArrayList<>();
//
//        //
//        // process metrics per source
//        //
//
//        long readingBegins = System.currentTimeMillis();
//
//
//        for(Address a: metricDefinitionsPerSourceAddress.keySet()) {
//
//            Set<Property> fromSource = collect(a, metricDefinitionsPerSourceAddress.get(a));
//            properties.addAll(fromSource);
//        }
//
//        long readingEnds = System.currentTimeMillis();
//
//        log.debug("reading complete in " + (readingEnds - readingBegins) + " ms");
//
//        //
//        // create the timed event
//        //
//
//        long t = readingBegins + (readingEnds - readingBegins) / 2;
//
//        // It is possible to get an empty property list. This happens when the underlying layer fails to take a
//        // reading. The underlying layer warned already, so we just generate an empty event, it'll show up in the
//        // data set.
//
//        TimedEvent te = new GenericTimedEvent(t, properties);
//




//    List<Property> readMetrics(List<MetricDefinition> metricDefinitions) throws DataCollectionException {
//
//        Set<MetricSource> sources = establishSources(metricDefinitions);
//
//        if (debug) { log.debug("metric sources: " + sources); }
//
//        Set<Property> allProperties = new HashSet<>();
//
//        for(MetricSource source: sources) {
//
//            List<Property> props;
//
//            try {
//
//                //
//                // optimization: collect all possible metrics in one go. It may return an empty list for some sources
//                //
//                props = source.collectMetrics(metricDefinitions);
//            }
//            catch(MetricException e) {
//
//                throw new DataCollectionException(e);
//            }
//
//            allProperties.addAll(props);
//        }
//
//        List<Property> properties = new ArrayList<>();
//
//        if (debug) { log.debug("metric definitions: " + metricDefinitions); }
//
//        metricLoop: for(MetricDefinition m: metricDefinitions) {
//
//            //noinspection Convert2streamapi
//            for(Property p: allProperties) {
//
//                if (p.getName().equals(m.getId())) {
//                    properties.add(p);
//                    continue metricLoop;
//                }
//            }
//
//            //
//            // this happens when the "bulk" metric collection for a source returns an empty list. Attempt collecting
//            // the specific metric with its preferred source
//            //
//
//            MetricSource preferredSource = m.getSource();
//
//            try {
//
//                List<Property> props = preferredSource.collectMetrics(Collections.singletonList(m));
//
//                //
//                // because we're only passing one metric definition, we expect one property
//                //
//
//                if (props.size() != 1) {
//
//                    throw new DataCollectionException(
//                            m + " produced " + (props.size() == 0 ? "no" : props.size()) + " values");
//                }
//
//                Property p = props.get(0);
//                properties.add(p);
//            }
//            catch(MetricException e) {
//
//                throw new DataCollectionException(e);
//            }
//
//        }
//
//        return properties;
//    }


    TimedEvent collectMetrics() {

        log.debug(this + " collecting metrics ...");

        Configuration configuration = dataBot.getConfiguration();

        List<MetricDefinition> metricDefinitions = configuration.getMetricDefinitions();

        List<MetricSource> sources = consolidateSources(metricDefinitions);

        //noinspection Convert2streamapi
        for(MetricSource s: sources) {

            List<Property> properties = collectMetricsForSource(s);
        }

        GenericTimedEvent event = new GenericTimedEvent();

        return event;
    }

    List<MetricSource> consolidateSources(List<MetricDefinition> metricDefinitions) {

        return Collections.emptyList();
    }

    List<Property> collectMetricsForSource(MetricSource source) {

        return Collections.emptyList();
    }


    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
