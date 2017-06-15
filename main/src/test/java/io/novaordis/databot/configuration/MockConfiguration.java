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

package io.novaordis.databot.configuration;

import io.novaordis.databot.DataConsumer;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricSourceFactory;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.address.Address;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class MockConfiguration extends ConfigurationBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockConfiguration() throws UserErrorException {

        super(false, null);
    }

    // ConfigurationBase overrides -------------------------------------------------------------------------------------

    @Override
    protected void load(InputStream is) throws UserErrorException {

        throw new RuntimeException("load() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setForeground(boolean b) {

        super.setForeground(b);
    }

    public void setEventQueueSize(int i) {

        super.setEventQueueSize(i);
    }

    public void setSamplingIntervalSec(int s) {

        super.setSamplingIntervalSec(s);
    }

    /**
     * The relative order is preserved.
     */
    public void addMetricDefinition(MetricDefinition md) {

        super.addMetricDefinition(md);
    }

    public void setMetricSourceAddresses(Set<Address> addresses) {

        //
        // we're not actually replacing the set, but we transfer the content
        //

        //noinspection Convert2streamapi
        for(Address a : addresses) {

            addMetricSource(a);
        }
    }

    public void setDataConsumers(List<DataConsumer> dcs) {

        //
        // we're not actually replacing the repository, but we transfer the content
        //

        //noinspection Convert2streamapi
        for(DataConsumer d: dcs) {

            addDataConsumer(d);
        }
    }

    @Override
    public void setMetricSourceFactory(MetricSourceFactory f) {

        super.setMetricSourceFactory(f);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
