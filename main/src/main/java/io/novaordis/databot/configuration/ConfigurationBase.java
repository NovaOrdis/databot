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
import io.novaordis.events.api.metric.MetricSourceDefinition;
import io.novaordis.events.api.metric.MetricSourceDefinitionImpl;
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
import java.util.Collections;
import java.util.List;

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

    // may be null if the configuration was not loaded from a file
    private String fileName;

    private boolean foreground;

    private int samplingInterval;

    private List<MetricSourceDefinition> sourceDefinitions;

    private List<MetricDefinition> metricDefinitions;

    private List<DataConsumer> dataConsumers;

    private int eventQueueSize;

    private MetricSourceFactory sourceFactory;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param fileName null is acceptable, will return default values.
     */
    protected ConfigurationBase(boolean foreground, String fileName) throws UserErrorException {

        setForeground(foreground);

        this.fileName = fileName;

        this.samplingInterval = DEFAULT_SAMPLING_INTERVAL_SEC;

        setEventQueueSize(DEFAULT_EVENT_QUEUE_SIZE);

        this.sourceDefinitions = new ArrayList<>();
        this.metricDefinitions = new ArrayList<>();
        this.dataConsumers = new ArrayList<>();

        if (fileName != null) {

            FileInputStream fis = null;

            try {

                fis = new FileInputStream(fileName);
                load(fis);
            }
            catch(FileNotFoundException fe) {

                throw new UserErrorException("configuration file " + fileName + " does not exist or cannot be read");
            }
            catch (UserErrorException e) {

                //
                // add the file name to the error message
                //

                throw new UserErrorException(e.getMessage() + ": " + fileName);

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

    @Override
    public List<MetricDefinition> getMetricDefinitions(Address a) {

        if (metricDefinitions.isEmpty()) {

            return Collections.emptyList();
        }

        List<MetricDefinition> result = null;

        for(MetricDefinition d: metricDefinitions) {

            if (d.getMetricSourceAddress().equals(a)) {

                if (result == null) {

                    result = new ArrayList<>();
                }

                result.add(d);
            }
        }

        if (result == null) {

            return Collections.emptyList();
        }

        return result;
    }

    /**
     * @return the underlying storage.
     */
    @Override
    public List<MetricSourceDefinition> getMetricSourceDefinitions() {

        return sourceDefinitions;
    }

    @Override
    public int getMetricSourceCount() {

        return sourceDefinitions.size();
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

    /**
     * May be null if the configuration was not loaded from a file.
     */
    public String getFileName() {

        return fileName;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract void load(InputStream is) throws UserErrorException;

    protected void setSamplingIntervalSec(int i) {

        this.samplingInterval = i;
    }

    protected void addMetricDefinition(MetricDefinition md) {

        metricDefinitions.add(md);

        //
        // also, "collect" its metric source; if it is already there, adding it will be a noop
        //

        Address a = md.getMetricSourceAddress();
        addMetricSource(a);
    }

    /**
     * Insures that a MetricSourceDefinition corresponding to the given address already exists. If it exists, the
     * method is a noop. If it doesn't, a MetricSourceDefinition will be created and added.
     */
    protected void addMetricSource(Address a) {

        for(MetricSourceDefinition d: sourceDefinitions) {

            if (d.getAddress().equals(a)) {

                return;
            }
        }

        MetricSourceDefinition d = new MetricSourceDefinitionImpl(a);
        sourceDefinitions.add(d);
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

        List<MetricSourceDefinition> sds = getMetricSourceDefinitions();

        for(MetricSourceDefinition d : sds) {

            s += "    - " + d.getAddress() + "\n";
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
