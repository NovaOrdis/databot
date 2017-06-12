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

import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.databot.configuration.ConfigurationBase;
import io.novaordis.events.api.metric.MetricDefinitionException;
import io.novaordis.events.api.metric.MetricDefinitionParser;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    /**
     * Parses the value of the "metrics" (or equivalent) property. We expect a comma separated string, where values
     * that contain commas or spaces are double-quote enclosed.
     */
    public static List<String> parseMetricDefinitions(String s) {

        List<String> result = new ArrayList<>();

        int from = 0;
        boolean withinQuotes = false;

        for(int i = 0; i < s.length(); i ++) {

            char crt = s.charAt(i);

            if (crt == '"') {

                if (!withinQuotes) {

                    withinQuotes = true;
                    from = i + 1;
                    continue;
                }
                else {

                    //
                    // end quotes
                    //

                    withinQuotes = false;

                    String tok = s.substring(from, i).trim();

                    if (!tok.isEmpty()) {

                        result.add(tok);
                    }

                    from = i + 1;

                }
            }

            if (crt == ',') {

                if (withinQuotes) {

                    continue;
                }

                String tok = s.substring(from, i).trim();

                if (!tok.isEmpty()) {

                    result.add(tok);
                }

                from = i + 1;
            }
        }

        if (from < s.length() - 1) {

            String tok = s.substring(from).trim();

            if (!tok.isEmpty()) {

                result.add(tok);
            }
        }

        return result;
    }

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

        log.debug("loading configuration from input stream");

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

        String outputFileName;
        Boolean append = null;

        s = properties.getProperty(OUTPUT_FILE_PROPERTY_NAME);

        if (s == null) {

            throw new UserErrorException("missing '" + OUTPUT_FILE_PROPERTY_NAME + "'");
        }
        else {

            outputFileName = s;
        }

        s = properties.getProperty(OUTPUT_FILE_APPEND_PROPERTY_NAME);

        if (s != null) {

            String ls = s;
            ls = ls.trim().toLowerCase();

            if ("true".equals(ls) || "yes".equals(ls)) {

                append = true;
            }
            else if ("false".equals(ls) || "no".equals(ls)) {

                append = false;
            }
            else {

                throw new UserErrorException(
                        "invalid '" + OUTPUT_FILE_APPEND_PROPERTY_NAME + "' boolean value: \"" + s + "\"");
            }
        }

        try {

            AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(outputFileName, append, null);
            addDataConsumer(w);

        }
        catch(Exception e) {

            throw new UserErrorException(e);
        }

        s = properties.getProperty(METRICS_PROPERTY_NAME);

        if (s != null) {

            List<String> metricDefinitions = PropertiesConfigurationFile.parseMetricDefinitions(s);

            for(String mds: metricDefinitions) {

                try {

                    MetricDefinition md = MetricDefinitionParser.parse(null, mds);

                    addMetricDefinition(md);
                }
                catch(MetricDefinitionException e) {

                    throw new UserErrorException(e);
                }
            }
        }

        //
        // we capture the metric order, to be later reflected in output
        //

        captureMetricOrder();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
