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

import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricException;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.events.api.metric.MetricSourceException;
import io.novaordis.utilities.address.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/5/16
 */
public class MockMetricSource implements MetricSource {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MockMetricSource.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Map<MetricDefinition, Property> readingsForMetrics;

    private boolean breakOnCollect;

    private boolean started;

    private Address address;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockMetricSource() {

        this(null);
    }

    public MockMetricSource(Address a) {

        readingsForMetrics = new HashMap<>();
        this.address = a;
    }

    // MetricSource implementation -------------------------------------------------------------------------------------

    @Override
    public Address getAddress() {

        return address;
    }

    @Override
    public boolean hasAddress(Address address) {

        return this.address != null && this.address.equals(address);
    }

    @Override
    public List<Property> collectMetrics(List<MetricDefinition> metricDefinitions) throws MetricException {

        List<Property> result = new ArrayList<>();

        for(MetricDefinition d: metricDefinitions) {

            Property p = readingsForMetrics.get(d);

            if (p != null) {
                result.add(p);
            }
        }

        return result;
    }

    @Override
    public void start() throws MetricSourceException {

        started = true;

        log.info(this + " was started");
    }

    @Override
    public boolean isStarted() {

        return started;
    }

    @Override
    public void stop() {

        started = false;

        log.info(this + " was stopped");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void addReadingForMetric(MetricDefinition d, Property p) {

        readingsForMetrics.put(d, p);
    }

    public void breakOnCollectMetrics() {

        breakOnCollect = true;
    }

    @Override
    public String toString() {

        return "Mock Metric Source";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
