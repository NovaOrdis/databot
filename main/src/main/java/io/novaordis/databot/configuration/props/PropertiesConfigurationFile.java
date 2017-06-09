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

package io.novaordis.databot.configuration.props;

import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.databot.configuration.ConfigurationBase;
import io.novaordis.events.api.metric.MetricDefinitionException;
import io.novaordis.events.api.metric.MetricDefinitionParser;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * A configuration instance backed by a property file.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class PropertiesConfigurationFile extends ConfigurationBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(PropertiesConfigurationFile.class);

    public static final String SAMPLING_INTERVAL_PROPERTY_NAME = "sampling.interval";
    public static final String OUTPUT_FILE_PROPERTY_NAME = "output.file";
    public static final String OUTPUT_FILE_APPEND_PROPERTY_NAME = "output.file.append";
    public static final String METRICS_PROPERTY_NAME = "metrics";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public PropertiesConfigurationFile(boolean foreground, String fileName) throws UserErrorException {

        super(foreground, fileName);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected void load(InputStream is) throws UserErrorException {

        log.debug("loading configuration");

        Properties properties = new Properties();

        try {

            properties.load(is);
        }
        catch(IOException e) {

            throw new UserErrorException("failed to read input file", e);
        }


        String s = properties.getProperty(SAMPLING_INTERVAL_PROPERTY_NAME);

        if (s != null) {

            //
            // if null, we rely on the built-in values, set in the constructor
            //

            try {

                setSamplingIntervalSec(Integer.parseInt(s));
            }
            catch(Exception e) {
                throw new UserErrorException("invalid sampling interval value: \"" + s + "\"", e);
            }
        }

        s = properties.getProperty(OUTPUT_FILE_PROPERTY_NAME);

        if (s != null) {

            setOutputFileName(s);
        }

        s = properties.getProperty(OUTPUT_FILE_APPEND_PROPERTY_NAME);

        if (s != null) {

            String ls = s;
            ls = ls.trim().toLowerCase();

            if ("true".equals(ls) || "yes".equals(ls)) {

                setOutputFileAppend(true);
            }
            else if ("false".equals(ls) || "no".equals(ls)) {

                setOutputFileAppend(false);
            }
            else {

                throw new UserErrorException(
                        "invalid '" + OUTPUT_FILE_APPEND_PROPERTY_NAME + "' boolean value: \"" + s + "\"");
            }
        }

        s = properties.getProperty(METRICS_PROPERTY_NAME);

        if (s != null) {

            StringTokenizer st = new StringTokenizer(s, ", ");

            while(st.hasMoreTokens()) {

                String tok = st.nextToken();

                try {

                    MetricDefinition md = MetricDefinitionParser.parse(null, tok);

                    addMetricDefinition(md);
                }
                catch(MetricDefinitionException e) {

                    throw new UserErrorException(e);
                }
            }
        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
