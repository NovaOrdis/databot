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

import io.novaordis.events.core.event.GenericTimedEvent;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.TimedEvent;
import io.novaordis.osstats.metric.MetricDefinition;
import io.novaordis.osstats.metric.source.MetricSource;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
     * The method looks at the definitions of the metrics we need and determines the minimal amount of native
     * command executions and file system reads necessary in order to gather all the metrics.
     *
     * @param osName one of OS.Linux, OS.MacOS, OS.Windows
     *
     * @exception DataCollectionException if at least one metric has no source defined.
     */
    static Set<MetricSource> establishSources(List<MetricDefinition> metrics, String osName)
            throws DataCollectionException {

        //
        // find the common source of any possible pair
        //
        Set<MetricSource> sources = new HashSet<>();

        for(MetricDefinition d: metrics) {

            definitionLoop2: for(MetricDefinition d2: metrics) {

                if (d2.equals(d)) {
                    continue;
                }

                List<MetricSource> sl = d.getSources(osName);

                if (sl.isEmpty()) {
                    throw new DataCollectionException(d + " has no declared sources for " + osName);
                }

                List<MetricSource> sl2 = d2.getSources(osName);

                if (sl2.isEmpty()) {
                    throw new DataCollectionException(d2 + " has no declared sources for " + osName);
                }

                for(MetricSource s: sl) {
                    for(MetricSource s2: sl2) {
                        if (s.equals(s2)) {
                            sources.add(s);
                            break definitionLoop2;
                        }
                    }
                }
            }
        }

        //
        // identify the metrics that do not have a source yet and add their preferred source
        //
        metricLoop: for(MetricDefinition d: metrics) {

            for(MetricSource s: d.getSources(osName)) {

                if (sources.contains(s)) {
                    //
                    // we're good
                    //
                    continue metricLoop;
                }
            }

            //
            // add the preferred source
            //
            sources.add(d.getSources(osName).get(0));
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
     * Reads the specified metrics from the underlying O/S.
     *
     * @return a list of properties. Order matters, and the event will preserve the order as it is processed downstream.
     * A reading or parsing failure will be logged as warning and an empty property list will be returned.
     *
     * @exception DataCollectionException must carry a human-readable message, as the message will be displayed in logs.
     */
    List<Property> readMetrics(List<MetricDefinition> metricDefinitions) throws DataCollectionException {

        Set<MetricSource> sources = establishSources(metricDefinitions, os.getName());

        if (debug) { log.debug("metric sources: " + sources); }

        Set<Property> allProperties = new HashSet<>();

        for(MetricSource source: sources) {

            List<Property> props = source.collectMetrics(os);
            allProperties.addAll(props);
        }

        List<Property> properties = new ArrayList<>();

        if (debug) { log.debug("metric definitions: " + metricDefinitions); }

        metricLoop: for(MetricDefinition m: metricDefinitions) {

            //noinspection Convert2streamapi
            for(Property p: allProperties) {

                if (p.getName().equals(m.getName())) {
                    properties.add(p);
                    continue metricLoop;
                }
            }

            //
            // this is not supposed to happen, we must find at least one property that corresponds to the given
            // metric, if that is not the case, it means the metric was configured with the incorrect sources
            //

            throw new IllegalStateException("NOT YET IMPLEMENTED");
        }

        return properties;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
