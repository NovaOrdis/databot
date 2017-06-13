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

package io.novaordis.databot.configuration;

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricSourceFactory;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.address.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 5/12/17
 */
public abstract class ConfigurationBase implements Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ConfigurationBase.class);
    private static final boolean debug = log.isDebugEnabled();

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean foreground;

    private int samplingInterval;

    private Set<Address> metricSourceAddresses;

    private List<MetricDefinition> metricDefinitions;

    private List<DataConsumer> dataConsumers;

    private int eventQueueSize;

    private MetricSourceFactory sourceFactory;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param filename null is acceptable, will return default values.
     */
    protected ConfigurationBase(boolean foreground, String filename) throws UserErrorException {

        setForeground(foreground);

        this.samplingInterval = DEFAULT_SAMPLING_INTERVAL_SEC;

        setEventQueueSize(DEFAULT_EVENT_QUEUE_SIZE);

        this.metricSourceAddresses = new HashSet<>();
        this.metricDefinitions = new ArrayList<>();
        this.dataConsumers = new ArrayList<>();

        if (filename != null) {

            FileInputStream fis = null;

            try {

                fis = new FileInputStream(filename);
                load(fis);
            }
            catch(FileNotFoundException fe) {

                throw new UserErrorException("configuration file " + filename + " does not exist or cannot be read");
            }
            catch (UserErrorException e) {

                //
                // add the file name to the error message
                //

                throw new UserErrorException(e.getMessage() + ": " + filename);

            }
            finally {

                if (fis != null) {

                    try {

                        fis.close();
                    }
                    catch (IOException e) {

                        log.warn("failed to close input stream");
                    }
                }
            }
        }

        if (debug) {

            dumpConfiguration();
        }
    }

    // Configuration implementation ------------------------------------------------------------------------------------

    @Override
    public  boolean isForeground() {

        return foreground;
    }

    @Override
    public int getSamplingIntervalSec() {

        return samplingInterval;
    }

    @Override
    public int getEventQueueSize() {

        return eventQueueSize;
    }

    @Override
    public List<MetricDefinition> getMetricDefinitions() {

        return metricDefinitions;
    }

    /**
     * @return the underlying storage.
     */
    @Override
    public Set<Address> getMetricSourceAddresses() {

        return metricSourceAddresses;
    }

    /**
     * Returns the underlying storage so handle with care.
     */
    @Override
    public List<DataConsumer> getDataConsumers() {

        return dataConsumers;
    }

    @Override
    public MetricSourceFactory getMetricSourceFactory() {

        return sourceFactory;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract void load(InputStream is) throws UserErrorException;

    protected void setSamplingIntervalSec(int i) {

        this.samplingInterval = i;
    }

    protected void addMetricDefinition(MetricDefinition md) {

        metricDefinitions.add(md);

        //
        // also, "collect" its metric source in the repository; if it is already there, adding it will be a noop
        //

        Address a = md.getMetricSourceAddress();
        addMetricSourceAddress(a);
    }

    protected void addMetricSourceAddress(Address a) {

        metricSourceAddresses.add(a);
    }

    protected void addDataConsumer(DataConsumer dc) {

        dataConsumers.add(dc);
    }

    protected void setForeground(boolean b) {

        this.foreground = b;
    }

    protected void setEventQueueSize(int i) {

        this.eventQueueSize = i;
    }

    /**
     * This is a method to be invoked by subclasses after at the end of the load() method after both the data consumers
     * and the metrics have been parsed, and it is intended to capture the metric order, to be later reflected in
     * data consumer output.
     */
    protected void captureMetricOrder() {

        if (metricDefinitions.isEmpty()) {

            return;
        }

        if (dataConsumers.isEmpty()) {

            return;
        }

        //noinspection Convert2streamapi
        for(DataConsumer c: dataConsumers) {

            if (c instanceof AsynchronousCsvLineWriter) {

                ((AsynchronousCsvLineWriter)c).setFieldOrder(metricDefinitions);
            }
        }
    }

    protected void setMetricSourceFactory(MetricSourceFactory f) {

        this.sourceFactory = f;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    private void dumpConfiguration() {

        String s =
                "\n\nconfiguration:\n\n" +
                        " sampling interval:     " + getSamplingIntervalSec() + " seconds\n" +
                        " event queue size:      " + getEventQueueSize() + "\n" +
                        " metric sources:\n";

        Set<Address> as = getMetricSourceAddresses();

        for(Address a : as) {

            s += "    - " + a + "\n";
        }

        s += " metrics:\n";

        List<MetricDefinition> mds = getMetricDefinitions();

        for(MetricDefinition md: mds) {

            s += "    - " + md.getMetricSourceAddress() + "/" + md.getId() + "\n";
        }

        s += " data consumers:\n";

        List<DataConsumer> dcs = getDataConsumers();

        for(DataConsumer c: dcs) {

            s += "    - " + c + "\n";
        }

        log.debug(s);
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
