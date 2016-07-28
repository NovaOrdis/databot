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
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class ConfigurationFactoryTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ConfigurationFactoryTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @After
    public void tearDown() {

        System.clearProperty(ConfigurationFactory.BIN_DIR_SYSTEM_PROPERTY_NAME);
    }

    // buildInstance() command line arguments --------------------------------------------------------------------------

    @Test
    public void buildInstance_DefaultConfiguration() throws Exception {

        File d = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        assertTrue(d.isDirectory());
        System.setProperty(ConfigurationFactory.BIN_DIR_SYSTEM_PROPERTY_NAME, d.getAbsolutePath());

        Configuration c = ConfigurationFactory.buildInstance(new String[0]);

        assertNotNull(c);

        assertEquals(Configuration.DEFAULT_SAMPLING_INTERVAL_SEC, c.getSamplingInterval());
    }

    @Test
    public void buildInstance_ConfigurationFile_ShortOption_NoFileNameFollows() throws Exception {

        try {
            ConfigurationFactory.buildInstance(new String[]{"-c"});
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals(
                    "a configuration file name should follow " + ConfigurationFactory.CONFIGURATION_FILE_SHORT_OPTION,
                    msg);
        }
    }

    @Test
    public void buildInstance_ConfigurationFile_LongOption_NoFileNameFollows() throws Exception {

        try {
            ConfigurationFactory.buildInstance(new String[]{"--configuration", "something"});
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("correct configuration file syntax is --configuration=<file-name>", msg);
        }
    }

    @Test
    public void buildInstance_ConfigurationFile_ShortOption() throws Exception {

        File resourceDir = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        assertTrue(resourceDir.isDirectory());
        File configFile = new File(resourceDir, "reference-props.conf");
        assertTrue(configFile.isFile());

        Configuration c = ConfigurationFactory.buildInstance(new String[] {
                "-c", configFile.getAbsolutePath() });

        assertNotNull(c);

        assertEquals(20, c.getSamplingInterval());
    }

    @Test
    public void buildInstance_ConfigurationFile_LongOption() throws Exception {

        File resourceDir = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        assertTrue(resourceDir.isDirectory());
        File configFile = new File(resourceDir, "reference-props.conf");
        assertTrue(configFile.isFile());

        Configuration c = ConfigurationFactory.buildInstance(new String[] {
                "--configuration="+configFile.getAbsolutePath() });

        assertEquals(20, c.getSamplingInterval());
    }

    // buildInstance() file name ---------------------------------------------------------------------------------------

    @Test
    public void buildInstance_FileName() throws Exception {

        File resourceDir = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        assertTrue(resourceDir.isDirectory());
        File configFile = new File(resourceDir, "reference-props.conf");
        assertTrue(configFile.isFile());

        Configuration c = ConfigurationFactory.buildInstance(configFile.getAbsolutePath());

        assertEquals(20, c.getSamplingInterval());
    }

    @Test
    public void buildInstance_UnknownType() throws Exception {

        try {
            ConfigurationFactory.buildInstance("something/that/we/do/not/recognize");
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("we don't know yet to handle recognize configuration files", msg);
        }
    }

    // getDefaultConfigurationFileName() -------------------------------------------------------------------------------

    @Test
    public void getDefaultConfigurationFileName_NoBinDir() throws Exception {

        try {
            ConfigurationFactory.getDefaultConfigurationFileName();
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("required system property '" + ConfigurationFactory.BIN_DIR_SYSTEM_PROPERTY_NAME + "' missing",
                    msg);
        }
    }

    @Test
    public void getDefaultConfigurationFileName() throws Exception {

        System.setProperty(ConfigurationFactory.BIN_DIR_SYSTEM_PROPERTY_NAME, "something/something-else");
        String fileName = ConfigurationFactory.getDefaultConfigurationFileName();
        assertEquals("something/something-else/" + ConfigurationFactory.DEFAULT_CONFIGURATION_FILE_NAME, fileName);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
