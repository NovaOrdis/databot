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
import org.junit.Test;

import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/11/17
 */
public abstract class DataConsumerTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void start_NoEventQueue() throws Exception {

        DataConsumer c = getDataConsumerToTest(null);

        assertNull(c.getEventQueue());

        try {

            c.start();
            fail("should have thrown exception");
        }
        catch(DataConsumerException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("null event queue"));
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @param events null is acceptable, will return an instance with a null event queue.
     */
    protected abstract DataConsumer getDataConsumerToTest(BlockingQueue<Event> events) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
