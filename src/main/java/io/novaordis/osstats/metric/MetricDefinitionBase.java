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

package io.novaordis.osstats.metric;

import io.novaordis.utilities.os.OS;

import java.util.Collections;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public abstract class MetricDefinitionBase implements MetricDefinition {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // MetricDefinition implementation ---------------------------------------------------------------------------------

    @Override
    public List<String> getSources(OS os) {

        if (os == null) {
            throw new IllegalArgumentException("null os");
        }

        if ("MacOS".equals(os.getName())) {

            return Collections.singletonList("top");
        }
        else if ("Linux".equals(os.getName())) {

            return Collections.singletonList("top");
        }
        else {
            throw new IllegalArgumentException("unknown operating system " + os);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return getName();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
