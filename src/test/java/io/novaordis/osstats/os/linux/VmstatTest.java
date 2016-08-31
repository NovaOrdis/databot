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

package io.novaordis.osstats.os.linux;

import io.novaordis.events.core.event.IntegerProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.MemoryMeasureUnit;
import io.novaordis.events.core.event.Property;
import io.novaordis.osstats.os.InvalidExecutionOutputException;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.os.OS;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/31/16
 */
public class VmstatTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(VmstatTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // parseCommandOutput() --------------------------------------------------------------------------------------------

    @Test
    public void parseCommandOutput() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/data/os/vmstat.out");
        assertTrue(f.isFile());
        String content = Files.read(f);

        List<Property> properties = Vmstat.parseCommandOutput(content);

        IntegerProperty ip;

        ip = (IntegerProperty)properties.remove(0);
        assertEquals("Runnable Process Count", ip.getName());
        assertEquals(1, ip.getValue());
        assertNull(ip.getMeasureUnit());

        ip = (IntegerProperty)properties.remove(0);
        assertEquals("Uninterruptible Sleep Process Count", ip.getName());
        assertEquals(2, ip.getValue());
        assertNull(ip.getMeasureUnit());

        LongProperty lp;

        lp = (LongProperty)properties.remove(0);
        assertEquals("Memory Swapped Out to Disk", lp.getName());
        long value = (Long)lp.getValue();
        int multiplicationFactor = OS.getInstance().getConfiguration().getMemoryPageSize();
        assertEquals(3L * multiplicationFactor, value);
        assertEquals(MemoryMeasureUnit.BYTE, lp.getMeasureUnit());

        assertTrue(properties.isEmpty());


//        procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----
//         r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa st
//         1  2      0 778480    700 156424    0    0   122     4   29   64  0  0 98  0  1


    }

    // parseProperty() -------------------------------------------------------------------------------------------------

    @Test
    public void parseProperty_WrongType() throws Exception {

        try {
            Vmstat.parseProperty(
                    "test-header", "unconvertible-value", "test-header", "test-name", Integer.class, 1d, null);
            fail("should throw exception");
        }
        catch(InvalidExecutionOutputException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("cannot convert \"unconvertible-value\" to an integer", msg);
        }
    }

    @Test
    public void parseProperty_ConversionFromString_NullMultiplicationFactor() throws Exception {

        IntegerProperty ip = (IntegerProperty)Vmstat.parseProperty(
                "test-header", "7", "test-header", "test-name", Integer.class, null, null);

        assertEquals("test-name", ip.getName());
        assertEquals(7, ip.getInteger().intValue());
    }

    @Test
    public void parseProperty_ConversionFromString_MultiplicationFactor() throws Exception {

        IntegerProperty ip = (IntegerProperty)Vmstat.parseProperty(
                "test-header", "7", "test-header", "test-name", Integer.class, 10d, null);

        assertEquals("test-name", ip.getName());
        assertEquals(70, ip.getInteger().intValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
