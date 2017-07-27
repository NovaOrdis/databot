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

import io.novaordis.events.api.event.BooleanProperty;
import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.ListProperty;
import io.novaordis.events.api.event.LongProperty;
import io.novaordis.events.api.event.MapProperty;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.utilities.address.Address;
import io.novaordis.utilities.time.Timestamp;
import io.novaordis.utilities.time.TimestampImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The encapsulations of all metrics from different sources during concurrent runs.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/26/17
 */
public class MultiSourceReadingEvent extends GenericTimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MultiSourceReadingEvent.class);

    public static final SimpleDateFormat TO_STRING_TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private long collectionStartTimestamp;
    private long collectionEndTimestamp;

    // maintains the order of add()
    private List<Address> addresses;
    private Map<Address, List<Property>> properties;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MultiSourceReadingEvent() {

        this.collectionStartTimestamp = System.currentTimeMillis();

        this.addresses = new ArrayList<>();
        this.properties = new HashMap<>();

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
    public Property getPropertyByKey(Object propertyKey) {

        if (propertyKey == null) {

            throw new IllegalArgumentException("null key");
        }

        if (!(propertyKey instanceof MetricDefinition)) {

            log.debug(propertyKey + "(" + propertyKey.getClass() + ") is not a valid key");
            return null;
        }

        MetricDefinition md = (MetricDefinition)propertyKey;
        Address a = md.getMetricSourceAddress();

        if (a == null) {

            return null;
        }

        List<Property> ps = getProperties(a);

        if (ps.isEmpty()) {

            return null;
        }

        String id = md.getId();

        for(Property p: ps) {

            if (p.getName().equals(id)) {

                return p;
            }
        }

        return null;
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {

        //
        // it does not make sense to set timestamp with this type of implementation
        //

        throw new IllegalStateException("timestamp cannot be changed this way for MultiSourceReadingEvents");
    }

    @Override
    public Set<Property> getProperties() {

        Set<Property> result = new HashSet<>();

        //noinspection Convert2streamapi
        for(List<Property> ps: properties.values()) {

            result.addAll(ps);
        }

        return result;
    }

    //
    // TODO 9ys5C3: these methods do not belong to the Event interface.
    //
    // See: https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed
    //

    @Override
    public Property getProperty(String name) {

        log.warn("TODO 9ys5C3 https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed");
        return null;
    }

    @Override
    public StringProperty getStringProperty(String stringPropertyName) {

        throw new IllegalStateException("TODO 9ys5C3 https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed");
    }

    @Override
    public LongProperty getLongProperty(String longPropertyName) {

        throw new IllegalStateException("TODO 9ys5C3 https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed");
    }

    @Override
    public IntegerProperty getIntegerProperty(String integerPropertyName) {

        throw new IllegalStateException("TODO 9ys5C3 https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed");
    }

    @Override
    public BooleanProperty getBooleanProperty(String booleanPropertyName){

        throw new IllegalStateException("TODO 9ys5C3 https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed");
    }

    @Override
    public MapProperty getMapProperty(String mapPropertyName){

        throw new IllegalStateException("TODO 9ys5C3 https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed");
    }

    @Override
    public ListProperty getListProperty(String listPropertyName){

        throw new IllegalStateException("TODO 9ys5C3 https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed");
    }

    @Override
    public Property setProperty(Property property){

        throw new IllegalStateException("TODO 9ys5C3 https://kb.novaordis.com/index.php/Events-api_Concepts#Event_Interface_Refactoring_Needed");
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
     * @param sourceAddress only one source address per event is permitted.
     */
    public void addSourceReading(Address sourceAddress, List<Property> readings) {

        if (addresses.contains(sourceAddress)) {

            throw new IllegalArgumentException("duplicate metric source: " + sourceAddress);
        }

        if (addresses.isEmpty()) {

            //
            // this is the first add, adjust collectionStartTimestamp
            //
            this.collectionStartTimestamp = System.currentTimeMillis();
        }

        this.collectionEndTimestamp = System.currentTimeMillis();
        addresses.add(sourceAddress);
        properties.put(sourceAddress, readings);
    }

    /**
     * @return the total count of properties added so far, across all sources.
     */
    public int getPropertyCount() {

        int count = 0;

        for(List<Property> ps: properties.values()) {

            count += ps.size();
        }

        return count;
    }

    /**
     * Returns the underlying storage, so handle with care.
     *
     * @return the list of source addresses, in the order in which they were added.  May return an empty list, but never
     * null.
     */
    public List<Address> getSourceAddresses() {

        return addresses;
    }

    public int getSourceCount() {

        return addresses.size();
    }

    /**
     * @return the properties for the given source, in the order they were added. May return an empty list, but never
     * null.
     */
    public List<Property> getProperties(Address source) {

        List<Property> ps = properties.get(source);

        if (ps == null) {

            ps = Collections.emptyList();
        }

        return ps;
    }

    @Override
    public String toString() {

        String s = TO_STRING_TIMESTAMP_FORMAT.format(getTime());
        s += " multi-source collection from " + getSourceCount() + " source(s), " + getPropertyCount() + " properties";
        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
