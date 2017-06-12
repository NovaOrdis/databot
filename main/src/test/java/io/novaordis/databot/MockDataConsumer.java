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

import io.novaordis.events.api.event.Event;

import java.util.concurrent.BlockingQueue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/11/17
 */
public class MockDataConsumer implements DataConsumer {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // DataConsumer implementation -------------------------------------------------------------------------------------

    @Override
    public void setEventQueue(BlockingQueue<Event> q) {
        throw new RuntimeException("setEventQueue() NOT YET IMPLEMENTED");
    }

    @Override
    public BlockingQueue<Event> getEventQueue() {
        throw new RuntimeException("getEventQueue() NOT YET IMPLEMENTED");
    }

    @Override
    public void start() throws DataConsumerException {
        throw new RuntimeException("start() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean isStarted() {
        throw new RuntimeException("isStarted() NOT YET IMPLEMENTED");
    }

    @Override
    public void stop() {
        throw new RuntimeException("stop() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}