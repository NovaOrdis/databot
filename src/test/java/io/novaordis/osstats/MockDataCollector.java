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

import io.novaordis.events.core.event.TimedEvent;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/30/16
 */
public class MockDataCollector implements DataCollector {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean broken;

    // Constructors ----------------------------------------------------------------------------------------------------

    // DataCollector implementation ------------------------------------------------------------------------------------

    @Override
    public TimedEvent read() {

        if (broken) {
            throw new RuntimeException("SYNTHETIC EXCEPTION");
        }

        return new MockTimedEvent();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setBroken(boolean b) {

        this.broken = b;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
