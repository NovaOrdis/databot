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
import io.novaordis.databot.configuration.ConfigurationBase;
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.event.PropertyFactory;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricDefinitionParser;
import io.novaordis.events.api.metric.MetricSourceDefinition;
import io.novaordis.events.api.metric.MetricSourceDefinitionImpl;
import io.novaordis.events.api.metric.MetricSourceException;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.logging.AlternativeLoggingConfiguration;
import io.novaordis.utilities.logging.LoggerConfiguration;
import io.novaordis.utilities.logging.YamlLoggingConfiguration;
import io.novaordis.utilities.variable2.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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

    @Override
    public List<LoggerConfiguration> getLoggerConfiguration() {

        if (delegate == null) {

            return Collections.emptyList();
        }

        return delegate.getLoggerConfiguration();
    }

    @Override
    public File getFile() {

        if (delegate == null) {

            return null;
        }

        return delegate.getFile();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected void load(InputStream is) throws UserErrorException {

        log.debug("loading configuration from " + getFileName());

        Object o = fromYaml(is);

        Scope rootScope = getRootScope();

        Map topLevelMap = toNonNullMap(o);

        //
        // 'logging' top-level key - attempt to process logging overrides as early as possible, so we can turn on more
        // verbose logging as soon as possible, if needed
        //

        processLogging(topLevelMap.get(YamlLoggingConfiguration.LOGGING_KEY));


        //
        // 'sampling.interval'
        //

        processSamplingInterval(topLevelMap.get(SAMPLING_INTERVAL_KEY));

        //
        // 'sources'
        //

        processSources(topLevelMap.get(SOURCES_KEY), rootScope);

        //
        // 'output'
        //

        processOutput(topLevelMap.get(OUTPUT_KEY));

        //
        // 'metrics'
        //

        processMetrics(topLevelMap.get(METRICS_KEY), rootScope);
    }

    // Package protected static ----------------------------------------------------------------------------------------

    Map toNonNullMap(Object o) throws UserErrorException {

        //
        // we get null object on empty YAML files
        //

        if (o == null) {

            throw new UserErrorException("empty configuration file");
        }

        if (!(o instanceof Map)) {

            throw new UserErrorException("invalid configuration file content, expecting a map");
        }

        return (Map)o;
    }

    void processLogging(Object o) throws UserErrorException {

        if (o == null) {

            return;
        }

        if (!(o instanceof Map)) {

            throw new UserErrorException(
                    "'" + YamlLoggingConfiguration.LOGGING_KEY + "' must contain a Map, but it contains " +
                            o.getClass().getSimpleName());
        }

        try {

            this.delegate = new YamlLoggingConfiguration((Map)o);
        }
        catch(Exception e) {

            throw new UserErrorException(e);
        }

        //
        // apply new logging configuration as soon as we can
        //

        try {

            AlternativeLoggingConfiguration.apply(this.delegate, true);
        }
        catch(Exception e) {

            throw new UserErrorException(e);
        }
    }

    void processSamplingInterval(Object o) throws UserErrorException {

        if (o == null) {

            return;
        }

        //
        // if null, we rely on the built-in values, set in the constructor
        //

        if (!(o instanceof Integer)) {

            throw new UserErrorException("invalid sampling interval value: \"" + o + "\"");
        }

        setSamplingIntervalSec((Integer) o);
    }

    void processSources(Object o, Scope rootScope) throws UserErrorException {

        if (o == null) {

            return;
        }

        List<MetricSourceDefinition> definitions = parseSources(o);
        setMetricSourceVariables(definitions, rootScope);
        setMetricSourceDefinitions(definitions);
    }

    /**
     * Installs String variables named after the metric source names that carry the metric source address as value.
     * This way, the metric source names can be used as variables in metric definitions and simply those declarations.
     */
    static void setMetricSourceVariables(List<MetricSourceDefinition> definitions, Scope rootScope) {

        for(MetricSourceDefinition d: definitions) {

            String metricSourceName = d.getName();
            String address = d.getAddress().getLiteral();
            rootScope.declare(metricSourceName, String.class, address);
        }
    }

    void processOutput(Object o) throws UserErrorException {

        if (o == null) {

            throw new UserErrorException("missing '" + OUTPUT_KEY + "'");
        }

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

    void processMetrics(Object o, Scope rootScope) throws UserErrorException {

        if (o == null) {

            return;
        }

        if (!(o instanceof List)) {

            throw new UserErrorException("'" + YamlConfigurationFile.METRICS_KEY + "' not a list");
        }

        List list = (List)o;

        for(Object le: list) {

            MetricDefinition md = toMetricDefinition(getPropertyFactory(), rootScope, le);
            addMetricDefinition(md);
        }

        //
        // we capture the metric order, to be later reflected in output
        //

        captureMetricOrder();
    }

    static MetricDefinition toMetricDefinition(PropertyFactory pf, Scope rootScope, Object o)
            throws UserErrorException {

        if (o == null) {

            throw new IllegalArgumentException("null metric definition");
        }

        if (!(o instanceof String)) {

            throw new RuntimeException(o + " NOT YET IMPLEMENTED");
        }

        if (rootScope == null) {

            throw new IllegalArgumentException("null scope");
        }

        //
        // evaluate and replace all variables in the metric definitions, if any
        //

        String tok = (String)o;

        String declarationWithVariablesResolved = rootScope.evaluate(tok);

        MetricDefinition md;

        try {

            md = MetricDefinitionParser.parse(pf, declarationWithVariablesResolved);
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

            o = sources.get(sn);

            //
            // we expect a Map here, anything else is a configuration error. Help identify the cause of the failure
            // with user-friendly messages
            //

            if (o == null) {

                throw new UserErrorException("invalid empty metric source declaration: '" + sn + "' (check YAML indentation)");
            }

            if (!(o instanceof Map)) {

                throw new UserErrorException("invalid metric source declaration: '" + sn + "' not a map but a(n) " + o.getClass().getSimpleName());
            }

            try {

                MetricSourceDefinitionImpl sd = new MetricSourceDefinitionImpl(sn, o);
                sourceDefinitions.add(sd);
            }
            catch(MetricSourceException e) {

                throw new UserErrorException(e);
            }
        }

        return sourceDefinitions;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
