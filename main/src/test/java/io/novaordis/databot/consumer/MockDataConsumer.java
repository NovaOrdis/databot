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

package io.novaordis.databot.consumer;

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.DataConsumerException;
import io.novaordis.events.api.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/11/17
 */
public class MockDataConsumer implements DataConsumer {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MockDataConsumer.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> eventQueue;

    private boolean started;

    // Constructors ----------------------------------------------------------------------------------------------------

    // DataConsumer implementation -------------------------------------------------------------------------------------

    @Override
    public void setEventQueue(BlockingQueue<Event> q) {

        this.eventQueue = q;

        log.info(this + " was configured with an event queue " + q);
    }

    @Override
    public BlockingQueue<Event> getEventQueue() {

        return eventQueue;
    }

    @Override
    public void start() throws DataConsumerException {

        started = true;

        log.info(this + " was started");
    }

    @Override
    public boolean isStarted() {

        return started;
    }

    @Override
    public void stop() {

        started = false;

        log.info(this + " was stopped");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Get and remove the event from the head of the queue of received events (not the main event queue), or return null
     * if no events were not pushed into consumer.
     */
    public Event getEvent() {

        return null;
    }

    @Override
    public String toString() {

        return "MockDataConsumer[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
