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

import io.novaordis.events.api.event.EventProperty;
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.events.api.metric.MockAddress;
import io.novaordis.utilities.address.Address;
import io.novaordis.utilities.address.AddressImpl;
import io.novaordis.utilities.time.Timestamp;
import io.novaordis.utilities.time.TimestampImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    // getCollectionStartTimestamp() -----------------------------------------------------------------------------------

    @Test
    public void getCollectionStartTimestamp() throws Exception {

        long t0 = System.currentTimeMillis();

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        long t1 = System.currentTimeMillis();

        assertTrue(t0 <= e.getCollectionStartTimestamp());
        assertTrue(e.getCollectionStartTimestamp() <= t1);
    }

    // getCollectionEndTimestamp() -------------------------------------------------------------------------------------

    @Test
    public void getCollectionEndTimestamp() throws Exception {

        long t0 = System.currentTimeMillis();

        MultiSourceReadingEvent sre = new MultiSourceReadingEvent();

        long t1 = System.currentTimeMillis();

        assertTrue(t0 <= sre.getCollectionEndTimestamp());
        assertTrue(sre.getCollectionEndTimestamp() <= t1);

        long t2 = System.currentTimeMillis();

        sre.addSourceReading(new MockAddress("mock1"), Collections.singletonList(new StringProperty("A", "A value")));

        long t3 = System.currentTimeMillis();

        assertTrue(t2 <= sre.getCollectionEndTimestamp());
        assertTrue(sre.getCollectionEndTimestamp() <= t3);

        Thread.sleep(15L);

        long t4 = System.currentTimeMillis();

        sre.addSourceReading(new MockAddress("mock2"), Collections.singletonList(new StringProperty("B", "B value")));

        long t5 = System.currentTimeMillis();

        assertTrue(t4 <= sre.getCollectionEndTimestamp());
        assertTrue(sre.getCollectionEndTimestamp() <= t5);
    }

    // event timestamp -------------------------------------------------------------------------------------------------

    @Test
    public void getTime_NoDataAdded() {

        long t0 = System.currentTimeMillis();
        MultiSourceReadingEvent sre = new MultiSourceReadingEvent();
        long t1 = System.currentTimeMillis();

        Long t = sre.getTime();
        assertTrue(t0 <= t);
        assertTrue(t <= t1);

        Timestamp timestamp = sre.getTimestamp();

        t = timestamp.getTime();
        assertTrue(t0 <= t);
        assertTrue(t <= t1);
    }

    @Test
    public void setTimestampDisabled() throws Exception {

        MultiSourceReadingEvent msre = new MultiSourceReadingEvent();

        try {

            msre.setTimestamp(new TimestampImpl(1L));
            fail("should have thrown exception");
        }
        catch(IllegalStateException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("timestamp cannot be changed this way for MultiSourceReadingEvents"));
        }
    }

    // addSourceReading() ----------------------------------------------------------------------------------------------

    @Test
    public void addSourceReading_NullAddress() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        try {

            e.addSourceReading(null, Collections.singletonList(new IntegerProperty("B", 2)));
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException ex) {

            String msg = ex.getMessage();
            assertTrue(msg.contains("null source address"));
        }
    }

    @Test
    public void addSourceReading_NullReadingsList() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        try {

            e.addSourceReading(new MockAddress("something"), null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException ex) {

            String msg = ex.getMessage();
            assertTrue(msg.contains("null readings list"));
        }
    }

    @Test
    public void addSourceReading_DuplicateAddress() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        e.addSourceReading(new MockAddress("something"), Collections.singletonList(new IntegerProperty("A", 1)));

        try {

            e.addSourceReading(new MockAddress("something"), Collections.singletonList(new IntegerProperty("B", 2)));
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException ex) {

            String msg = ex.getMessage();
            assertTrue(msg.startsWith("duplicate metric source: "));
            assertTrue(msg.contains("something"));
        }
    }

    @Test
    public void addSourceReading() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        long collectionStarted = e.getCollectionStartTimestamp();

        long time = e.getTime();
        assertEquals(time, e.getCollectionStartTimestamp());

        Timestamp ts = e.getTimestamp();
        assertEquals(time, ts.getTime());

        long sleep = 15L;

        Thread.sleep(sleep);

        e.addSourceReading(new MockAddress("something"), Collections.singletonList(new IntegerProperty("A", 1)));

        List<Address> addresses = e.getSourceAddresses();
        assertEquals(1, addresses.size());
        assertEquals(new MockAddress("something"), addresses.get(0));

        List<Property> properties = e.getPropertiesForSource(new MockAddress("something"));
        assertEquals(1, properties.size());
        Property p = properties.get(0);
        assertEquals("A", p.getName());
        assertEquals(1, p.getValue());

        assertEquals(1, e.getAllPropertiesCount());

        //
        // make sure collection started timestamp was updated on first add()
        //
        assertTrue(e.getCollectionStartTimestamp() - collectionStarted >= sleep);
        collectionStarted = e.getCollectionStartTimestamp();

        long time2 = e.getTime();
        assertEquals(time2, e.getCollectionStartTimestamp());

        Thread.sleep(sleep);

        e.addSourceReading(new MockAddress("something else"), Collections.singletonList(new IntegerProperty("A", 2)));

        List<Address> addresses2 = e.getSourceAddresses();
        assertEquals(2, addresses2.size());
        assertEquals(new MockAddress("something"), addresses2.get(0));
        assertEquals(new MockAddress("something else"), addresses2.get(1));

        List<Property> properties2 = e.getPropertiesForSource(new MockAddress("something"));
        assertEquals(1, properties2.size());
        Property p2 = properties2.get(0);
        assertEquals("A", p2.getName());
        assertEquals(1, p2.getValue());

        List<Property> properties3 = e.getPropertiesForSource(new MockAddress("something else"));
        assertEquals(1, properties3.size());
        Property p3 = properties3.get(0);
        assertEquals("A", p3.getName());
        assertEquals(2, p3.getValue());

        assertEquals(2, e.getAllPropertiesCount());

        //
        // make sure collection started timestamp should not be updated on second add()
        //

        assertEquals(e.getCollectionStartTimestamp(), collectionStarted);

        long time3 = e.getTime();
        assertTrue(time3 > e.getCollectionStartTimestamp());
        assertTrue(time3 < e.getCollectionEndTimestamp());

        assertEquals(time3,
                e.getCollectionStartTimestamp() + (e.getCollectionEndTimestamp() - e.getCollectionStartTimestamp())/2);
    }

    // getProperties() -------------------------------------------------------------------------------------------------

    @Test
    public void getProperties_NoAdds() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        assertTrue(e.getProperties().isEmpty());

        assertEquals(0, e.getAllPropertiesCount());
    }

    /**
     * Make sure getProperties() preserves the original semantics.
     */
    @Test
    public void getProperties() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        e.addSourceReading(new MockAddress("something"), Collections.singletonList(new IntegerProperty("A", 1)));
        e.addSourceReading(new MockAddress("something else"), Collections.singletonList(new IntegerProperty("A", 1)));

        List<Property> properties = e.getProperties();

        assertEquals(2, properties.size());

        EventProperty ep = (EventProperty)properties.get(0);
        assertEquals("mock://something", ep.getName());
        assertEquals("A", ep.getEvent().getProperties().get(0).getName());
        assertEquals(1, ep.getEvent().getProperties().get(0).getValue());

        EventProperty ep2 = (EventProperty)properties.get(1);
        assertEquals("mock://something else", ep2.getName());
        assertEquals("A", ep2.getEvent().getProperties().get(0).getName());
        assertEquals(1, ep2.getEvent().getProperties().get(0).getValue());
    }

    // getProperties() -------------------------------------------------------------------------------------------------

    @Test
    public void getPropertiesForSource_NoAdds() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        assertTrue(e.getPropertiesForSource(new AddressImpl("some-random-source")).isEmpty());
    }

    @Test
    public void getPropertiesForSource() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        e.addSourceReading(new AddressImpl("something"), Collections.singletonList(new IntegerProperty("A", 1)));

        List<Property> properties = e.getPropertiesForSource(new AddressImpl("something"));

        assertEquals(1, properties.size());

        assertEquals("A", properties.get(0).getName());
        assertEquals(1, properties.get(0).getValue());

        e.addSourceReading(new AddressImpl("something else"),
                Arrays.asList(new IntegerProperty("A", 1), new StringProperty("B", "something")));

        List<Property> properties2 = e.getPropertiesForSource(new AddressImpl("something else"));
        assertEquals(2, properties2.size());

        assertEquals("A", properties2.get(0).getName());
        assertEquals(1, properties2.get(0).getValue());
        assertEquals("B", properties2.get(1).getName());
        assertEquals("something", properties2.get(1).getValue());
    }

    // getSourceAddresses() --------------------------------------------------------------------------------------------

    @Test
    public void getSourceAddresses_NoAddresses() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        assertTrue(e.getSourceAddresses().isEmpty());
    }

    @Test
    public void getSourceAddresses_OneAddress() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        Address a = new AddressImpl("something");

        e.addSourceReading(a, Collections.singletonList(new StringProperty("something", "something else")));

        List<Address> addresses = e.getSourceAddresses();

        assertEquals(1, addresses.size());

        Address a2 = addresses.get(0);
        assertEquals(a, a2);
    }

    // getSourceCount() ------------------------------------------------------------------------------------------------

    @Test
    public void getSourceCount_NoAddresses() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        assertEquals(0, e.getSourceCount());
    }

    @Test
    public void getSourceCount_OneAddress() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        Address a = new AddressImpl("something");

        e.addSourceReading(a, Collections.singletonList(new StringProperty("something", "something else")));

        assertEquals(1, e.getSourceCount());
    }

    // getAllPropertiesCount() -----------------------------------------------------------------------------------------

    @Test
    public void getAllPropertiesCount_NoProperties() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        assertEquals(0, e.getAllPropertiesCount());
    }

    @Test
    public void getAllPropertiesCount_OneSource_OneSecondLevelProperty() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        e.addSourceReading(new AddressImpl("something"), Collections.singletonList(new StringProperty("A", "B")));

        assertEquals(1, e.getAllPropertiesCount());
    }

    @Test
    public void getAllPropertiesCount_OneSource_TwoSecondLevelProperty() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        e.addSourceReading(new AddressImpl("something"),
                Arrays.asList(new StringProperty("A", "B"), new IntegerProperty("C", 6)));

        assertEquals(2, e.getAllPropertiesCount());
    }

    @Test
    public void getAllPropertiesCount_TwoSources_OneSecondLevelPropertyEach() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        e.addSourceReading(new AddressImpl("something"), Collections.singletonList(new StringProperty("A", "B")));
        e.addSourceReading(new AddressImpl("something-else"), Collections.singletonList(new StringProperty("A", "B")));

        assertEquals(2, e.getAllPropertiesCount());
    }

    // toString() ------------------------------------------------------------------------------------------------------

    @Test
    public void testToString() throws Exception {

        MultiSourceReadingEvent e = new MultiSourceReadingEvent();

        e.addSourceReading(new MockAddress("something"), Collections.singletonList(new IntegerProperty("A", 1)));
        e.addSourceReading(new MockAddress("something else"), Collections.singletonList(new IntegerProperty("B", 2)));

        String s = e.toString();

        assertTrue(s.contains("multi-source collection from 2 source(s), 2 properties"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
