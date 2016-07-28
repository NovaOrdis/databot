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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class PropertiesConfigurationFileTest extends ConfigurationTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ConfigurationFactoryTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_configurationFileDoesNotExist() throws Exception {

        try {
            new PropertiesConfigurationFile("there/is/no/such/file", false);
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("configuration file there/is/no/such/file does not exist or cannot be read" , msg);
        }
    }

    // readConfiguration() ---------------------------------------------------------------------------------------------

    @Test
    public void readConfiguration_InvalidSamplingInterval() throws Exception {

        PropertiesConfigurationFile p = new PropertiesConfigurationFile();

        Properties props = new Properties();
        props.setProperty(PropertiesConfigurationFile.SAMPLING_INTERVAL_PROPERTY_NAME, "blah");

        try {

            p.readConfiguration(props);
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid sampling interval value: \"blah\"" , msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected Configuration getConfigurationToTest(boolean useReferenceFile) throws Exception {

        if (useReferenceFile) {

            File configFile = new File(
                    System.getProperty("basedir"), "src/test/resources/data/configuration/reference-props.conf");
            assertTrue(configFile.isFile());
            return new PropertiesConfigurationFile(configFile.getAbsolutePath(), false);
        }
        else {
            return new PropertiesConfigurationFile();
        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
