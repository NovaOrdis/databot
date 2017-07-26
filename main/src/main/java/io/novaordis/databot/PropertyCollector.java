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

import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.PropertyFactory;
import io.novaordis.utilities.address.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Package protected utility class that collects Property instances arriving from different sources, and adjust their
 * names.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/25/17
 */
class PropertyCollector {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Set<Address> sourceAddresses;
    private List<Property> properties;

    // Constructors ----------------------------------------------------------------------------------------------------

    PropertyCollector(Set<Address> sourceAddresses) {

        this.sourceAddresses = sourceAddresses;
        this.properties = new ArrayList<>();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * @exception IllegalArgumentException if the given address is not among the initial set of addresses this instance
     * as initialized with.
     */
    void add(Address a, List<Property> props) {

        if (!sourceAddresses.contains(a)) {

            throw new IllegalArgumentException("unknown source address: " + a);

        }

        if (sourceAddresses.size() == 1) {

            //
            // no need to qualify
            //

            this.properties.addAll(props);
        }
        else {

            //
            // need to qualify to avoid name conflicts
            //

            for(Property p: props) {

                Property pCopy = PropertyFactory.createInstance(
                        a.getLiteral() + ":" + p.getName(), p.getType(), p.getValue(), p.getMeasureUnit());

                this.properties.add(pCopy);
            }
        }
    }

    /**
     * @return the number of distinct properties.
     */
    int size() {

        return this.properties.size();
    }

    /**
     * @return all properties in a random order. Returns the underlying storage, this class instances are supposed to
     * be short lived.
     */
    List<Property> getProperties() {

        return properties;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
