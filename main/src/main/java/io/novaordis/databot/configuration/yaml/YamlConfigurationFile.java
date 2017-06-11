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

package io.novaordis.databot.configuration.yaml;

import io.novaordis.databot.DataConsumerException;
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.databot.configuration.ConfigurationBase;
import io.novaordis.events.api.metric.MetricDefinitionException;
import io.novaordis.events.api.metric.MetricDefinitionParser;
import io.novaordis.events.api.metric.MetricSourceRepository;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * A configuration instance backed by a property file.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class YamlConfigurationFile extends ConfigurationBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(YamlConfigurationFile.class);

    public static final String SAMPLING_INTERVAL_KEY = "sampling.interval";

    public static final String OUTPUT_KEY = "output";
    public static final String OUTPUT_FILE_KEY = "file";
    public static final String OUTPUT_APPEND_KEY = "append";

    public static final String METRICS_KEY = "metrics";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public YamlConfigurationFile(boolean foreground, String fileName) throws UserErrorException {

        super(foreground, fileName);
    }

    // Configuration implementation ------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected void load(InputStream is) throws UserErrorException {

        log.debug("loading configuration");

        Yaml yaml = new Yaml();

        Map m = (Map)yaml.load(is);

        //
        // we get null on empty files
        //

        if (m == null) {

            throw new UserErrorException("empty configuration file");
        }

        Object o = m.get(SAMPLING_INTERVAL_KEY);

        if (o != null) {

            //
            // if null, we rely on the built-in values, set in the constructor
            //

            if (!(o instanceof Integer)) {

                throw new UserErrorException("invalid sampling interval value: \"" + o + "\"");
            }

            setSamplingIntervalSec((Integer)o);
        }

        o = m.get(OUTPUT_KEY);

        if (o == null) {

            throw new UserErrorException("missing '" + OUTPUT_KEY + "'");
        }
        else {

            String outputFileName;
            Boolean append = null;

            Map sm = (Map)o;

            o = sm.get(OUTPUT_FILE_KEY);

            if (o == null) {

                throw new UserErrorException("missing '" + OUTPUT_KEY + "." + OUTPUT_FILE_KEY + "'");
            }
            else {

                if (!(o instanceof String)) {

                    throw new UserErrorException("invalid output file name: \"" + o + "\"");
                }

                outputFileName = (String)o;
            }

            o = sm.get(OUTPUT_APPEND_KEY);

            if (o != null) {

                if (!(o instanceof Boolean)) {

                    throw new UserErrorException("invalid '" + OUTPUT_APPEND_KEY + "' boolean value: \"" + o + "\"");
                }

                append = (Boolean)o;
            }

            try {

                AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(outputFileName, append);
                addDataConsumer(w);
            }
            catch(DataConsumerException e) {

                throw new UserErrorException(e);
            }
        }

        o = m.get(METRICS_KEY);

        if (o != null) {

            if (!(o instanceof List)) {

                throw new UserErrorException("'" + YamlConfigurationFile.METRICS_KEY + "' not a list");
            }

            List list = (List)o;

            MetricSourceRepository mr = getMetricSourceRepository();

            for(Object le: list) {

                MetricDefinition md = toMetricDefinition(mr, le);
                addMetricDefinition(md);
            }
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    protected static MetricDefinition toMetricDefinition(MetricSourceRepository sourceRepository, Object o)
            throws UserErrorException {

        if (o == null) {

            throw new IllegalArgumentException("null metric definition");
        }

        if (!(o instanceof String)) {

            throw new RuntimeException(o + " NOT YET IMPLEMENTED");
        }

        String tok = (String)o;

        MetricDefinition md;

        try {

            md = MetricDefinitionParser.parse(sourceRepository, tok);
        }
        catch (MetricDefinitionException e) {

            throw new UserErrorException(e);
        }

        return md;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
