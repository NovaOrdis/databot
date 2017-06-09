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

import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.TimedEvent;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricException;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataCollectorImpl implements DataCollector {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DataCollectionTimerTask.class);
    private static final boolean debug = log.isDebugEnabled();

    // Static ----------------------------------------------------------------------------------------------------------

    // Package protected static ----------------------------------------------------------------------------------------

    /**
     * The method looks at the definitions of all the metrics given as argument and determines the sources we need
     * to query to gather all the metrics.
     *
     * @exception DataCollectionException if at least one metric has no source defined.
     */
    static Set<MetricSource> establishSources(List<MetricDefinition> metrics) throws DataCollectionException {

        //
        // find the common source of any possible pair
        //
        Set<MetricSource> sources = new HashSet<>();

        for(MetricDefinition d: metrics) {

            MetricSource s  = d.getSource();

            if (s == null) {

                throw new DataCollectionException(d + " has no declared sources");
            }

            sources.add(s);
        }

        return sources;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private OS os;

    // Constructors ----------------------------------------------------------------------------------------------------

    public DataCollectorImpl(OS os) {
        this.os = os;
        log.debug(this + " created");
    }

    // DataCollectorImpl implementation --------------------------------------------------------------------------------

    @Override
    public TimedEvent read(List<MetricDefinition> metrics) throws DataCollectionException {

        log.debug("reading metrics ...");

        long readingBegins = System.currentTimeMillis();
        List<Property> properties = readMetrics(metrics);
        long readingEnds = System.currentTimeMillis();

        log.debug("reading complete in " + (readingEnds - readingBegins) + " ms");

        long t = readingBegins + (readingEnds - readingBegins) / 2;

        // It is possible to get an empty property list. This happens when the underlying layer fails to take a
        // reading. The underlying layer warned already, so we just generate an empty event, it'll show up in the
        // data set.

        return new GenericTimedEvent(t, properties);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return "DataCollectorImpl[" + os + ":" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Reads the specified metrics from the underlying metric sources.
     *
     * @return a list of properties. Order matters, and the event will preserve the order as it is processed downstream.
     * A reading or parsing failure will be logged as warning and an empty property list will be returned.
     *
     * @exception DataCollectionException must carry a human-readable message, as the message will be displayed in logs.
     */
    List<Property> readMetrics(List<MetricDefinition> metricDefinitions) throws DataCollectionException {

        Set<MetricSource> sources = establishSources(metricDefinitions);

        if (debug) { log.debug("metric sources: " + sources); }

        Set<Property> allProperties = new HashSet<>();

        for(MetricSource source: sources) {

            List<Property> props;

            try {

                //
                // optimization: collect all possible metrics in one go. It may return an empty list for some sources
                //
                props = source.collectMetrics(metricDefinitions);
            }
            catch(MetricException e) {

                throw new DataCollectionException(e);
            }

            allProperties.addAll(props);
        }

        List<Property> properties = new ArrayList<>();

        if (debug) { log.debug("metric definitions: " + metricDefinitions); }

        metricLoop: for(MetricDefinition m: metricDefinitions) {

            //noinspection Convert2streamapi
            for(Property p: allProperties) {

                if (p.getName().equals(m.getId())) {
                    properties.add(p);
                    continue metricLoop;
                }
            }

            //
            // this happens when the "bulk" metric collection for a source returns an empty list. Attempt collecting
            // the specific metric with its preferred source
            //

            MetricSource preferredSource = m.getSource();

            try {

                List<Property> props = preferredSource.collectMetrics(Collections.singletonList(m));

                //
                // because we're only passing one metric definition, we expect one property
                //

                if (props.size() != 1) {

                    throw new DataCollectionException(
                            m + " produced " + (props.size() == 0 ? "no" : props.size()) + " values");
                }

                Property p = props.get(0);
                properties.add(p);
            }
            catch(MetricException e) {

                throw new DataCollectionException(e);
            }

        }

        return properties;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
