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
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.events.api.metric.MetricSourceRepository;
import io.novaordis.events.api.metric.MetricSourceRepositoryImpl;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    private MetricSourceRepository metricSourceRepository;

    private List<MetricDefinition> metricDefinitions;

    private List<DataConsumer> dataConsumers;

    private int eventQueueSize;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param filename null is acceptable, will return default values.
     */
    protected ConfigurationBase(boolean foreground, String filename) throws UserErrorException {

        setForeground(foreground);

        this.samplingInterval = DEFAULT_SAMPLING_INTERVAL_SEC;

        setEventQueueSize(DEFAULT_EVENT_QUEUE_SIZE);

        this.metricSourceRepository = new MetricSourceRepositoryImpl();
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

    @Override
    public MetricSourceRepository getMetricSourceRepository() {

        return metricSourceRepository;
    }

    /**
     * Returns the underlying storage so handle with care.
     */
    @Override
    public List<DataConsumer> getDataConsumers() {

        return dataConsumers;
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

        MetricSource ms = md.getSource();
        addMetricSource(ms);
    }

    protected void addMetricSource(MetricSource ms) {

        metricSourceRepository.add(ms);
    }

    protected void addDataConsumer(DataConsumer dc) {

        throw new RuntimeException("NYE "+ dc);
    }

    protected void setForeground(boolean b) {

        this.foreground = b;
    }

    protected void setEventQueueSize(int i) {

        this.eventQueueSize = i;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    private void dumpConfiguration() {

        String s =
                "\n\nconfiguration:\n\n" +
                        " sampling interval:     " + getSamplingIntervalSec() + " seconds\n" +
                        " event queue size:      " + getEventQueueSize() + "\n" +
                        " metric sources:\n";

        Set<MetricSource> mss = getMetricSourceRepository().getSources();

        for(MetricSource ms : mss) {

            s += "    - " + ms + "\n";
        }

        s += " metrics:\n";

        List<MetricDefinition> mds = getMetricDefinitions();

        for(MetricDefinition md: mds) {

            s += "    - " + md.getSource() + "/" + md.getId() + "\n";
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
