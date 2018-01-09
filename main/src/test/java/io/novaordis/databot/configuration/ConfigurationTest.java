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

package io.novaordis.databot.configuration;

import java.io.File;
import java.util.List;

import org.junit.Test;

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.MockMetricDefinition;
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.databot.consumer.MockDataConsumer;
import io.novaordis.events.api.event.PropertyFactory;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricSourceDefinition;
import io.novaordis.events.api.metric.MetricSourceType;
import io.novaordis.events.api.metric.MockAddress;
import io.novaordis.events.api.metric.jboss.JBossDmrMetricDefinition;
import io.novaordis.events.api.metric.jmx.JmxMetricDefinition;
import io.novaordis.events.api.metric.os.mdefs.CpuUserTime;
import io.novaordis.events.api.metric.os.mdefs.LoadAverageLastMinute;
import io.novaordis.events.api.metric.os.mdefs.PhysicalMemoryTotal;
import io.novaordis.jboss.cli.model.JBossControllerAddress;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.address.AddressImpl;
import io.novaordis.utilities.address.LocalOSAddress;
import io.novaordis.utilities.logging.LoggerConfiguration;
import io.novaordis.utilities.logging.log4j.Log4jLevel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

    // constructor -----------------------------------------------------------------------------------------------------

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

        //
        // logging
        //

        File file = c.getFile();
        assertEquals(new File("/tmp/test.log"), file);

        List<LoggerConfiguration> lcs = c.getLoggerConfiguration();
        assertEquals(1, lcs.size());
        LoggerConfiguration lc = lcs.get(0);
        assertEquals("io.novaordis.databot", lc.getName());
        assertEquals(Log4jLevel.TRACE, lc.getLevel());

        //
        // metric sources
        //

        assertEquals(4, c.getMetricSourceCount());

        List<MetricSourceDefinition> sourceDefinitions = c.getMetricSourceDefinitions();

        assertEquals(4, sourceDefinitions.size());

        MetricSourceDefinition d = sourceDefinitions.get(0);

        assertEquals(new JBossControllerAddress("jbosscli://admin@localhost:9999"), d.getAddress());
        assertEquals("local-jboss-instance", d.getName());
        assertEquals(MetricSourceType.JBOSS_CONTROLLER, d.getType());

        MetricSourceDefinition d2 = sourceDefinitions.get(1);

        assertEquals(new JBossControllerAddress("jbosscli://admin@other-host:10101"), d2.getAddress());
        assertEquals("remote-jboss-instance", d2.getName());
        assertEquals(MetricSourceType.JBOSS_CONTROLLER, d2.getType());

        MetricSourceDefinition d3 = sourceDefinitions.get(2);

        assertEquals(new LocalOSAddress(), d3.getAddress());
        assertNull(d3.getName());
        assertEquals(MetricSourceType.LOCAL_OS, d3.getType());

        MetricSourceDefinition d4 = sourceDefinitions.get(3);

        assertEquals(new AddressImpl("jmx://admin:admin123@localhost:9999"), d4.getAddress());
        assertNull(d4.getName());
        assertEquals(MetricSourceType.JMX, d4.getType());

        //
        // metric definitions
        //

        List<MetricDefinition> metrics = c.getMetricDefinitions();

        assertEquals(5, metrics.size());

        PhysicalMemoryTotal m = (PhysicalMemoryTotal)metrics.get(0);
        assertNotNull(m);
        assertEquals(new LocalOSAddress(), m.getMetricSourceAddress());

        CpuUserTime m2 = (CpuUserTime)metrics.get(1);
        assertNotNull(m2);
        assertEquals(new LocalOSAddress(), m2.getMetricSourceAddress());

        LoadAverageLastMinute m3 = (LoadAverageLastMinute)metrics.get(2);
        assertNotNull(m3);
        assertEquals(new LocalOSAddress(), m3.getMetricSourceAddress());

        JmxMetricDefinition m4 = (JmxMetricDefinition)metrics.get(3);
        assertNotNull(m4);
        assertEquals(
                "jmx://admin@localhost:9999/jboss.as:subsystem=messaging,hornetq-server=default,jms-queue=DLQ/messageCount",
                m4.getLabel());
        assertEquals(new AddressImpl("jmx", "admin", null, "localhost", 9999), m4.getMetricSourceAddress());

        JBossDmrMetricDefinition m5 = (JBossDmrMetricDefinition)metrics.get(4);
        assertNotNull(m5);
        assertEquals(
                "jbosscli://admin@localhost:9999/subsystem=messaging/hornetq-server=default/jms-queue=DLQ/message-count",
                m5.getLabel());
        assertTrue(new JBossControllerAddress("admin", null, "localhost", 9999).equals(m5.getMetricSourceAddress()));

        //
        // data consumers
        //

        List<DataConsumer> dcs = c.getDataConsumers();

        assertEquals(1, dcs.size());

        AsynchronousCsvLineWriter w = (AsynchronousCsvLineWriter)dcs.get(0);

        assertFalse(w.isStarted());
        assertFalse(w.isOutputFileAppend());
        assertEquals("/tmp/test.csv", w.getOutputFileName());
    }

    /**
     * The default configuration represents built-in values, values that are available when no external configuration
     * file is specified, or when no specific values are present in the configuration file.
     */
    @Test
    public void defaultConfiguration() throws Exception {

        Configuration c = getConfigurationToTest(false, null);

        assertEquals(Configuration.DEFAULT_SAMPLING_INTERVAL_SEC, c.getSamplingIntervalSec());
        assertEquals(Configuration.DEFAULT_EVENT_QUEUE_SIZE, c.getEventQueueSize());
        assertEquals(0, c.getMetricSourceCount());
        assertTrue(c.getMetricSourceDefinitions().isEmpty());
        assertTrue(c.getMetricDefinitions().isEmpty());
        assertTrue(c.getDataConsumers().isEmpty());
    }

    // addMetricDefinition() -------------------------------------------------------------------------------------------

    @Test
    public void addMetricDefinition() throws Exception {

        PropertyFactory pf = new PropertyFactory();

        ConfigurationBase c = (ConfigurationBase)getConfigurationToTest(false, null);

        assertEquals(0, c.getMetricSourceCount());
        assertTrue(c.getMetricDefinitions().isEmpty());
        assertTrue(c.getMetricSourceDefinitions().isEmpty());

        AddressImpl a = new AddressImpl("mock-host");

        MockMetricDefinition md = new MockMetricDefinition(pf, a, "mock");

        c.addMetricDefinition(md);

        List<MetricDefinition> mds = c.getMetricDefinitions();
        assertEquals(1, mds.size());
        assertTrue(mds.contains(md));

        List<MetricSourceDefinition> sds = c.getMetricSourceDefinitions();
        assertEquals(1, sds.size());
        assertTrue(sds.get(0).getAddress().equals(md.getMetricSourceAddress()));

        MockMetricDefinition md2 = new MockMetricDefinition(pf, a, "mock-2");

        c.addMetricDefinition(md2);

        mds = c.getMetricDefinitions();
        assertEquals(2, mds.size());
        assertEquals(mds.get(0), md);
        assertEquals(mds.get(1), md2);

        sds = c.getMetricSourceDefinitions();
        assertEquals(1, sds.size());
        assertTrue(sds.get(0).getAddress().equals(a));
    }

    // getMetricDefinitions() ------------------------------------------------------------------------------------------

    @Test
    public void getMetricDefinitions() throws Exception {

        PropertyFactory pf = new PropertyFactory();

        ConfigurationBase c = (ConfigurationBase)getConfigurationToTest(true, null);

        List<MetricDefinition> result = c.getMetricDefinitions();
        assertTrue(result.isEmpty());

        result = c.getMetricDefinitions(new MockAddress("no-such-address"));
        assertTrue(result.isEmpty());

        MockAddress ma = new MockAddress("test-address");
        MetricDefinition md = new MockMetricDefinition(pf, ma);

        c.addMetricDefinition(md);

        result = c.getMetricDefinitions();
        assertEquals(1, result.size());
        assertEquals(md, result.get(0));

        List<MetricDefinition> result2 = c.getMetricDefinitions(new MockAddress("test-address"));
        assertEquals(1, result2.size());
        assertEquals(md, result2.get(0));

        MockAddress ma2 = new MockAddress("test-address");
        MetricDefinition md2 = new MockMetricDefinition(pf, ma2);

        c.addMetricDefinition(md2);

        result = c.getMetricDefinitions();
        assertEquals(2, result.size());
        assertEquals(md, result.get(0));
        assertEquals(md2, result.get(1));

        result2 = c.getMetricDefinitions(new MockAddress("test-address"));
        assertEquals(2, result2.size());
        assertEquals(md, result2.get(0));
        assertEquals(md2, result2.get(1));
    }

    // addDataConsumer() -----------------------------------------------------------------------------------------------

    @Test
    public void addDataConsumer() throws Exception {

        ConfigurationBase cb = (ConfigurationBase)getConfigurationToTest(true, null);

        List<DataConsumer> dcs = cb.getDataConsumers();
        assertTrue(dcs.isEmpty());

        MockDataConsumer mdc = new MockDataConsumer();

        cb.addDataConsumer(mdc);

        dcs = cb.getDataConsumers();

        assertEquals(1, dcs.size());
        assertEquals(mdc, dcs.get(0));

        MockDataConsumer mdc2 = new MockDataConsumer();

        cb.addDataConsumer(mdc2);

        dcs = cb.getDataConsumers();

        assertEquals(2, dcs.size());
        assertEquals(mdc, dcs.get(0));
        assertEquals(mdc2, dcs.get(1));
    }

    // consumers in configuration --------------------------------------------------------------------------------------

    @Test
    public void outputAndConsumersInConfiguration() throws Exception {

        String cf = getConfigurationFileName("output-and-consumers");

        assertTrue(new File(cf).isFile());

        Configuration c = getConfigurationToTest(true, cf);

        List<DataConsumer> dcs = c.getDataConsumers();

        assertEquals(3, dcs.size());

        // "output" is always on the first position in line
        DataConsumer output = dcs.get(0);
        assertTrue(output instanceof AsynchronousCsvLineWriter);

        MockDataConsumer mdc = (MockDataConsumer)dcs.get(1);
        assertNotNull(mdc);

        MockDataConsumer mdc2 = (MockDataConsumer)dcs.get(2);
        assertNotNull(mdc2);

        assertNotEquals(mdc, mdc2);
    }

    @Test
    public void consumersAndOutputInConfiguration() throws Exception {

        String cf = getConfigurationFileName("consumers-and-output");

        assertTrue(new File(cf).isFile());

        Configuration c = getConfigurationToTest(true, cf);

        List<DataConsumer> dcs = c.getDataConsumers();

        assertEquals(3, dcs.size());

        // "output" is always on the first position in line
        DataConsumer output = dcs.get(0);
        assertTrue(output instanceof AsynchronousCsvLineWriter);

        MockDataConsumer mdc = (MockDataConsumer)dcs.get(1);
        assertNotNull(mdc);

        MockDataConsumer mdc2 = (MockDataConsumer)dcs.get(2);
        assertNotNull(mdc2);

        assertNotEquals(mdc, mdc2);
    }

    @Test
    public void onlyConsumersInConfiguration() throws Exception {

        String cf = getConfigurationFileName("only-consumers");

        assertTrue(new File(cf).isFile());

        Configuration c = getConfigurationToTest(true, cf);

        List<DataConsumer> dcs = c.getDataConsumers();

        assertEquals(2, dcs.size());
        MockDataConsumer mdc = (MockDataConsumer)dcs.get(0);
        assertNotNull(mdc);

        MockDataConsumer mdc2 = (MockDataConsumer)dcs.get(1);
        assertNotNull(mdc2);

        assertNotEquals(mdc, mdc2);
    }

    // addMetricSource() -----------------------------------------------------------------------------------------------

    @Test
    public void addMetricSource() throws Exception {

        ConfigurationBase cb = (ConfigurationBase)getConfigurationToTest(true, null);

        assertEquals(0, cb.getMetricSourceCount());

        AddressImpl a = new AddressImpl("mock");

        cb.addMetricSource(a);

        assertEquals(1, cb.getMetricSourceCount());

        //
        // add the "same" address
        //

        AddressImpl a2 = new AddressImpl("mock");

        cb.addMetricSource(a2);

        assertEquals(1, cb.getMetricSourceCount());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @param fileName null acceptable, will produce a configuration file that exposes defaults.
     */
    protected abstract Configuration getConfigurationToTest(boolean foreground, String fileName) throws Exception;

    protected abstract String getReferenceFileName();

    protected abstract String getConfigurationFileName(String basename);

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
