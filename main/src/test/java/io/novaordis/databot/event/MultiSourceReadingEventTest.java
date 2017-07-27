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

package io.novaordis.databot.event;

import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.events.api.metric.MockAddress;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/26/17
 */
public class MultiSourceReadingEventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void identity() throws Exception {

        MockAddress ma = new MockAddress("something");

        List<Property> readings = Arrays.asList(new StringProperty("A", "A value"), new IntegerProperty("B", 2));

        long t0 = System.currentTimeMillis();

        MetricSourceReadingEvent e = new MetricSourceReadingEvent(ma, readings);

        long t1 = System.currentTimeMillis();

        //
        // the timestamp is captured at instance initialization
        //

        long timestamp = e.getTime();
        assertTrue(t0 <= timestamp);
        assertTrue(timestamp <= t1);

        assertEquals(new MockAddress("something"), e.getSourceAddress());

        List<Property> properties = e.getPropertyList();

        assertEquals(2, properties.size());

        Property p = properties.get(0);
        assertEquals("A", p.getName());
        assertEquals("A value", p.getValue());

        Property p2 = properties.get(1);
        assertEquals("B", p2.getName());
        assertEquals(2, p2.getValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
