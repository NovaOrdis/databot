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

import io.novaordis.osstats.env.EnvironmentVariableProvider;
import io.novaordis.osstats.env.MockEnvironmentVariableProvider;
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
    public void buildInstance_DefaultConfiguration() throws Exception {

        File d = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        assertTrue(d.isDirectory());

        EnvironmentVariableProvider orig = ConfigurationFactory.getEnvironmentVariableProvider();
        MockEnvironmentVariableProvider mevp = new MockEnvironmentVariableProvider();
        mevp.set(ConfigurationFactory.OS_STATS_CONFIG_DIR_ENVIRONMENT_VARIABLE_NAME, d.getAbsolutePath());

        try {

            ConfigurationFactory.setEnvironmentVariableProvider(mevp);

            Configuration c = ConfigurationFactory.buildInstance(new String[0]);

            assertEquals(Configuration.DEFAULT_SAMPLING_INTERVAL_SEC, c.getSamplingInterval());
            assertFalse(c.isForeground());
        }
        finally {

            ConfigurationFactory.setEnvironmentVariableProvider(orig);
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

        assertEquals(20, c.getSamplingInterval());
        assertFalse(c.isForeground());
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
        assertFalse(c.isForeground());
    }

    @Test
    public void buildInstance_Foreground_ShortOption() throws Exception {

        File d = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        EnvironmentVariableProvider orig = ConfigurationFactory.getEnvironmentVariableProvider();
        MockEnvironmentVariableProvider mevp = new MockEnvironmentVariableProvider();
        mevp.set(ConfigurationFactory.OS_STATS_CONFIG_DIR_ENVIRONMENT_VARIABLE_NAME, d.getAbsolutePath());

        try {

            ConfigurationFactory.setEnvironmentVariableProvider(mevp);

            Configuration c = ConfigurationFactory.buildInstance(new String[]{"-fg"});
            assertTrue(c.isForeground());
        }
        finally {
            ConfigurationFactory.setEnvironmentVariableProvider(orig);
        }
    }

    @Test
    public void buildInstance_Foreground_LongOption() throws Exception {

        File d = new File(System.getProperty("basedir"), "src/test/resources/data/configuration");
        EnvironmentVariableProvider orig = ConfigurationFactory.getEnvironmentVariableProvider();
        MockEnvironmentVariableProvider mevp = new MockEnvironmentVariableProvider();
        mevp.set(ConfigurationFactory.OS_STATS_CONFIG_DIR_ENVIRONMENT_VARIABLE_NAME, d.getAbsolutePath());

        try {

            ConfigurationFactory.setEnvironmentVariableProvider(mevp);

            Configuration c = ConfigurationFactory.buildInstance(new String[]{"--foreground"});
            assertTrue(c.isForeground());
        }
        finally {
            ConfigurationFactory.setEnvironmentVariableProvider(orig);
        }
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

        assertEquals(20, c.getSamplingInterval());
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

    // getDefaultConfigurationFileName() -------------------------------------------------------------------------------

    @Test
    public void getDefaultConfigurationFileName_NoOsStatsConfigDirEnvironmentVariableDefined() throws Exception {

        EnvironmentVariableProvider orig = ConfigurationFactory.getEnvironmentVariableProvider();
        MockEnvironmentVariableProvider mevp = new MockEnvironmentVariableProvider();

        try {

            ConfigurationFactory.setEnvironmentVariableProvider(mevp);
            ConfigurationFactory.getDefaultConfigurationFileName();
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals(
                    ConfigurationFactory.OS_STATS_CONFIG_DIR_ENVIRONMENT_VARIABLE_NAME +
                            " environment variable not defined", msg);
        }
        finally {
            ConfigurationFactory.setEnvironmentVariableProvider(orig);
        }
    }

    @Test
    public void getDefaultConfigurationFileName() throws Exception {

        EnvironmentVariableProvider orig = ConfigurationFactory.getEnvironmentVariableProvider();
        MockEnvironmentVariableProvider mevp = new MockEnvironmentVariableProvider();
        mevp.set(ConfigurationFactory.OS_STATS_CONFIG_DIR_ENVIRONMENT_VARIABLE_NAME, "something/something-else");


        try {
            ConfigurationFactory.setEnvironmentVariableProvider(mevp);
            String fileName = ConfigurationFactory.getDefaultConfigurationFileName();
            assertEquals("something/something-else/" + ConfigurationFactory.DEFAULT_CONFIGURATION_FILE_NAME, fileName);
        }
        finally {
            ConfigurationFactory.setEnvironmentVariableProvider(orig);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
