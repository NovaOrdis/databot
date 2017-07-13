/*
 * Copyright (c) 2017 Nova Ordis LLC
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

package io.novaordis.databot.task;

import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricException;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.events.api.metric.MetricSourceException;
import io.novaordis.utilities.address.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * An executor task that queries a metric source for metrics and returns the metric values.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/14/17
 */
public class SourceQueryTask implements Callable<List<Property>> {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(SourceQueryTask.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private MetricSource source;

    private List<MetricDefinition> metricDefinitions;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param s the metric source to query.
     *
     * @param metrics all metrics must be associated with the given sources, otherwise the constructor will throw
     *                IllegalArgumentException
     */
    public SourceQueryTask(MetricSource s, List<MetricDefinition> metrics) {

        if (s == null) {

            throw new IllegalArgumentException("null metric source");
        }

        if (metrics == null) {

            throw new IllegalArgumentException("null metric definition list");
        }

        this.source = s;

        this.metricDefinitions = new ArrayList<>();

        //
        // check to see if all metric definitions are associated with the source we want to query and throw
        // IllegalArgumentException if we identify a definition associated with a different address.
        //

        //noinspection Convert2streamapi
        for(MetricDefinition d: metrics) {

            Address a = d.getMetricSourceAddress();

            if (!source.getAddress().equals(a)) {

                throw new IllegalArgumentException(d + " is not associated with source " + source);
            }

            metricDefinitions.add(d);
        }
    }

    // Callable implementation -----------------------------------------------------------------------------------------

    /**
     * This method is invoked on a source-handling thread, independently of other sources.
     *
     * @throws MetricSourceException if the query failed.
     */
    @Override
    public List<Property> call() throws MetricException {

        try {

            List<Property> result = source.collectMetrics(metricDefinitions);

            log.debug(this + " completed source collection");

            return result;
        }
        catch(RuntimeException t) {

            //
            // we also wrap unexpected exceptions into (expected) MetricSourceException so they cleanly propagate
            // to the collection task; do not log, we'll only add noise, they'll be logged upstairs.
            //
            throw new MetricSourceException(t);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Address getSourceAddress() {

        return source.getAddress();
    }

    public List<MetricDefinition>  getMetricDefinitions() {

        return metricDefinitions;
    }

    @Override
    public String toString() {

        return (source == null ? "UNINITIALIZED" : source.toString()) + " query";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
