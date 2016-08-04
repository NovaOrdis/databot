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

package io.novaordis.osstats.os;

import io.novaordis.events.core.event.FloatProperty;
import io.novaordis.events.core.event.Percentage;
import io.novaordis.events.core.event.Property;
import io.novaordis.osstats.metric.cpu.CpuKernelTime;
import io.novaordis.osstats.metric.cpu.CpuUserTime;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastFiveMinutes;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastMinute;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastTenMinutes;
import io.novaordis.utilities.Files;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/4/16
 */
public class TopTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

//    @Test
//    public void parseLinuxCommandOutput() throws Exception {
//
//        File f = new File(System.getProperty("basedir"), "src/test/resources/data/os/top-linux.out");
//        assertTrue(f.isFile());
//        String content = Files.read(f);
//
//        List<Property> properties = Top.parseLinuxCommandOutput(content);
//
//        throw new RuntimeException("RETURN HERE");
//    }
//
//    @Test
//    public void parseMacCommandOutput() throws Exception {
//
//        File f = new File(System.getProperty("basedir"), "src/test/resources/data/os/top-mac.out");
//        assertTrue(f.isFile());
//        String content = Files.read(f);
//
//        List<Property> properties = Top.parseMacCommandOutput(content);
//
//        throw new RuntimeException("RETURN HERE");
//    }

    @Test
    public void parseLinuxLoadAverage() throws Exception {

        List<Property> props = Top.parseLinuxLoadAverage("1.11, 2.22, 3.33");
        assertEquals(3, props.size());

        LoadAverageLastMinute metric = new LoadAverageLastMinute();
        FloatProperty p = (FloatProperty)props.get(0);
        assertNull(p.getMeasureUnit());
        assertEquals(metric.getName(), p.getName());
        assertEquals(1.11d, p.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p.getType());

        LoadAverageLastFiveMinutes metric2 = new LoadAverageLastFiveMinutes();
        FloatProperty p2 = (FloatProperty)props.get(1);
        assertNull(p2.getMeasureUnit());
        assertEquals(metric2.getName(), p2.getName());
        assertEquals(2.22d, p2.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p2.getType());

        LoadAverageLastTenMinutes metric3 = new LoadAverageLastTenMinutes();
        FloatProperty p3 = (FloatProperty)props.get(2);
        assertNull(p3.getMeasureUnit());
        assertEquals(metric3.getName(), p3.getName());
        assertEquals(3.33d, p3.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p3.getType());
    }

    @Test
    public void parseLinuxCpuInfo() throws Exception {

        List<Property> props = Top.parseLinuxCpuInfo(
                "  1.1 us,  2.2 sy,  3.3 ni, 44.4 id,  5.5 wa,  6.6 hi,  7.7 si,  8.8 st");

        assertEquals(2, props.size());

        CpuUserTime metric = new CpuUserTime();
        FloatProperty p = (FloatProperty)props.get(0);
        assertEquals(Percentage.getInstance(), p.getMeasureUnit());
        assertEquals(metric.getName(), p.getName());
        assertEquals(1.1f, p.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p.getType());

        CpuKernelTime metric2 = new CpuKernelTime();
        FloatProperty p2 = (FloatProperty)props.get(1);
        assertEquals(Percentage.getInstance(), p2.getMeasureUnit());
        assertEquals(metric2.getName(), p2.getName());
        assertEquals(2.2f, p2.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p2.getType());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
