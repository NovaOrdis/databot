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
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.MemoryMeasureUnit;
import io.novaordis.events.core.event.Percentage;
import io.novaordis.events.core.event.Property;
import io.novaordis.osstats.metric.cpu.CpuHardwareInterruptTime;
import io.novaordis.osstats.metric.cpu.CpuIdleTime;
import io.novaordis.osstats.metric.cpu.CpuIoWaitTime;
import io.novaordis.osstats.metric.cpu.CpuKernelTime;
import io.novaordis.osstats.metric.cpu.CpuNiceTime;
import io.novaordis.osstats.metric.cpu.CpuSoftwareInterruptTime;
import io.novaordis.osstats.metric.cpu.CpuStolenTime;
import io.novaordis.osstats.metric.cpu.CpuUserTime;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastFiveMinutes;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastMinute;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastTenMinutes;
import io.novaordis.osstats.metric.memory.PhysicalMemoryFree;
import io.novaordis.osstats.metric.memory.PhysicalMemoryTotal;
import io.novaordis.osstats.metric.memory.PhysicalMemoryUsed;
import io.novaordis.osstats.metric.memory.SwapTotal;
import io.novaordis.utilities.Files;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    @Test
    public void parseMacCommandOutput() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/data/os/top-mac.out");
        assertTrue(f.isFile());
        String content = Files.read(f);

        List<Property> properties = Top.parseMacCommandOutput(content);

        assertNotNull(properties);

        fail("return here");
    }

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

        assertEquals(8, props.size());

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

        CpuNiceTime metric3 = new CpuNiceTime();
        FloatProperty p3 = (FloatProperty)props.get(2);
        assertEquals(Percentage.getInstance(), p3.getMeasureUnit());
        assertEquals(metric3.getName(), p3.getName());
        assertEquals(3.3f, p3.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p3.getType());

        CpuIdleTime metric4 = new CpuIdleTime();
        FloatProperty p4 = (FloatProperty)props.get(3);
        assertEquals(Percentage.getInstance(), p4.getMeasureUnit());
        assertEquals(metric4.getName(), p4.getName());
        assertEquals(44.4f, p4.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p4.getType());

        CpuIoWaitTime metric5 = new CpuIoWaitTime();
        FloatProperty p5 = (FloatProperty)props.get(4);
        assertEquals(Percentage.getInstance(), p5.getMeasureUnit());
        assertEquals(metric5.getName(), p5.getName());
        assertEquals(5.5f, p5.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p5.getType());

        CpuHardwareInterruptTime metric6 = new CpuHardwareInterruptTime();
        FloatProperty p6 = (FloatProperty)props.get(5);
        assertEquals(Percentage.getInstance(), p6.getMeasureUnit());
        assertEquals(metric6.getName(), p6.getName());
        assertEquals(6.6f, p6.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p6.getType());

        CpuSoftwareInterruptTime metric7 = new CpuSoftwareInterruptTime();
        FloatProperty p7 = (FloatProperty)props.get(6);
        assertEquals(Percentage.getInstance(), p7.getMeasureUnit());
        assertEquals(metric7.getName(), p7.getName());
        assertEquals(7.7f, p7.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p7.getType());

        CpuStolenTime metric8 = new CpuStolenTime();
        FloatProperty p8 = (FloatProperty)props.get(7);
        assertEquals(Percentage.getInstance(), p8.getMeasureUnit());
        assertEquals(metric8.getName(), p8.getName());
        assertEquals(8.8f, p8.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p8.getType());
    }

    @Test
    public void parseLinuxMemoryInfo() throws Exception {

        List<Property> props = Top.parseLinuxMemoryInfo(
                "  1015944 total,   802268 free,    86860 used,   126816 buff/cache");

        assertEquals(3, props.size());

        PhysicalMemoryTotal metric = new PhysicalMemoryTotal();
        LongProperty p = (LongProperty)props.get(0);
        assertEquals(MemoryMeasureUnit.BYTE, p.getMeasureUnit());
        assertEquals(metric.getName(), p.getName());
        assertEquals(1015944l * 1024, p.getLong().longValue());
        assertEquals(Long.class, p.getType());

        PhysicalMemoryFree metric2 = new PhysicalMemoryFree();
        LongProperty p2 = (LongProperty)props.get(1);
        assertEquals(MemoryMeasureUnit.BYTE, p2.getMeasureUnit());
        assertEquals(metric2.getName(), p2.getName());
        assertEquals(802268L * 1024, p2.getLong().longValue());
        assertEquals(Long.class, p2.getType());

        PhysicalMemoryUsed metric3 = new PhysicalMemoryUsed();
        LongProperty p3 = (LongProperty)props.get(2);
        assertEquals(MemoryMeasureUnit.BYTE, p3.getMeasureUnit());
        assertEquals(metric3.getName(), p3.getName());
        assertEquals(86860L * 1024, p3.getLong().longValue());
        assertEquals(Long.class, p3.getType());
    }

    @Test
    public void parseLinuxSwapInfo() throws Exception {

        List<Property> props = Top.parseLinuxSwapInfo(
                "  10 total,        10 free,        10 used.   791404 avail Mem");

        assertEquals(4, props.size());

        SwapTotal metric = new SwapTotal();
        LongProperty p = (LongProperty)props.get(0);
        assertEquals(MemoryMeasureUnit.BYTE, p.getMeasureUnit());
        assertEquals(metric.getName(), p.getName());
        assertEquals(10L * 1024, p.getLong().longValue());
        assertEquals(Long.class, p.getType());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
