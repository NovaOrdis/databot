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

package io.novaordis.osstats;

import io.novaordis.events.core.event.BooleanProperty;
import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.ListProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.MapProperty;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.StringProperty;
import io.novaordis.events.core.event.TimedEvent;
import io.novaordis.utilities.time.Timestamp;

import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/30/16
 */
public class MockTimedEvent implements TimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // TimedEvent implementation ---------------------------------------------------------------------------------------

    @Override
    public Long getTime() {
        throw new RuntimeException("getTime() NOT YET IMPLEMENTED");
    }

    @Override
    public Timestamp getTimestamp() {
        throw new RuntimeException("getTimestamp() NOT YET IMPLEMENTED");
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {
        throw new RuntimeException("setTimestamp() NOT YET IMPLEMENTED");
    }

    @Override
    public Set<Property> getProperties() {
        throw new RuntimeException("getProperties() NOT YET IMPLEMENTED");
    }

    @Override
    public Property getProperty(String s) {
        throw new RuntimeException("getProperty() NOT YET IMPLEMENTED");
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
        throw new RuntimeException("setProperty() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
