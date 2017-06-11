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
import io.novaordis.databot.consumer.AsynchronousCsvLineWriter;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.metric.MetricSource;

import java.util.ArrayList;
import java.util.List;
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

    private final List<DataConsumer> consumers;

    private final Timer timer;

    private final DataBotTimerTask timerTask;

    // Constructors ----------------------------------------------------------------------------------------------------

    public DataBot(Configuration configuration) throws DataBotException {

        if (configuration == null) {

            throw new IllegalArgumentException("null configuration");
        }

        this.configuration = configuration;

        this.events = new ArrayBlockingQueue<>(configuration.getEventQueueSize());

        this.sources = new ArrayList<>();

        this.consumers = new ArrayList<>();

        this.timer = new Timer();

        this.timerTask = new DataBotTimerTask(this);

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
    public void start() throws DataBotException {

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

        throw new RuntimeException("NYE");
    }

    public boolean isStarted() {

        throw new RuntimeException("NYE");
    }

    public List<MetricSource> getMetricSources() {

        return sources;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Report the effective queue size, as read from the queue.
     */
    int getEventQueueSize() {

        return events.size();
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
        // create sources
        //

        //
        // create consumers:
        //

        //
        // the event writer
        //

        AsynchronousCsvLineWriter csvWriter = new AsynchronousCsvLineWriter(events, configuration);

        consumers.add(csvWriter);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
