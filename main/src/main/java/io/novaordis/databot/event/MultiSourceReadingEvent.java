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

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.EventProperty;
import io.novaordis.events.api.event.GenericEvent;
import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.Property;
import io.novaordis.utilities.address.Address;
import io.novaordis.utilities.address.AddressException;
import io.novaordis.utilities.address.AddressImpl;
import io.novaordis.utilities.time.Timestamp;
import io.novaordis.utilities.time.TimestampImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The encapsulations of all metrics from different sources during concurrent runs.
 *
 * It is implemented as a hierarchical event, where the top level event contains the calculated "read timestamp" and
 * second-level events, each of the second-level event corresponding to a source reading. The second-level events
 * are keyed on the source address' literal. The metrics reads from the source are available as properties of the
 * corresponding second-level event.
 *
 * Note that all Event interface keep their original semantics: they will return EventProperties, and not the second
 * level properties. The users of this class must be aware of this and interpret.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/26/17
 */
public class MultiSourceReadingEvent extends GenericTimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final SimpleDateFormat TO_STRING_TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private long collectionStartTimestamp;

    private long collectionEndTimestamp;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MultiSourceReadingEvent() {

        this.collectionStartTimestamp = System.currentTimeMillis();

        // if there are no readings, collection started and ended at the same time
        this.collectionEndTimestamp = collectionStartTimestamp;
    }

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    public Timestamp getTimestamp() {

        return new TimestampImpl(getTime());
    }

    @Override
    public Long getTime() {

        //
        // an average between the first collection and last collection
        //

        return collectionStartTimestamp + (collectionEndTimestamp - collectionStartTimestamp)/2;
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {

        //
        // it does not make sense to set timestamp with this type of implementation
        //

        throw new IllegalStateException("timestamp cannot be changed this way for MultiSourceReadingEvents");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the timestamp of the collection start - this is when the data runs are initiated on sources. The
     * default value is given by the event creation timestamp.
     */
    public long getCollectionStartTimestamp() {

        return collectionStartTimestamp;
    }

    /**
     * @return the timestamp of the collection end - this is when all data was returned from sources and stored in
     * the event. This is updated automatically by addSourceReading(). Null means no source reading was yet recorded.
     */
    public Long getCollectionEndTimestamp() {

        return collectionEndTimestamp;
    }

    /**
     * Specialized mutator that converts a metric source reading into an event property.
     *
     * @param sourceAddress only one source address per event is permitted.
     */
    public void addSourceReading(Address sourceAddress, List<Property> readings) {

        if (sourceAddress == null) {

            throw new IllegalArgumentException("null source address");
        }

        if (readings == null) {

            throw new IllegalArgumentException("null readings list");
        }

        String addressLiteral = sourceAddress.getLiteral();

        if (getEventProperty(addressLiteral) != null) {

            throw new IllegalArgumentException("duplicate metric source: " + sourceAddress);
        }

        if (getProperties(Event.class).isEmpty()) {

            //
            // no sub-events are present, this is the first add, adjust collectionStartTimestamp
            //
            this.collectionStartTimestamp = System.currentTimeMillis();
        }

        this.collectionEndTimestamp = System.currentTimeMillis();

        //
        // add the source reading as a new event
        //

        GenericEvent sourceReading = new GenericEvent(readings);
        setEventProperty(addressLiteral, sourceReading);
    }

    /**
     * @return the total count of second-level properties added so far, across all sources. It only counts the
     * second-level properties, not the top level event properties.
     */
    public int getAllPropertiesCount() {

        int count = 0;

        for(Property p: getProperties(Event.class)) {

            EventProperty ep = (EventProperty)p;

            Event reading = ep.getEvent();

            if (reading != null) {

                count += reading.getProperties().size();
            }
        }

        return count;
    }

    /**
     * @return the list of source addresses, in the order in which they were added.  May return an empty list, but never
     * null.
     */
    public List<Address> getSourceAddresses() {

        List<Property> eventProperties = getProperties(Event.class);

        if (eventProperties.isEmpty()) {

            return Collections.emptyList();
        }

        List<Address> result = new ArrayList<>();

        for(Property p: eventProperties) {

            EventProperty ep = (EventProperty)p;

            String addressLiteral = ep.getName();

            try {

                result.add(new AddressImpl(addressLiteral));
            }
            catch (AddressException e) {

                //
                // this should not happen, as we got a valid address. Will happen if the conversion from literal
                // to address fails, which indicates a programming error
                //

                throw new IllegalStateException(e);
            }
        }

        return result;
    }

    public int getSourceCount() {

        return getProperties(Event.class).size();
    }

    /**
     * @return the properties for the given source, in the order they were added. May return an empty list, but never
     * null.
     */
    public List<Property> getPropertiesForSource(Address source) {

        for(Property p: getProperties(Event.class)) {

            EventProperty ep = (EventProperty)p;

            String address = ep.getName();

            if (source.getLiteral().equals(address)) {

                return ep.getEvent().getProperties();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String toString() {

        String s = TO_STRING_TIMESTAMP_FORMAT.format(getTime());
        s += " multi-source collection from " + getSourceCount() + " source(s), " + getAllPropertiesCount() + " properties";
        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
