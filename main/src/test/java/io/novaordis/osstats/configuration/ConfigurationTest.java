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

import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.cpu.CpuUserTime;
import io.novaordis.events.api.metric.loadavg.LoadAverageLastMinute;
import io.novaordis.events.api.metric.memory.PhysicalMemoryTotal;
import io.novaordis.utilities.UserErrorException;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public abstract class ConfigurationTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // isForeground() --------------------------------------------------------------------------------------------------

    @Test
    public void isForeground_True() throws Exception {

        Configuration c = getConfigurationToTest(true, null);
        assertTrue(c.isForeground());
    }

    @Test
    public void isForeground_False() throws Exception {

        Configuration c = getConfigurationToTest(false, null);
        assertFalse(c.isForeground());
    }

    // constructor -----------------------------------------------------------------------------------------------

    @Test
    public void constructor_configurationFileDoesNotExist() throws Exception {

        try {

            getConfigurationToTest(true, "there/is/no/such/file");
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("there/is/no/such/file"));
            assertTrue(msg.contains("does not exist or cannot be read"));
        }
    }

    /**
     * The reference configuration is represented by files in ${basedir}/src/test/resources/data/configuration
     */
    @Test
    public void referenceConfiguration() throws Exception {

        String referenceFile = getReferenceFileName();

        assertTrue(new File(referenceFile).isFile());

        Configuration c = getConfigurationToTest(true, referenceFile);

        assertEquals(20, c.getSamplingIntervalSec());
        assertNotEquals(20, Configuration.DEFAULT_SAMPLING_INTERVAL_SEC);
        assertFalse(c.isOutputFileAppend());

        assertEquals("/tmp/test.csv", c.getOutputFileName());
        assertNotEquals("/tmp/test.csv", Configuration.DEFAULT_OUTPUT_FILE_NAME);

        List<MetricDefinition> metrics = c.getMetricDefinitions();

        assertEquals(3, metrics.size());

        PhysicalMemoryTotal mt = (PhysicalMemoryTotal)metrics.get(0);
        assertNotNull(mt);

        CpuUserTime ct = (CpuUserTime)metrics.get(1);
        assertNotNull(ct);

        LoadAverageLastMinute lm = (LoadAverageLastMinute)metrics.get(2);
        assertNotNull(lm);
    }

    /**
     * The default configuration represents built-in values, values that are available when no external configuration
     * file is specified, or when no specific values are present in the configuration file.
     */
    @Test
    public void defaultConfiguration() throws Exception {

        Configuration c = getConfigurationToTest(false, null);

        assertEquals(Configuration.DEFAULT_SAMPLING_INTERVAL_SEC, c.getSamplingIntervalSec());
        assertEquals(Configuration.DEFAULT_OUTPUT_FILE_NAME, c.getOutputFileName());
        assertEquals(Configuration.DEFAULT_OUTPUT_FILE_APPEND, c.isOutputFileAppend());
        assertTrue(c.getMetricDefinitions().isEmpty());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @param fileName null acceptable, will produce a configuration file that exposes defaults.
     */
    protected abstract Configuration getConfigurationToTest(boolean foreground, String fileName) throws Exception;

    protected abstract String getReferenceFileName();

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
