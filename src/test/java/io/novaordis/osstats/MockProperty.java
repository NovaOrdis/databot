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

import io.novaordis.events.core.event.MeasureUnit;
import io.novaordis.events.core.event.Property;

import java.text.Format;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/31/16
 */
public class MockProperty implements Property {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockProperty() {
        this("MockProperty");
    }

    public MockProperty(String name) {
        this.name = name;
    }

    // Property implementation -----------------------------------------------------------------------------------------

    @Override
    public String getName() {

        return name;
    }

    @Override
    public Object getValue() {
        throw new RuntimeException("getValue() NOT YET IMPLEMENTED");
    }

    @Override
    public void setValue(Object value) {
        throw new RuntimeException("setValue() NOT YET IMPLEMENTED");
    }

    @Override
    public Class getType() {
        throw new RuntimeException("getType() NOT YET IMPLEMENTED");
    }

    @Override
    public MeasureUnit getMeasureUnit() {
        throw new RuntimeException("getMeasureUnit() NOT YET IMPLEMENTED");
    }

    @Override
    public Format getFormat() {
        throw new RuntimeException("getFormat() NOT YET IMPLEMENTED");
    }

    @Override
    public Property fromString(String s) throws IllegalArgumentException {
        throw new RuntimeException("fromString() NOT YET IMPLEMENTED");
    }

    @Override
    public String externalizeValue() {

        return "mock-value";
    }

    @Override
    public String externalizeType() {
        throw new RuntimeException("externalizeType() NOT YET IMPLEMENTED");
    }

    @Override
    public int compareTo(Property o) {
        throw new RuntimeException("compareTo() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setName(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return getName();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
