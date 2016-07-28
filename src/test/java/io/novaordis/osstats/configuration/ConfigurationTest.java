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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

    /**
     * The default configuration represents built-in values, values that are available when no external configuration
     * file is specified, or when no specific values are present in the configuration file.
     */
    @Test
    public void defaultConfiguration() throws Exception {

        Configuration c = getConfigurationToTest(false);

        assertEquals(Configuration.DEFAULT_SAMPLING_INTERVAL_SEC, c.getSamplingInterval());
        assertEquals(Configuration.DEFAULT_OUTPUT_FILE_NAME, c.getOutputFileName());
    }

    /**
     * The reference configuration is represented by files in ${basedir}/src/test/resources/data/configuration
     */
    @Test
    public void referenceConfiguration() throws Exception {

        Configuration c = getConfigurationToTest(true);

        assertEquals(20, c.getSamplingInterval());
        assertNotEquals(20, Configuration.DEFAULT_SAMPLING_INTERVAL_SEC);

        assertEquals("/tmp/test.csv", c.getOutputFileName());
        assertNotEquals("/tmp/test.csv", Configuration.DEFAULT_OUTPUT_FILE_NAME);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @param useReferenceFile if true, use the corresponding reference file from under src/test/resources/data, if
     *                         false, don't use any file, but expect built-in values.
     */
    protected abstract Configuration getConfigurationToTest(boolean useReferenceFile) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
