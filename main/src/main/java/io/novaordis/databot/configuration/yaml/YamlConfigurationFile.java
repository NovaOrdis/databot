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
import io.novaordis.events.api.metric.MetricDefinitionParser;
import io.novaordis.events.api.metric.MetricSourceDefinition;
import io.novaordis.events.api.metric.MetricSourceDefinitionImpl;
import io.novaordis.events.api.metric.MetricSourceException;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.logging.LoggerConfiguration;
import io.novaordis.utilities.logging.YamlLoggingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static final String SOURCES_KEY = "sources";

    public static final String OUTPUT_KEY = "output";

    public static final String OUTPUT_FILE_KEY = "file";

    public static final String OUTPUT_APPEND_KEY = "append";

    public static final String METRICS_KEY = "metrics";

    // Static ----------------------------------------------------------------------------------------------------------

    public static Object fromYaml(InputStream is) {

        Yaml yaml = new Yaml();
        return yaml.load(is);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private YamlLoggingConfiguration delegate;

    // Constructors ----------------------------------------------------------------------------------------------------

    public YamlConfigurationFile(boolean foreground, String fileName) throws UserErrorException {

        super(foreground, fileName);
    }

    // Configuration implementation ------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------


    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected void load(InputStream is) throws UserErrorException {

        log.debug("loading configuration from " + getFileName());

        Object o = fromYaml(is);


        //
        // we get null on empty files
        //

        if (o == null) {

            throw new UserErrorException("empty configuration file");
        }

        if (!(o instanceof Map)) {

            throw new UserErrorException("invalid configuration file content, expecting a map");
        }

        Map m = (Map)o;

        o = m.get(SAMPLING_INTERVAL_KEY);

        if (o != null) {

            //
            // if null, we rely on the built-in values, set in the constructor
            //

            if (!(o instanceof Integer)) {

                throw new UserErrorException("invalid sampling interval value: \"" + o + "\"");
            }

            setSamplingIntervalSec((Integer)o);
        }

        o = m.get(SOURCES_KEY);

        if (o != null) {

            List<MetricSourceDefinition> definitions = parseSources(o);
            setMetricSourceDefinitions(definitions);
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

                AsynchronousCsvLineWriter w = new AsynchronousCsvLineWriter(outputFileName, append, null);
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

            for(Object le: list) {

                MetricDefinition md = toMetricDefinition(le);
                addMetricDefinition(md);
            }
        }

        //
        // we capture the metric order, to be later reflected in output
        //

        captureMetricOrder();
    }

    // Package protected static ----------------------------------------------------------------------------------------

    static MetricDefinition toMetricDefinition(Object o) throws UserErrorException {

        if (o == null) {

            throw new IllegalArgumentException("null metric definition");
        }

        if (!(o instanceof String)) {

            throw new RuntimeException(o + " NOT YET IMPLEMENTED");
        }

        String tok = (String)o;

        MetricDefinition md;

        try {

            md = MetricDefinitionParser.parse(tok);
        }
        catch (Exception e) {

            throw new UserErrorException(e);
        }

        return md;
    }

    static List<MetricSourceDefinition> parseSources(Object o) throws UserErrorException {

        if (!(o instanceof Map)) {

            throw new UserErrorException("invalid '" +  SOURCES_KEY + "' value: \"" + o + "\"");
        }

        Map sources = (Map)o;

        //
        // the keys are supposed to be metric source names
        //

        List<MetricSourceDefinition> sourceDefinitions = new ArrayList<>();

        for(Object sourceName: sources.keySet()) {

            if (!(sourceName instanceof String)) {

                throw new UserErrorException(
                        "expecting source name as String and got \"" + sourceName + "\" (" + sourceName.getClass().getSimpleName() + ")");
            }

            String sn = (String)sourceName;

            try {

                MetricSourceDefinitionImpl sd = new MetricSourceDefinitionImpl(sn, sources.get(sn));
                sourceDefinitions.add(sd);
            }
            catch(MetricSourceException e) {

                throw new UserErrorException(e);
            }
        }

        return sourceDefinitions;
    }

    @Override
    public Set<LoggerConfiguration> getConfiguration() {

        if (delegate == null) {

            return Collections.emptySet();
        }

        return delegate.getConfiguration();
    }

    @Override
    public File getFile() {

        if (delegate == null) {

            return null;
        }

        return delegate.getFile();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
