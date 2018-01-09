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

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.novaordis.databot.DataConsumer;
import io.novaordis.databot.DataConsumerException;
import io.novaordis.databot.Util;
import io.novaordis.events.api.event.Event;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/11/17
 */
public abstract class DataConsumerBase implements DataConsumer {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DataConsumerBase.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private BlockingQueue<Event> eventQueue;

    // Constructors ----------------------------------------------------------------------------------------------------

    protected DataConsumerBase() {

        this(null);
    }

    protected DataConsumerBase(BlockingQueue<Event> eventQueue) {

        setEventQueue(eventQueue);
    }

    // DataConsumer implementation -------------------------------------------------------------------------------------

    @Override
    public final void setEventQueue(BlockingQueue<Event> q) {

        if (q == null) {

            if (this.eventQueue != null) {

                log.debug(this + " disconnected from " + Util.queueLogLabel(eventQueue));
            }
        }
        else {

            log.debug(this + " connected to " + Util.queueLogLabel(q));
        }

        this.eventQueue = q;
    }

    @Override
    public final BlockingQueue<Event> getEventQueue() {

        return eventQueue;
    }

    @Override
    public synchronized void start() throws DataConsumerException {

        if (eventQueue == null) {

            throw new DataConsumerException("null event queue, " + this + " was not properly configured before starting");
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
