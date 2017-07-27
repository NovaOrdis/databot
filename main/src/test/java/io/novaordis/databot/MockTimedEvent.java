/*
 * Copyright (c) 2016 Nova Ordis LLC
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

import io.novaordis.events.api.event.BooleanProperty;
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.ListProperty;
import io.novaordis.events.api.event.LongProperty;
import io.novaordis.events.api.event.MapProperty;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.events.api.event.TimedEvent;
import io.novaordis.utilities.time.Timestamp;
import io.novaordis.utilities.time.TimestampImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/30/16
 */
public class MockTimedEvent implements TimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Timestamp timestamp;
    private List<Property> properties;


    // Constructors ----------------------------------------------------------------------------------------------------

    public MockTimedEvent() {

        timestamp = new TimestampImpl(System.currentTimeMillis());
        this.properties = new ArrayList<>();
    }

    // TimedEvent implementation ---------------------------------------------------------------------------------------

    @Override
    public Long getTime() {
        return timestamp.getTime();
    }

    @Override
    public Timestamp getTimestamp() {

        return timestamp;
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {
        throw new RuntimeException("setTimestamp() NOT YET IMPLEMENTED");
    }

    @Override
    public Set<Property> getProperties() {
        return new HashSet<>(properties);
    }

    @Override
    public Property getProperty(String s) {

        for(Property p: properties) {
            if (p.getName().equals(s)) {
                return p;
            }
        }

        return null;
    }

    @Override
    public Property getPropertyByKey(Object propertyKey) {
        throw new RuntimeException("getPropertyByKey() NOT YET IMPLEMENTED");
    }

    @Override
    public StringProperty getStringProperty(String s) {
        throw new RuntimeException("getStringProperty() NOT YET IMPLEMENTED");
    }

    @Override
    public LongProperty getLongProperty(String s) {
        throw new RuntimeException("getLongProperty() NOT YET IMPLEMENTED");
    }

    @Override
    public IntegerProperty getIntegerProperty(String s) {
        throw new RuntimeException("getIntegerProperty() NOT YET IMPLEMENTED");
    }

    @Override
    public BooleanProperty getBooleanProperty(String s) {
        throw new RuntimeException("getBooleanProperty() NOT YET IMPLEMENTED");
    }

    @Override
    public MapProperty getMapProperty(String s) {
        throw new RuntimeException("getMapProperty() NOT YET IMPLEMENTED");
    }

    @Override
    public ListProperty getListProperty(String s) {
        throw new RuntimeException("getListProperty() NOT YET IMPLEMENTED");
    }

    @Override
    public Property setProperty(Property property) {

        properties.add(property);
        return null;
    }

    @Override
    public Long getLineNumber() {
        throw new RuntimeException("getLineNumber() NOT YET IMPLEMENTED");
    }

    @Override
    public void setLineNumber(Long lineNumber) {
        throw new RuntimeException("setLineNumber() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
