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
    private String outputFileName;
    private boolean outputFileAppend;

    private MetricSourceRepository metricSourceRepository;

    private List<MetricDefinition> metricDefinitions;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param filename null is acceptable, will return default values.
     */
    protected ConfigurationBase(boolean foreground, String filename) throws UserErrorException {

        this.foreground = foreground;

        this.samplingInterval = DEFAULT_SAMPLING_INTERVAL_SEC;
        this.outputFileName = DEFAULT_OUTPUT_FILE_NAME;
        this.outputFileAppend = true;
        this.metricSourceRepository = new MetricSourceRepositoryImpl();
        this.metricDefinitions = new ArrayList<>();

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
    public String getOutputFileName() {

        return outputFileName;
    }

    @Override
    public boolean isOutputFileAppend() {

        return outputFileAppend;
    }

    @Override
    public List<MetricDefinition> getMetricDefinitions() {

        return metricDefinitions;
    }

    @Override
    public MetricSourceRepository getMetricSourceRepository() {

        return metricSourceRepository;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract void load(InputStream is) throws UserErrorException;

    protected void setSamplingIntervalSec(int i) {

        this.samplingInterval = i;
    }

    protected void setOutputFileName(String s) {

        this.outputFileName = s;
    }

    protected void setOutputFileAppend(boolean b) {

        this.outputFileAppend = b;
    }

    protected void addMetricDefinition(MetricDefinition md) {

        metricDefinitions.add(md);

        //
        // also, "collect" its metric source in the repository; if it is already there, adding it will be a noop
        //

        MetricSource ms = md.getSource();
        metricSourceRepository.add(ms);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    private void dumpConfiguration() {

        String s =
                "\n\nconfiguration:\n\n" +
                        " sampling interval:     " + getSamplingIntervalSec() + " seconds\n" +
                        " output file:           " + getOutputFileName() + "\n" +
                        " append to output file: " + isOutputFileAppend() + "\n" +
                        " metrics:\n";

        List<MetricDefinition> mds = getMetricDefinitions();

        for(MetricDefinition md: mds) {

            s += "    - " + md.getSource() + "/" + md.getId() + "\n";
        }

        log.debug(s);
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
