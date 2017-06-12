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

import io.novaordis.events.api.metric.MetricDefinition;
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

        List<MetricDefinition> metricDefinitions = dataBot.getConfiguration().getMetricDefinitions();


        // TODO bw3s5fsf4

        throw new RuntimeException("RETURN HERE");

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
//        BlockingQueue<Event> eventQueue = dataBot.getEventQueue();
//
//        try {
//
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
