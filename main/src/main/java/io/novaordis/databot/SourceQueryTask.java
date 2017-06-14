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

package io.novaordis.databot;

import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricSourceException;
import io.novaordis.utilities.address.Address;

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

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Address metricSourceAddress;

    private List<MetricDefinition> metricDefinitions;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param metrics all metrics must be associated with the same source, otherwise the constructor will throw
     *                IllegalArgumentException
     */
    public SourceQueryTask(List<MetricDefinition> metrics) {

        MetricDefinition previous = null;

        //
        // check to see if all metric definitions are associated with the same source and throw IllegalArgumentException
        // if we identify at least two definitions associated with different addresses.
        //

        //noinspection Convert2streamapi
        for(MetricDefinition d: metrics) {

            Address a = d.getMetricSourceAddress();

            if (metricSourceAddress == null) {

                metricSourceAddress = a;
            }
            else if (!metricSourceAddress.equals(a)) {

                throw new IllegalArgumentException("metrics do not belong to the same source: " + previous + ", " + d);
            }

            if (metricDefinitions == null) {

                metricDefinitions = new ArrayList<>();
            }

            metricDefinitions.add(d);
            previous = d;
        }
    }

    // Callable implementation -----------------------------------------------------------------------------------------

    /**
     * @throws MetricSourceException if the query failed.
     */
    @Override
    public List<Property> call() throws MetricSourceException {

        throw new RuntimeException("run() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Address getSourceAddress() {

        return metricSourceAddress;
    }

    public List<MetricDefinition>  getMetricDefinitions() {

        return metricDefinitions;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
