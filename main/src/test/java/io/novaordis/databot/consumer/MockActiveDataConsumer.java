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

import io.novaordis.events.api.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/13/17
 */
public class MockActiveDataConsumer extends ActiveDataConsumerBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MockActiveDataConsumer.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<Event> receivedEvents;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockActiveDataConsumer() {

        this(null);
    }

    protected MockActiveDataConsumer(BlockingQueue<Event> eventQueue) {

        super(eventQueue);

        this.receivedEvents = new ArrayList<>();
    }

    // ActiveDataConsumerBase overrides --------------------------------------------------------------------------------

    @Override
    protected String getName() {

        return "Mock DataConsumer";
    }

    @Override
    protected synchronized void handleEvent(Event event) {

        log.info(this + " got " + event);
        receivedEvents.add(event);
    }

    @Override
    public String toString() {

        return "MockActiveDataConsumer[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Get and remove the event from the head of the queue of received events (not the main event queue), or return null
     * if no events were not pushed into consumer.
     */
    public synchronized Event getEvent() {

        if (receivedEvents.isEmpty()) {

            return null;
        }

        return receivedEvents.remove(0);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
