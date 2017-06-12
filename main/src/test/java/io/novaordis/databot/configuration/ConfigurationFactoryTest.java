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

import io.novaordis.utilities.UserErrorException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    // buildInstance() command line arguments --------------------------------------------------------------------------

    @Test
    public void buildInstance_NoConfigurationFileSpecified() throws Exception {

        try {

            ConfigurationFactory.buildInstance(new String[0]);
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("no configuration file specified"));
        }
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

        assertEquals(20, c.getSamplingIntervalSec());
        assertFalse(c.isForeground());
    }

    @Test
    public void buildInstance_ConfigurationFile_LongOption() throws Exception {

        File resourceDir = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        assertTrue(resourceDir.isDirectory());
        File configFile = new File(resourceDir, "reference-props.conf");
        assertTrue(configFile.isFile());

        String[] args = new String[] {"--configuration="+configFile.getAbsolutePath()};

        Configuration c = ConfigurationFactory.buildInstance(args);

        assertEquals(20, c.getSamplingIntervalSec());
        assertFalse(c.isForeground());
    }

    @Test
    public void buildInstance_Foreground() throws Exception {

        File resourceDir = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        assertTrue(resourceDir.isDirectory());

        File configFile = new File(resourceDir, "reference-props.conf");
        assertTrue(configFile.isFile());

        Configuration c;

        try {

            System.setProperty(ConfigurationFactory.FOREGROUND_SYSTEM_PROPERTY_NAME, "true");

            c = ConfigurationFactory.buildInstance(new String[]{"-c", configFile.getPath()});
        }
        finally {

            System.clearProperty(ConfigurationFactory.FOREGROUND_SYSTEM_PROPERTY_NAME);

        }

        assertTrue(c.isForeground());
    }

    @Test
    public void buildInstance_UnknownCommandOrOption() throws Exception {

        try {
            ConfigurationFactory.buildInstance(new String[]{"--configuraton"});
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("unknown command or option --configuraton", msg);
        }
    }

    // buildInstance() file name ---------------------------------------------------------------------------------------

    @Test
    public void buildInstance_FileName() throws Exception {

        File resourceDir = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        assertTrue(resourceDir.isDirectory());
        File configFile = new File(resourceDir, "reference-props.conf");
        assertTrue(configFile.isFile());

        Configuration c = ConfigurationFactory.buildInstance(configFile.getAbsolutePath(), false);

        assertEquals(20, c.getSamplingIntervalSec());
    }

    @Test
    public void buildInstance_UnknownType() throws Exception {

        try {
            ConfigurationFactory.buildInstance("something/that/we/do/not/recognize", false);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("we don't know yet to handle recognize configuration files", msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
