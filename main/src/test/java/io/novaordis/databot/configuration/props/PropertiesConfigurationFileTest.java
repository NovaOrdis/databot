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

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.configuration.Configuration;
import io.novaordis.databot.configuration.ConfigurationFactoryTest;
import io.novaordis.databot.configuration.ConfigurationTest;
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.utilities.UserErrorException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class PropertiesConfigurationFileTest extends ConfigurationTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ConfigurationFactoryTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // constructor -----------------------------------------------------------------------------------------------------

    // load() ----------------------------------------------------------------------------------------------------------

    @Test
    public void load_InvalidSamplingInterval() throws Exception {

        PropertiesConfigurationFile p = new PropertiesConfigurationFile(true, null);

        Properties props = new Properties();
        props.setProperty(PropertiesConfigurationFile.SAMPLING_INTERVAL_PROPERTY_NAME, "blah");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store(baos, "comments");
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {

            p.load(bais);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.startsWith("invalid sampling interval value: \"blah\""));
        }
    }

    @Test
    public void load_InvalidOutputFileAppend() throws Exception {

        PropertiesConfigurationFile p = new PropertiesConfigurationFile(true, null);

        Properties props = new Properties();
        props.setProperty(PropertiesConfigurationFile.OUTPUT_FILE_PROPERTY_NAME, "something.prop");
        props.setProperty(PropertiesConfigurationFile.OUTPUT_FILE_APPEND_PROPERTY_NAME, "blah");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store(baos, "comments");
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {

            p.load(bais);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid '" + PropertiesConfigurationFile.OUTPUT_FILE_APPEND_PROPERTY_NAME +
                    "' boolean value: \"blah\"", msg);
        }
    }

    @Test
    public void load_AsynchronousCsvWriter() throws Exception {

        PropertiesConfigurationFile p = new PropertiesConfigurationFile(true, null);

        Properties props = new Properties();
        props.setProperty(PropertiesConfigurationFile.OUTPUT_FILE_PROPERTY_NAME, "something");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store(baos, "comments");
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        p.load(bais);

        List<DataConsumer> dcs = p.getDataConsumers();
        assertEquals(1, dcs.size());

        AsynchronousCsvLineWriter w = (AsynchronousCsvLineWriter)dcs.get(0);
        assertEquals("something", w.getOutputFileName());

        assertEquals(AsynchronousCsvLineWriter.DEFAULT_APPEND, w.isOutputFileAppend());
    }

    @Test
    public void load() throws Exception {

        PropertiesConfigurationFile p = new PropertiesConfigurationFile(true, null);

        Properties props = new Properties();
        props.setProperty(PropertiesConfigurationFile.OUTPUT_FILE_PROPERTY_NAME, "something.props");
        props.setProperty(PropertiesConfigurationFile.OUTPUT_FILE_APPEND_PROPERTY_NAME, "false");
        props.setProperty(PropertiesConfigurationFile.METRICS_PROPERTY_NAME, "PhysicalMemoryTotal");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store(baos, "comments");
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        p.load(bais);

        List<DataConsumer> dcs = p.getDataConsumers();
        assertEquals(1, dcs.size());
        AsynchronousCsvLineWriter w = (AsynchronousCsvLineWriter)dcs.get(0);
        assertEquals("something.props", w.getOutputFileName());
        assertEquals(false, w.isOutputFileAppend());

        List<MetricDefinition> mds = p.getMetricDefinitions();
        assertEquals(1, mds.size());
        assertEquals("PhysicalMemoryTotal", mds.get(0).getId());

    }

    // parseMetricDefinitions() ----------------------------------------------------------------------------------------

    @Test
    public void parseMetricDefinitions() throws Exception {

        List<String> result = PropertiesConfigurationFile.parseMetricDefinitions("a, b, c");
        assertEquals(3, result.size());

        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    public void parseMetricDefinitions_Quotes() throws Exception {

        String s = "a, \"b, something, something else\", c";

        List<String> result = PropertiesConfigurationFile.parseMetricDefinitions(s);
        assertEquals(3, result.size());

        assertEquals("a", result.get(0));
        assertEquals("b, something, something else", result.get(1));
        assertEquals("c", result.get(2));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected Configuration getConfigurationToTest(boolean foreground, String fileName) throws Exception {

        return new PropertiesConfigurationFile(foreground, fileName);
    }

    @Override
    protected String getReferenceFileName() {

        File f = new File(System.getProperty("basedir"), "src/test/resources/data/configuration/reference-props.conf");
        assertTrue(f.isFile());
        return f.getPath();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
