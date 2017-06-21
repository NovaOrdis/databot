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

import io.novaordis.databot.configuration.Configuration;
import io.novaordis.databot.configuration.DefaultConfiguration;
import io.novaordis.databot.configuration.MockConfiguration;
import io.novaordis.databot.consumer.MockDataConsumer;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.events.api.metric.MockAddress;
import io.novaordis.events.api.metric.jboss.JBossController;
import io.novaordis.events.api.metric.jmx.JmxBus;
import io.novaordis.events.api.metric.os.LocalOS;
import io.novaordis.events.api.metric.os.RemoteOS;
import io.novaordis.jboss.cli.model.JBossControllerAddress;
import io.novaordis.jmx.JmxAddress;
import io.novaordis.utilities.address.Address;
import io.novaordis.utilities.address.AddressImpl;
import io.novaordis.utilities.address.LocalOSAddress;
import io.novaordis.utilities.address.OSAddressImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    public void constructorAndInitialization_Defaults() throws Exception {

        Configuration c = new DefaultConfiguration();

        //
        // construct and initialize
        //

        DataBot d = new DataBot(c);

        assertEquals(c.getEventQueueSize(), d.getEventQueueSize());
        assertEquals(0, d.getEventCount());

        assertFalse(d.isStarted());

        Set<MetricSource> sources = d.getMetricSources();
        assertTrue(sources.isEmpty());

        List<DataConsumer> consumers = d.getDataConsumers();
        assertTrue(consumers.isEmpty());

        BlockingQueue<Event> eventQueue = d.getEventQueue();
        assertEquals(c.getEventQueueSize(), eventQueue.remainingCapacity());

        ThreadFactory tf = d.getSourceThreadFactory();
        assertNotNull(tf);

        ExecutorService se = d.getSourceExecutor();
        assertFalse(se.isShutdown());
        assertFalse(se.isTerminated());

        ThreadPoolExecutor sourceExecutor = (ThreadPoolExecutor)d.getSourceExecutor();
        assertEquals(DataBot.DEFAULT_SOURCE_EXECUTOR_CORE_POOL_SIZE, sourceExecutor.getCorePoolSize());
    }

    @Test
    public void constructorAndInitialization_ComplexSimulation() throws Exception {

        int eventQueueSize = 7;

        Set<Address> metricSourceAddresses = new HashSet<>();

        metricSourceAddresses.add(new LocalOSAddress());
        metricSourceAddresses.add(new OSAddressImpl("ssh://mock-remote-ssh-server/"));
        metricSourceAddresses.add(new JmxAddress("jmx://mock-remote-jmx-bus:1000/"));
        metricSourceAddresses.add(new JBossControllerAddress("jbosscli://mock-remote-jboss-controller/"));

        MockDataConsumer mdc = new MockDataConsumer();
        MockDataConsumer mdc2 = new MockDataConsumer();
        List<DataConsumer> dataConsumers = new ArrayList<>();
        dataConsumers.add(mdc);
        dataConsumers.add(mdc2);

        MockConfiguration mc = new MockConfiguration();
        mc.setEventQueueSize(eventQueueSize);
        mc.setMetricSourceAddresses(metricSourceAddresses);
        mc.setDataConsumers(dataConsumers);

        //
        // construct and initialize
        //

        DataBot d = new DataBot(mc);

        assertEquals(eventQueueSize, d.getEventQueueSize());
        assertEquals(0, d.getEventCount());

        assertFalse(d.isStarted());

        Set<MetricSource> sources = d.getMetricSources();

        assertEquals(4, sources.size());

        MetricSource expected;

        expected = new LocalOS();
        assertTrue(sources.contains(expected));

        expected = new RemoteOS("ssh://mock-remote-ssh-server/");
        assertTrue(sources.contains(expected));

        expected = new JmxBus("jmx://mock-remote-jmx-bus:1000/");
        assertTrue(sources.contains(expected));

        expected = new JBossController("jbosscli://mock-remote-jboss-controller/");
        assertTrue(sources.contains(expected));

        List<DataConsumer> consumers = d.getDataConsumers();

        assertEquals(2, consumers.size());

        assertEquals(mdc, consumers.get(0));
        assertFalse(mdc.isStarted());
        assertEquals(mdc2, consumers.get(1));
        assertFalse(mdc2.isStarted());

        ThreadPoolExecutor sourceExecutor = (ThreadPoolExecutor)d.getSourceExecutor();
        assertEquals(metricSourceAddresses.size(), sourceExecutor.getCorePoolSize());
    }

    // lifecycle -------------------------------------------------------------------------------------------------------

    @Test
    public void lifecycle() throws Exception {

        AddressImpl sourceAddress = new AddressImpl("mock-host");

        MockDataConsumer mdc = new MockDataConsumer();

        Set<Address> addresses = new HashSet<>();
        addresses.add(sourceAddress);

        MockConfiguration mc = new MockConfiguration();

        mc.setMetricSourceAddresses(addresses);
        mc.setDataConsumers(Collections.singletonList(mdc));
        mc.setSamplingIntervalSec(1);

        mc.setMetricSourceFactory(new MockMetricSourceFactory());

        DataBot d = new DataBot(mc);

        assertFalse(d.isStarted());

        Set<MetricSource> sources = d.getMetricSources();
        assertEquals(1, sources.size());
        MockMetricSource source = (MockMetricSource)sources.iterator().next();
        assertEquals(sourceAddress, source.getAddress());

        List<DataConsumer> consumers = d.getDataConsumers();
        assertEquals(1, consumers.size());
        assertEquals(mdc, consumers.get(0));
        assertFalse(consumers.get(0).isStarted());
        assertEquals(0, d.getTimerTaskExecutionCount());

        d.start();

        assertTrue(d.isStarted());

        sources = d.getMetricSources();

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
        // wait twice the sampling interval to make sure that the timer task is scheduled as planned
        //

        long waitTimeMs = 1000L * mc.getSamplingIntervalSec();
        long t0 = System.currentTimeMillis();

        while(System.currentTimeMillis() - t0 < waitTimeMs) {

            Thread.sleep(200L);
        }

        long timerTaskExecutionCount = d.getTimerTaskExecutionCount();
        assertTrue(timerTaskExecutionCount > 0);

        d.stop();

        //
        // make sure all is stopped
        //

        //
        // wait twice the sampling interval to make sure that the timer task has time to shut down
        //

        waitTimeMs = 1000L * mc.getSamplingIntervalSec();
        t0 = System.currentTimeMillis();

        while(System.currentTimeMillis() - t0 < waitTimeMs) {

            Thread.sleep(200L);
        }

        ExecutorService sourceExecutor = d.getSourceExecutor();
        assertTrue(sourceExecutor.isShutdown());
        assertTrue(sourceExecutor.isTerminated());
    }

    // lifecycle -------------------------------------------------------------------------------------------------------

    @Test
    public void maximumNumberOfDataCollectionsSet() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        //
        // set the sampling interval to the smallest possible
        //
        mc.setSamplingIntervalSec(1);

        CountDownLatch exitLatch = new CountDownLatch(1);

        DataBot d = new DataBot(mc);

        d.setExitLatch(exitLatch);

        //
        // configure precisely 3 executions
        //

        d.setMaxExecutions(3L);

        d.start();

        //
        // we should be waiting a little bit more than 2 seconds if everything goes well
        //
        long timeoutSecs = 5;

        CountDownLatch exitLatch2 = d.getExitLatch();

        boolean notified = exitLatch2.await(timeoutSecs, TimeUnit.SECONDS);

        if (!notified) {

            fail("the exit latch has not been notified within " + timeoutSecs + " seconds");
        }

        //
        // check databot state
        //

        assertFalse(d.isStarted());

        assertEquals(3, d.getExecutionCount());
        assertEquals(3, d.getSuccessfulExecutionCount());
    }

    // getMetricSource() -----------------------------------------------------------------------------------------------

    @Test
    public void getMetricSource_DoesNotExist() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        DataBot d = new DataBot(mc);

        Address a = new AddressImpl("something");
        assertNull(d.getMetricSource(a));
    }

    @Test
    public void getMetricSource_Exists() throws Exception {

        MockAddress a = new MockAddress("mock");
        MockMetricDefinition md = new MockMetricDefinition(a);
        MockConfiguration mc = new MockConfiguration();
        mc.addMetricDefinition(md);
        mc.setMetricSourceFactory(new MockMetricSourceFactory());

        DataBot d = new DataBot(mc);

        Address a2 = new MockAddress("mock");

        assertEquals(a, a2);

        MetricSource s = d.getMetricSource(a2);

        assertNotNull(s);

        MockMetricSource mms = (MockMetricSource)s;
        assertEquals(a, mms.getAddress());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
