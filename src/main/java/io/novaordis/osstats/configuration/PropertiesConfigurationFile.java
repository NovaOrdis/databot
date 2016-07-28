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

package io.novaordis.osstats.configuration;

import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * A configuration instance backed by a property file.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class PropertiesConfigurationFile implements Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(PropertiesConfigurationFile.class);

    public static final String SAMPLING_INTERVAL_PROPERTY_NAME = "sampling.interval";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private int samplingInterval;

    // Constructors ----------------------------------------------------------------------------------------------------

    public PropertiesConfigurationFile(String fileName) throws UserErrorException {

        this();

        File f = new File(fileName);
        if (!f.isFile() || !f.canRead()) {
            throw new UserErrorException("configuration file " + fileName + " does not exist or cannot be read");
        }

        InputStream is = null;
        Properties props = new Properties();

        try {

            is = new FileInputStream(f);
            props.load(is);
        }
        catch(Exception e) {
            throw new UserErrorException("failure while reading configuration file " + f);
        }
        finally {

            if (is != null) {
                try {
                    is.close();
                }
                catch(Exception e) {
                    log.warn("failed to close input stream for " + f);
                }
            }
        }

        readConfiguration(props);
    }

    /**
     * Testing only.
     */
    PropertiesConfigurationFile() {

        this.samplingInterval = DEFAULT_SAMPLING_INTERVAL_SEC;
    }

    // Configuration implementation ------------------------------------------------------------------------------------

    @Override
    public int getSamplingInterval() {

        return samplingInterval;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    protected void readConfiguration(Properties properties) throws UserErrorException {

        String s = properties.getProperty(SAMPLING_INTERVAL_PROPERTY_NAME);

        if (s != null) {

            //
            // if null, we rely on the built-in values, set in the constructor
            //

            try {
                samplingInterval = Integer.parseInt(s);
            }
            catch(Exception e) {
                throw new UserErrorException("invalid sampling interval value: \"" + s + "\"", e);
            }
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
