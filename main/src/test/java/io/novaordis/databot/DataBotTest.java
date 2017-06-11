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

package io.novaordis.databot;

import io.novaordis.databot.configuration.MockConfiguration;
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.events.api.metric.MetricSourceRepository;
import io.novaordis.events.api.metric.MetricSourceRepositoryImpl;
import io.novaordis.events.api.metric.jboss.JBossController;
import io.novaordis.events.api.metric.jmx.JmxBus;
import io.novaordis.events.api.metric.os.LocalOS;
import io.novaordis.events.api.metric.os.RemoteOS;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/11/17
 */
public class DataBotTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor_NullConfiguration() throws Exception {

        try {

            new DataBot(null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertEquals("null configuration", msg);
        }
    }

    @Test
    public void constructorAndInitialization() throws Exception {

        int eventQueueSize = 7;

        MetricSourceRepository r = new MetricSourceRepositoryImpl();

        r.add(new LocalOS());
        r.add(new RemoteOS("ssh://mock-remote-ssh-server/"));
        r.add(new JmxBus("jmx://mock-remote-jmx-bus/"));
        r.add(new JBossController("jbosscli://mock-remote-jboss-controller/"));

        MockConfiguration mc = new MockConfiguration();
        mc.setMetricSourceRepository(r);

        mc.setEventQueueSize(eventQueueSize);

        //
        // construct and initialize
        //

        DataBot d = new DataBot(mc);

        assertEquals(eventQueueSize, d.getEventQueueSize());
        assertEquals(0, d.getEventCount());

        assertFalse(d.isStarted());

        List<MetricSource> sources = d.getMetricSources();

        assertEquals(4, sources.size());

        MetricSource expected;

        expected = new LocalOS();
        assertTrue(sources.contains(expected));

        expected = new RemoteOS("ssh://mock-remote-ssh-server/");
        assertTrue(sources.contains(expected));

        expected = new JmxBus("jmx://mock-remote-jmx-bus/");
        assertTrue(sources.contains(expected));

        expected = new JBossController("jbosscli://mock-remote-jboss-controller/");
        assertTrue(sources.contains(expected));

        List<DataConsumer> consumers = d.getDataConsumers();

        assertEquals(1, consumers.size());

        AsynchronousCsvLineWriter writer = (AsynchronousCsvLineWriter)consumers.get(0);

        assertFalse(writer.isStarted());
    }

    // lifecycle -------------------------------------------------------------------------------------------------------

    @Test
    public void lifecycle() throws Exception {

        MockMetricSource ms = new MockMetricSource();

        MetricSourceRepository r = new MetricSourceRepositoryImpl();
        r.add(ms);

        MockConfiguration mc = new MockConfiguration();
        mc.setMetricSourceRepository(r);

        DataBot d = new DataBot(mc);

        assertFalse(d.isStarted());

        List<DataConsumer> consumers = d.getDataConsumers();

        for(DataConsumer c: consumers) {

            assertFalse(c.isStarted());
        }

        d.start();

        assertTrue(d.isStarted());

        List<MetricSource> sources = d.getMetricSources();

        //
        // sources are not started at this time, they will be started on the next timer run
        //

        for(MetricSource s: sources) {

            assertFalse(s.isStarted());
        }

        consumers = d.getDataConsumers();

        for(DataConsumer c: consumers) {

            assertTrue(c.isStarted());
        }

        //
        // wait to make sure that the timer task is scheduled as planned
        //

        fail("insure timer task");

        d.stop();

        //
        // make sure all is stopped
        //

        fail("insure stopped");
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
