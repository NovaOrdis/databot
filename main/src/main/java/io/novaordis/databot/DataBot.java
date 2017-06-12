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
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.metric.MetricSource;
import io.novaordis.events.api.metric.MetricSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The central instance of a data collector node (DataBot). This instance:
 *
 * 1) Manages the in-memory event queue. The data collection threads will independently read and convert the readings
 *    into events, which will be placed into the queue. The consumers, either local file writers or network forwarders,
 *    will pick up events from the queue and process/forward them.
 *
 * 2) Maintains the source state between readings. Long lived metric sources, for which creating a connection is
 *    expensive, are stored in a "started" state.
 *
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/11/17
 */
public class DataBot {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DataBot.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private final Configuration configuration;

    private final List<MetricSource> sources;

    //
    // the in-memory event queue. The data collection threads will independently read and convert the readings into
    // events, which will be placed into the queue. The consumers, either local file writers or network forwarders,
    // will pick up events from the queue and process/forward them.
    //

    private final BlockingQueue<Event> events;

    private final int eventQueueSize;

    private final List<DataConsumer> consumers;

    private final Timer timer;

    private final DataBotTimerTask timerTask;

    private volatile boolean started;

    // Constructors ----------------------------------------------------------------------------------------------------

    public DataBot(Configuration configuration) throws DataBotException {

        if (configuration == null) {

            throw new IllegalArgumentException("null configuration");
        }

        this.configuration = configuration;

        this.eventQueueSize = configuration.getEventQueueSize();

        this.events = new ArrayBlockingQueue<>(eventQueueSize);

        this.sources = new ArrayList<>();

        this.consumers = new ArrayList<>();

        this.timer = new Timer();

        this.timerTask = new DataBotTimerTask(this);

        this.started = false;

        initialize();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Starts all its consumers and schedules the periodic read timer.
     *
     * Note that the source start is not attempted here. Source start attempt will be initiated on the first timer
     * read, and then subsequently on all next time reads. This is to deal with sources that "die" between readings
     * but then can be resuscitated.
     *
     * @exception DataBotException if a consumer cannot be successfully started.
     */
    public synchronized void start() throws DataBotException {

        //
        // start the consumers - they must start correctly or this instance will end up in an invalid state
        //

        for(DataConsumer dc: consumers) {

            dc.start();
        }

        //
        // start the timer that performs periodic data collections
        //

        timer.scheduleAtFixedRate(timerTask, 0, configuration.getSamplingIntervalSec() * 1000L);

        started = true;

        throw new RuntimeException("NYE");
    }

    public synchronized boolean isStarted() {

        return started;
    }

    public synchronized void stop() {

        throw new RuntimeException("NYE");
    }

    /**
     * @return the underlying storage, so handle with care.
     */
    public List<MetricSource> getMetricSources() {

        return sources;
    }

    /**
     * @return the underlying storage, so handle with care.
     */
    public List<DataConsumer> getDataConsumers() {

        return consumers;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Report the number of events in the queue at the time of the reading.
     */
    int getEventCount() {

        return events.size();
    }

    /**
     * Report the maximum number of events that can be accommodated by the event queue, before starting to block.
     */
    int getEventQueueSize() {

        return eventQueueSize;
    }

    /**
     * @return the actual storage, so handle with care.
     */
    BlockingQueue<Event> getEventQueue() {

        return events;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * Create active instances (sources, consumers) but do not start them.
     */
    void initialize() throws DataConsumerException {

        //
        // get the sources from configuration and copy their references locally
        //

        MetricSourceRepository sourceRepository = configuration.getMetricSourceRepository();

        Set<MetricSource> configSources = sourceRepository.getSources();

        //
        // this is where we may enforce a specific order, if we wanted it
        //

        for(MetricSource s: configSources) {

            log.debug("registering metric source " + s);
            this.sources.add(s);
        }

        //
        // initialize data consumers
        //

        for(DataConsumer c: configuration.getDataConsumers()) {

            c.setEventQueue(events);
            consumers.add(c);

        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
