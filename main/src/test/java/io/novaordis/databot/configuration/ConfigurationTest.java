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

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.MockDataConsumer;
import io.novaordis.databot.MockMetricDefinition;
import io.novaordis.databot.MockMetricSource;
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricSourceRepository;
import io.novaordis.events.api.metric.jboss.JBossCliMetricDefinition;
import io.novaordis.events.api.metric.jboss.JBossController;
import io.novaordis.events.api.metric.jmx.JmxBus;
import io.novaordis.events.api.metric.jmx.JmxMetricDefinition;
import io.novaordis.events.api.metric.os.LocalOS;
import io.novaordis.events.api.metric.os.mdefs.CpuUserTime;
import io.novaordis.events.api.metric.os.mdefs.LoadAverageLastMinute;
import io.novaordis.events.api.metric.os.mdefs.PhysicalMemoryTotal;
import io.novaordis.utilities.UserErrorException;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

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
        // metric sources
        //

        //
        // metric definitions
        //

        List<MetricDefinition> metrics = c.getMetricDefinitions();

        assertEquals(5, metrics.size());

        PhysicalMemoryTotal m = (PhysicalMemoryTotal)metrics.get(0);
        assertNotNull(m);
        assertEquals(new LocalOS(), m.getSource());

        CpuUserTime m2 = (CpuUserTime)metrics.get(1);
        assertNotNull(m2);
        assertEquals(new LocalOS(), m2.getSource());

        LoadAverageLastMinute m3 = (LoadAverageLastMinute)metrics.get(2);
        assertNotNull(m3);
        assertEquals(new LocalOS(), m3.getSource());

        JmxMetricDefinition m4 = (JmxMetricDefinition)metrics.get(3);
        assertNotNull(m4);
        assertEquals("jboss.as:subsystem=messaging,hornetq-server=default,jms-queue=DLQ/messageCount", m4.getLabel());

        JBossCliMetricDefinition m5 = (JBossCliMetricDefinition)metrics.get(4);
        assertNotNull(m5);
        assertEquals("/subsystem=messaging/hornetq-server=default/jms-queue=DLQ/message-count", m5.getLabel());

        MetricSourceRepository mr = c.getMetricSourceRepository();
        assertNotNull(mr);

        Set<LocalOS> localOSes = mr.getSources(LocalOS.class);
        assertEquals(1, localOSes.size());
        assertTrue(localOSes.contains(new LocalOS()));

        Set<JmxBus> jmxBuses = mr.getSources(JmxBus.class);
        assertEquals(1, jmxBuses.size());
        JmxBus jmxBus = jmxBuses.iterator().next();
        assertEquals("admin@localhost:9999", jmxBus.getAddress());

        Set<JBossController> jbossControllers = mr.getSources(JBossController.class);
        assertEquals(1, jbossControllers.size());
        JBossController jbossController = jbossControllers.iterator().next();
        assertEquals("admin@localhost:9999", jbossController.getAddress());


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
        assertTrue(c.getMetricSourceRepository().isEmpty());
        assertTrue(c.getMetricDefinitions().isEmpty());
        assertTrue(c.getDataConsumers().isEmpty());
    }

    // addMetricDefinition() -------------------------------------------------------------------------------------------

    @Test
    public void addMetricDefinition() throws Exception {

        ConfigurationBase c = (ConfigurationBase)getConfigurationToTest(false, null);

        assertTrue(c.getMetricDefinitions().isEmpty());
        assertTrue(c.getMetricSourceRepository().isEmpty());

        MockMetricSource ms = new MockMetricSource();

        MockMetricDefinition md = new MockMetricDefinition(ms, "mock");

        c.addMetricDefinition(md);

        List<MetricDefinition> mds = c.getMetricDefinitions();
        assertEquals(1, mds.size());
        assertTrue(mds.contains(md));

        Set<MockMetricSource> mss = c.getMetricSourceRepository().getSources(MockMetricSource.class);
        assertEquals(1, mss.size());
        assertTrue(mss.contains(ms));

        MockMetricDefinition md2 = new MockMetricDefinition(ms, "mock-2");

        c.addMetricDefinition(md2);

        mds = c.getMetricDefinitions();
        assertEquals(2, mds.size());
        assertEquals(mds.get(0), md);
        assertEquals(mds.get(1), md2);

        mss = c.getMetricSourceRepository().getSources(MockMetricSource.class);
        assertEquals(1, mss.size());
        assertTrue(mss.contains(ms));

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
