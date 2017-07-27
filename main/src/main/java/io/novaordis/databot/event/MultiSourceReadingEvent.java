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

import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.Property;
import io.novaordis.utilities.address.Address;

import java.util.List;

/**
 * The encapsulations of all readings from a metric source, during a single collection run, at a certain moment in time.
 *
 * The timestamp is given by the moment when all properties are successfully deserialized, after reading, at the
 * time when the actual reading has already taken place, to the actual timestamp of the readings is slightly ahead
 * of this event's timestamp.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/26/17
 */
public class MultiSourceReadingEvent extends GenericTimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Address sourceAddress;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * The timestamp is captured at instance initialization.
     */
    public MultiSourceReadingEvent(Address sourceAddress, List<Property> readings) {

        super(System.currentTimeMillis(), readings);

        if (sourceAddress == null) {

            throw new IllegalArgumentException("null source address");
        }

        this.sourceAddress = sourceAddress;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Address getSourceAddress() {

        return sourceAddress;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
