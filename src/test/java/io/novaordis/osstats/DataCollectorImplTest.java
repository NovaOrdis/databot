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

import io.novaordis.events.core.event.Property;
import io.novaordis.osstats.os.MockOS;
import io.novaordis.utilities.os.OS;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataCollectorImplTest extends DataCollectorTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void readProperties() throws Exception {

        MockOS mos = new MockOS();

        DataCollectorImpl dc = new DataCollectorImpl(mos);

        List<Property> properties = dc.readProperties();

        assertFalse(properties.isEmpty());
    }

    @Test
    public void readProperties_NativeCallThrowsNativeExecutionException() throws Exception {

        MockOS mos = new MockOS();

        //
        // configure MockOS to throw native execution exception on any command
        //

        mos.breakOnAnyCommand("SYNTHETIC NativeExecutionException message", new RuntimeException("SYNTHETIC RUNTIME"));

        DataCollectorImpl dc = new DataCollectorImpl(mos);

        List<Property> properties = dc.readProperties();

        assertTrue(properties.isEmpty());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected DataCollectorImpl getDataCollectorToTest(OS os) throws Exception {

        return new DataCollectorImpl(os);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
