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

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.databot.configuration.Configuration;
import io.novaordis.databot.configuration.ConfigurationTest;
import io.novaordis.events.api.metric.MetricSourceRepository;
import io.novaordis.events.api.metric.os.LocalOS;
import io.novaordis.utilities.UserErrorException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class YamlConfigurationFileTest extends ConfigurationTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // load() ----------------------------------------------------------------------------------------------------------

    @Test
    public void load_EmptyConfigurationFile() throws Exception {

        YamlConfigurationFile c = new YamlConfigurationFile(true, null);

        String s = "";
        InputStream is = new ByteArrayInputStream(s.getBytes());

        try {

            c.load(is);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.equals("empty configuration file"));
        }
    }

    @Test
    public void load_InvalidSamplingInterval() throws Exception {

        YamlConfigurationFile c = new YamlConfigurationFile(true, null);

        String s = "sampling.interval: blah";
        InputStream is = new ByteArrayInputStream(s.getBytes());

        try {

            c.load(is);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.startsWith("invalid sampling interval value: \"blah\""));
        }
    }

    @Test
    public void load_MissingOutputFile() throws Exception {

        YamlConfigurationFile c = new YamlConfigurationFile(true, null);

        String s =
                "output:\n" +
                "  append: false\n";

        InputStream is = new ByteArrayInputStream(s.getBytes());

        try {

            c.load(is);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertEquals(
                    "missing '" + YamlConfigurationFile.OUTPUT_KEY + "." + YamlConfigurationFile.OUTPUT_FILE_KEY + "'",
                    msg);
        }
    }

    @Test
    public void load_MissingOutput() throws Exception {

        YamlConfigurationFile c = new YamlConfigurationFile(true, null);

        String s = "something: something else\n";

        InputStream is = new ByteArrayInputStream(s.getBytes());

        try {

            c.load(is);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertEquals("missing '" + YamlConfigurationFile.OUTPUT_KEY + "'", msg);
        }
    }

    @Test
    public void load_AsynchronousCsvWriter() throws Exception {

        YamlConfigurationFile c = new YamlConfigurationFile(true, null);

        MetricSourceRepository mr = c.getMetricSourceRepository();
        assertTrue(mr.isEmpty());

        String s = "output:\n" +
                "  file: something\n" +
                "  append: false\n";

        InputStream is = new ByteArrayInputStream(s.getBytes());

        c.load(is);

        List<DataConsumer> dcs = c.getDataConsumers();
        assertEquals(1, dcs.size());

        AsynchronousCsvLineWriter w = (AsynchronousCsvLineWriter)dcs.get(0);
        assertEquals("something", w.getOutputFileName());
        assertEquals(false, w.isOutputFileAppend());
    }

    @Test
    public void load_MetricsNotAList() throws Exception {

        YamlConfigurationFile c = new YamlConfigurationFile(true, null);

        String s =
                "output:\n" +
                "  file: something.csv\n" +
                "metrics: something\n";

        InputStream is = new ByteArrayInputStream(s.getBytes());

        try {

            c.load(is);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertEquals("'" + YamlConfigurationFile.METRICS_KEY + "' not a list", msg);
        }
    }

    @Test
    public void load_Metrics() throws Exception {

        YamlConfigurationFile c = new YamlConfigurationFile(true, null);

        MetricSourceRepository mr = c.getMetricSourceRepository();
        assertTrue(mr.isEmpty());

        String s =
                "output:\n" +
                "  file: something.csv\n" +
                "metrics:\n" +
                "  - PhysicalMemoryTotal\n" +
                "  - CpuUserTime\n" +
                "  - LoadAverageLastMinute\n";

        InputStream is = new ByteArrayInputStream(s.getBytes());

        c.load(is);

        List<MetricDefinition> mds = c.getMetricDefinitions();
        assertEquals(3, mds.size());

        MetricDefinition md = mds.get(0);
        assertEquals("PhysicalMemoryTotal", md.getId());
        MetricDefinition md2 = mds.get(1);
        assertEquals("CpuUserTime", md2.getId());
        MetricDefinition md3 = mds.get(2);
        assertEquals("LoadAverageLastMinute", md3.getId());

        Set<LocalOS> localOSes = mr.getSources(LocalOS.class);
        assertEquals(1, localOSes.size());
        assertTrue(localOSes.contains(new LocalOS()));
    }

    // toMetricDefinition() --------------------------------------------------------------------------------------------

    @Test
    public void toMetricDefinition_Null() throws Exception {


        try {

            YamlConfigurationFile.toMetricDefinition(null, null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.equals("null metric definition"));
        }
    }

    @Test
    public void toMetricDefinition() throws Exception {

        MetricDefinition md = YamlConfigurationFile.toMetricDefinition(null, "PhysicalMemoryTotal");
        assertEquals("PhysicalMemoryTotal", md.getId());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected Configuration getConfigurationToTest(boolean foreground, String fileName) throws Exception {

        return new YamlConfigurationFile(foreground, fileName);
    }

    @Override
    protected String getReferenceFileName() {

        File f = new File(System.getProperty("basedir"), "src/test/resources/data/configuration/reference-yaml.yaml");
        assertTrue(f.isFile());
        return f.getPath();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
