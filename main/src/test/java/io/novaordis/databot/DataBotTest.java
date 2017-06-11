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
import io.novaordis.events.api.metric.MetricSource;
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
    public void constructor_and_initialization() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        mc.setEventQueueSize(7);

        DataBot d = new DataBot(mc);

        assertEquals(d.getEventQueueSize(), mc.getEventQueueSize());

        assertFalse(d.isStarted());
    }

    // start() ---------------------------------------------------------------------------------------------------------

    @Test
    public void start() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        DataBot d = new DataBot(mc);

        assertFalse(d.isStarted());

        d.start();

        assertTrue(d.isStarted());

        List<MetricSource> sources = d.getMetricSources();

        //
        // sources are not started at this time, they will be started on the next timer run
        //

        for(MetricSource s: sources) {

            assertFalse(s.isStarted());
        }

    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
