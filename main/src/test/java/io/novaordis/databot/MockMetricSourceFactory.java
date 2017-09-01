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

import io.novaordis.events.api.event.PropertyFactory;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.events.api.metric.MetricSourceException;
import io.novaordis.events.api.metric.MetricSourceFactory;
import io.novaordis.utilities.address.Address;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/13/17
 */
public class MockMetricSourceFactory implements MetricSourceFactory {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private PropertyFactory propertyFactory;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockMetricSourceFactory(PropertyFactory f) {

        this.propertyFactory = f;
    }

    // MetricSourceFactory implementation ------------------------------------------------------------------------------

    @Override
    public MetricSource buildMetricSource(Address a) throws MetricSourceException {

        return new MockMetricSource(propertyFactory, a);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
