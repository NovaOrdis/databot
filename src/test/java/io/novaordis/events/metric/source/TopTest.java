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

package io.novaordis.events.metric.source;

import io.novaordis.events.core.event.FloatProperty;
import io.novaordis.events.core.event.LongProperty;
import io.novaordis.events.core.event.MemoryMeasureUnit;
import io.novaordis.events.core.event.Percentage;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.metric.cpu.CpuHardwareInterruptTime;
import io.novaordis.events.metric.cpu.CpuIdleTime;
import io.novaordis.events.metric.cpu.CpuIoWaitTime;
import io.novaordis.events.metric.cpu.CpuKernelTime;
import io.novaordis.events.metric.cpu.CpuNiceTime;
import io.novaordis.events.metric.cpu.CpuSoftwareInterruptTime;
import io.novaordis.events.metric.cpu.CpuStolenTime;
import io.novaordis.events.metric.cpu.CpuUserTime;
import io.novaordis.events.metric.loadavg.LoadAverageLastFiveMinutes;
import io.novaordis.events.metric.loadavg.LoadAverageLastMinute;
import io.novaordis.events.metric.loadavg.LoadAverageLastTenMinutes;
import io.novaordis.events.metric.memory.PhysicalMemoryFree;
import io.novaordis.events.metric.memory.PhysicalMemoryTotal;
import io.novaordis.events.metric.memory.PhysicalMemoryUsed;
import io.novaordis.events.metric.memory.SwapFree;
import io.novaordis.events.metric.memory.SwapTotal;
import io.novaordis.events.metric.memory.SwapUsed;
import io.novaordis.utilities.Files;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/6/16
 */
public class TopTest extends OSCommandTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void parseLoadAverage() throws Exception {

        List<Property> props = Top.parseLoadAverage(" 1.11, 2.22, 3.33");
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

    // Linux static ----------------------------------------------------------------------------------------------------

    @Test
    public void parseLinuxCommandOutput() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/data/os/top-linux.out");
        assertTrue(f.isFile());
        String content = Files.read(f);

        List<Property> ps = Top.parseLinuxCommandOutput(content);

        assertEquals(3 + 8 + 3 + 3, ps.size());

        int i = 0;
        assertEquals(new LoadAverageLastMinute().getName(), ps.get(i).getName());
        assertEquals(0.11f, (Float)ps.get(i).getValue(), 0.0001);

        i = 1;
        assertEquals(new LoadAverageLastFiveMinutes().getName(), ps.get(i).getName());
        assertEquals(0.22f, (Float)ps.get(i).getValue(), 0.0001);

        i = 2;
        assertEquals(new LoadAverageLastTenMinutes().getName(), ps.get(i).getName());
        assertEquals(0.33f, (Float)ps.get(i).getValue(), 0.0001);

        i = 3;
        assertEquals(new CpuUserTime().getName(), ps.get(i).getName());
        assertEquals(0.0f, (Float)ps.get(i).getValue(), 0.0001);

        i = 4;
        assertEquals(new CpuKernelTime().getName(), ps.get(i).getName());
        assertEquals(0.1f, (Float)ps.get(i).getValue(), 0.0001);

        i = 5;
        assertEquals(new CpuNiceTime().getName(), ps.get(i).getName());
        assertEquals(0.2f, (Float)ps.get(i).getValue(), 0.0001);

        i = 6;
        assertEquals(new CpuIdleTime().getName(), ps.get(i).getName());
        assertEquals(99.8f, (Float)ps.get(i).getValue(), 0.0001);

        i = 7;
        assertEquals(new CpuIoWaitTime().getName(), ps.get(i).getName());
        assertEquals(0.3f, (Float)ps.get(i).getValue(), 0.0001);

        i = 8;
        assertEquals(new CpuHardwareInterruptTime().getName(), ps.get(i).getName());
        assertEquals(0.4f, (Float)ps.get(i).getValue(), 0.0001);

        i = 9;
        assertEquals(new CpuSoftwareInterruptTime().getName(), ps.get(i).getName());
        assertEquals(0.5f, (Float)ps.get(i).getValue(), 0.0001);

        i = 10;
        assertEquals(new CpuStolenTime().getName(), ps.get(i).getName());
        assertEquals(0.6f, (Float)ps.get(i).getValue(), 0.0001);

        i = 11;
        assertEquals(new PhysicalMemoryTotal().getName(), ps.get(i).getName());
        assertEquals(1040326656L, ((Long) ps.get(i).getValue()).longValue());

        i = 12;
        assertEquals(new PhysicalMemoryFree().getName(), ps.get(i).getName());
        assertEquals(821522432L, ((Long) ps.get(i).getValue()).longValue());

        i = 13;
        assertEquals(new PhysicalMemoryUsed().getName(), ps.get(i).getName());
        assertEquals(88944640L, ((Long) ps.get(i).getValue()).longValue());

        i = 14;
        assertEquals(new SwapTotal().getName(), ps.get(i).getName());
        assertEquals(3072L, ((Long) ps.get(i).getValue()).longValue());

        i = 15;
        assertEquals(new SwapFree().getName(), ps.get(i).getName());
        assertEquals(2048L, ((Long) ps.get(i).getValue()).longValue());

        i = 16;
        assertEquals(new SwapUsed().getName(), ps.get(i).getName());
        assertEquals(1024L, ((Long) ps.get(i).getValue()).longValue());
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
                "  100 total,        80 free,        20 used.   791404 avail Mem");

        assertEquals(3, props.size());

        SwapTotal metric = new SwapTotal();
        LongProperty p = (LongProperty)props.get(0);
        assertEquals(MemoryMeasureUnit.BYTE, p.getMeasureUnit());
        assertEquals(metric.getName(), p.getName());
        assertEquals(100L * 1024, p.getLong().longValue());
        assertEquals(Long.class, p.getType());

        SwapFree metric2 = new SwapFree();
        LongProperty p2 = (LongProperty)props.get(1);
        assertEquals(MemoryMeasureUnit.BYTE, p2.getMeasureUnit());
        assertEquals(metric2.getName(), p2.getName());
        assertEquals(80L * 1024, p2.getLong().longValue());
        assertEquals(Long.class, p2.getType());

        SwapUsed metric3 = new SwapUsed();
        LongProperty p3 = (LongProperty)props.get(2);
        assertEquals(MemoryMeasureUnit.BYTE, p3.getMeasureUnit());
        assertEquals(metric3.getName(), p3.getName());
        assertEquals(20L * 1024, p3.getLong().longValue());
        assertEquals(Long.class, p3.getType());
    }

    // Mac static ------------------------------------------------------------------------------------------------------

    @Test
    public void parseMacCommandOutput() throws Exception {

        File f = new File(System.getProperty("basedir"), "src/test/resources/data/os/top-mac.out");
        assertTrue(f.isFile());
        String content = Files.read(f);

        List<Property> ps = Top.parseMacCommandOutput(content);

        assertEquals(3 + 3 + 2, ps.size());

        int i = 0;
        assertEquals(new LoadAverageLastMinute().getName(), ps.get(i).getName());
        assertEquals(1.57f, (Float)ps.get(i).getValue(), 0.0001);

        i = 1;
        assertEquals(new LoadAverageLastFiveMinutes().getName(), ps.get(i).getName());
        assertEquals(1.59f, (Float)ps.get(i).getValue(), 0.0001);

        i = 2;
        assertEquals(new LoadAverageLastTenMinutes().getName(), ps.get(i).getName());
        assertEquals(1.69f, (Float)ps.get(i).getValue(), 0.0001);

        i = 3;
        assertEquals(new CpuUserTime().getName(), ps.get(i).getName());
        assertEquals(2.94f, (Float)ps.get(i).getValue(), 0.0001);

        i = 4;
        assertEquals(new CpuKernelTime().getName(), ps.get(i).getName());
        assertEquals(10.29f, (Float)ps.get(i).getValue(), 0.0001);

        i = 5;
        assertEquals(new CpuIdleTime().getName(), ps.get(i).getName());
        assertEquals(86.76f, (Float)ps.get(i).getValue(), 0.0001);

        i = 6;
        assertEquals(new PhysicalMemoryUsed().getName(), ps.get(i).getName());
        assertEquals(13L * 1024 * 1024 * 1024, ((Long) ps.get(i).getValue()).longValue());

        i = 7;
        assertEquals(new PhysicalMemoryFree().getName(), ps.get(i).getName());
        assertEquals(2563L * 1024 * 1024, ((Long) ps.get(i).getValue()).longValue());
    }

    @Test
    public void parseMacCpuInfo() throws Exception {

        List<Property> props = Top.parseMacCpuInfo(" 2.94% user, 10.29% sys, 86.76% idle");
        assertEquals(3, props.size());

        CpuUserTime m = new CpuUserTime();
        FloatProperty p = (FloatProperty)props.get(0);
        assertEquals(Percentage.getInstance(), p.getMeasureUnit());
        assertEquals(m.getName(), p.getName());
        assertEquals(2.94f, p.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p.getType());

        CpuKernelTime m2 = new CpuKernelTime();
        FloatProperty p2 = (FloatProperty)props.get(1);
        assertEquals(Percentage.getInstance(), p2.getMeasureUnit());
        assertEquals(m2.getName(), p2.getName());
        assertEquals(10.29f, p2.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p2.getType());

        CpuIdleTime m3 = new CpuIdleTime();
        FloatProperty p3 = (FloatProperty)props.get(2);
        assertEquals(Percentage.getInstance(), p3.getMeasureUnit());
        assertEquals(m3.getName(), p3.getName());
        assertEquals(86.76f, p3.getFloat().floatValue(), 0.00001);
        assertEquals(Float.class, p3.getType());
    }

    @Test
    public void parseMacMemoryInfo() throws Exception {

        List<Property> props = Top.parseMacMemoryInfo("   13G used (1470M wired), 2563M unused.");

        assertEquals(2, props.size());

        PhysicalMemoryUsed m = new PhysicalMemoryUsed();
        LongProperty p = (LongProperty)props.get(0);
        assertEquals(MemoryMeasureUnit.BYTE, p.getMeasureUnit());
        assertEquals(m.getName(), p.getName());
        assertEquals(13L * 1024 * 1024 * 1024, p.getLong().longValue());
        assertEquals(Long.class, p.getType());


        PhysicalMemoryFree m2 = new PhysicalMemoryFree();
        LongProperty p2 = (LongProperty)props.get(1);
        assertEquals(MemoryMeasureUnit.BYTE, p2.getMeasureUnit());
        assertEquals(m2.getName(), p2.getName());
        assertEquals(2563L * 1024 * 1024, p2.getLong().longValue());
        assertEquals(Long.class, p2.getType());
    }

    // accessors -------------------------------------------------------------------------------------------------------

    @Test
    public void accessors() throws Exception {

        Top top = new Top("something something else");

        assertEquals("top", top.getCommand());
        assertEquals("something something else", top.getArguments());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected Top getMetricSourceToTest(String arguments) throws Exception {

        return new Top(arguments);
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
