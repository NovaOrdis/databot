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
public interface DataConsumer {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * The queue to consume events from.
     */
    void setEventQueue(BlockingQueue<Event> q);

    /**
     * The queue to consume events from.
     */
    BlockingQueue<Event> getEventQueue();

    //
    // lifecycle methods -----------------------------------------------------------------------------------------------
    //

    /**
     * @throws DataConsumerException
     * @throws IllegalStateException if the instance was not properly configured before start
     */
    void start() throws DataConsumerException;

    boolean isStarted();

    void stop();

}
