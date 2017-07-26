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

import io.novaordis.events.api.event.LongProperty;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.events.api.measure.MemoryMeasureUnit;
import io.novaordis.events.api.metric.MockAddress;
import io.novaordis.utilities.address.Address;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/25/17
 */
public class PropertyCollectorTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void add_OneAddress() throws Exception {

        Set<Address> addresses = new HashSet<>();
        addresses.add(new MockAddress("something"));

        PropertyCollector pc = new PropertyCollector(addresses);

        List<Property> props = Collections.singletonList(new StringProperty("test-name", "test-value"));

        pc.add(new MockAddress("something"), props);

        assertEquals(1, pc.size());
        List<Property> props2 = pc.getProperties();

        assertEquals(1, props2.size());
        Property p = props2.get(0);

        assertEquals("test-name", p.getName());
        assertEquals("test-value", p.getValue());
    }

    @Test
    public void add_UnknownAddress() throws Exception {

        Set<Address> addresses = new HashSet<>();
        addresses.add(new MockAddress("something"));

        PropertyCollector pc = new PropertyCollector(addresses);

        List<Property> props = Collections.singletonList(new StringProperty("test-name", "test-value"));

        try {

            pc.add(new MockAddress("something-else"), props);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("unknown source address: "));
            assertTrue(msg.contains("something-else"));
        }
    }

    @Test
    public void add_TwoAddresses() throws Exception {

        Set<Address> addresses = new HashSet<>();
        addresses.add(new MockAddress("A"));
        addresses.add(new MockAddress("B"));

        PropertyCollector pc = new PropertyCollector(addresses);

        List<Property> in = Collections.singletonList(new StringProperty("test-name", "test-value"));

        pc.add(new MockAddress("A"), in);

        assertEquals(1, pc.size());
        List<Property> props2 = pc.getProperties();

        assertEquals(1, props2.size());
        Property p = props2.get(0);

        assertEquals("A:test-name", p.getName());
        assertEquals("test-value", p.getValue());
        assertEquals(String.class, p.getType());
        assertNull(p.getMeasureUnit());

        LongProperty lp = new LongProperty("test-name", 1L);
        lp.setMeasureUnit(MemoryMeasureUnit.GIGABYTE);
        List<Property> in2 = Collections.singletonList(lp);

        pc.add(new MockAddress("B"), in2);

        assertEquals(2, pc.size());
        List<Property> props3 = pc.getProperties();

        assertEquals(2, props3.size());

        p = props3.get(0);
        assertEquals("A:test-name", p.getName());
        assertEquals("test-value", p.getValue());
        assertEquals(String.class, p.getType());
        assertNull(p.getMeasureUnit());

        Property p2 = props3.get(1);
        assertEquals("B:test-name", p2.getName());
        assertEquals(1L, p2.getValue());
        assertEquals(Long.class, p2.getType());
        assertEquals(MemoryMeasureUnit.GIGABYTE, p2.getMeasureUnit());
    }

    // Tests -----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
