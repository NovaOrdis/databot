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

import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.PropertyFactory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/31/16
 */
public class Top {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    public static List<Property> parseCommandOutput(String osName, String output)
            throws InvalidExecutionOutputException {

        if ("Linux".equals(osName)) {
            return parseLinuxCommandOutput(output);
        }
        else if ("MacOS".equals(osName)) {
            return parseMacCommandOutput(output);
        }
        else {
            throw new IllegalArgumentException("unknown operating system " + osName);
        }
    }

    public static List<Property> parseLinuxCommandOutput(String output) throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(output, "\n");
        while(st.hasMoreTokens()) {

            String line = st.nextToken();

            int i = line.indexOf("load average:");
            if (i != -1) {

                List<Property> loadAverage = parseLinuxLoadAverage(line.substring(i + "load average:".length()));
                result.addAll(loadAverage);
            }
            else if (line.matches("^%Cpu.+:")) {
                i = line.indexOf(":");
                List<Property> loadAverage = parseLinuxCpuInfo(line.substring(i + 1));
                result.addAll(loadAverage);
            }
            else if (line.matches("Mem +:")) {
                i = line.indexOf(":");
                List<Property> loadAverage = parseLinuxMemoryInfo(line.substring(i + 1));
                result.addAll(loadAverage);
            }
            else if (line.matches("Swap +:")) {
                i = line.indexOf(":");
                List<Property> loadAverage = parseLinuxSwapInfo(line.substring(i + 1));
                result.addAll(loadAverage);
            }
        }
        return result;
    }

    public static List<Property> parseMacCommandOutput(String output) throws InvalidExecutionOutputException {

        throw new RuntimeException("parseMacCommandOutput() NOT YET IMPLEMENTED");
    }

    public static List<Property> parseLinuxLoadAverage(String s) throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(s, ", ");

        if (st.hasMoreTokens()) {
            LoadAverageLastMinute m = new LoadAverageLastMinute();
            String tok = st.nextToken();
            Property p = PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit());
            result.add(p);
        }

        if (st.hasMoreTokens()) {
            LoadAverageLastFiveMinutes m = new LoadAverageLastFiveMinutes();
            String tok = st.nextToken();
            Property p = PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit());
            result.add(p);
        }

        if (st.hasMoreTokens()) {
            LoadAverageLastTenMinutes m = new LoadAverageLastTenMinutes();
            String tok = st.nextToken();
            Property p = PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit());
            result.add(p);
        }
        return result;
    }

    public static List<Property> parseLinuxCpuInfo(String s) throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(s, ",");

        while(st.hasMoreTokens()) {

            String tok = st.nextToken();
            int i;
            if ((i = tok.indexOf("us")) != -1) {
                CpuUserTime m = new CpuUserTime();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("sy")) != -1) {
                CpuKernelTime m = new CpuKernelTime();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("ni")) != -1) {
                CpuNiceTime m = new CpuNiceTime();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("id")) != -1) {
                CpuIdleTime m = new CpuIdleTime();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("wa")) != -1) {
                CpuIoWaitTime m = new CpuIoWaitTime();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("hi")) != -1) {
                CpuHardwareInterruptTime m = new CpuHardwareInterruptTime();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("si")) != -1) {
                CpuSoftwareInterruptTime m = new CpuSoftwareInterruptTime();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("st")) != -1) {
                CpuStolenTime m = new CpuStolenTime();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
        }

        return result;
    }

    /**
     * Parses "1015944 total,   802268 free,    86860 used,   126816 buff/cache" (line usually starts with  KiB Mem :"
     */
    public static List<Property> parseLinuxMemoryInfo(String s) throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(s, ",");

        while(st.hasMoreTokens()) {

            //            1015944 total,   802268 free,    86860 used,   126816 buff/cache

            String tok = st.nextToken();
            int i;
            if ((i = tok.indexOf("total")) != -1) {
                PhysicalMemoryTotal m = new PhysicalMemoryTotal();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, 1024, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("free")) != -1) {
                PhysicalMemoryFree m = new PhysicalMemoryFree();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, 1024, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("used")) != -1) {
                PhysicalMemoryUsed m = new PhysicalMemoryUsed();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, 1024, m.getMeasureUnit()));
            }

            //
            // we're ignoring buff/cache for the time being
            //

        }

        return result;
    }

    /**
     * Parses "10 total,        10 free,        10 used.   791404 avail Mem" (line usually starts with  KiB Swap :"
     */
    public static List<Property> parseLinuxSwapInfo(String s) throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(s, ",");

        while(st.hasMoreTokens()) {

            //  10 total,        10 free,        10 used.   791404 avail Mem

            String tok = st.nextToken();
            int i;
            if ((i = tok.indexOf("total")) != -1) {
                SwapTotal m = new SwapTotal();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, 1024, m.getMeasureUnit()));
            }
        }

        return result;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
