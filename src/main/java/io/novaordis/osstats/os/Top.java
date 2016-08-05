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
import io.novaordis.osstats.metric.MetricDefinition;
import io.novaordis.osstats.metric.cpu.CpuHardwareInterruptTime;
import io.novaordis.osstats.metric.cpu.CpuIdleTime;
import io.novaordis.osstats.metric.cpu.CpuIoWaitTime;
import io.novaordis.osstats.metric.cpu.CpuKernelTime;
import io.novaordis.osstats.metric.cpu.CpuMetricDefinition;
import io.novaordis.osstats.metric.cpu.CpuNiceTime;
import io.novaordis.osstats.metric.cpu.CpuSoftwareInterruptTime;
import io.novaordis.osstats.metric.cpu.CpuStolenTime;
import io.novaordis.osstats.metric.cpu.CpuUserTime;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastFiveMinutes;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastMinute;
import io.novaordis.osstats.metric.loadavg.LoadAverageLastTenMinutes;
import io.novaordis.osstats.metric.loadavg.LoadAverageMetricDefinition;
import io.novaordis.osstats.metric.memory.PhysicalMemoryFree;
import io.novaordis.osstats.metric.memory.PhysicalMemoryTotal;
import io.novaordis.osstats.metric.memory.PhysicalMemoryUsed;
import io.novaordis.osstats.metric.memory.SwapFree;
import io.novaordis.osstats.metric.memory.SwapTotal;
import io.novaordis.osstats.metric.memory.SwapUsed;

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

                List<Property> loadAverage = parseLoadAverage(line.substring(i + "load average:".length()));
                result.addAll(loadAverage);
            }
            else if (line.matches("^%Cpu.*:.*")) {
                i = line.indexOf(":");
                List<Property> cpu = parseLinuxCpuInfo(line.substring(i + 1));
                result.addAll(cpu);
            }
            else if (line.matches(".*Mem.*:.*")) {
                i = line.indexOf(":");
                List<Property> memory = parseLinuxMemoryInfo(line.substring(i + 1));
                result.addAll(memory);
            }
            else if (line.matches(".*Swap.*:.*")) {
                i = line.indexOf(":");
                List<Property> swap = parseLinuxSwapInfo(line.substring(i + 1));
                result.addAll(swap);
            }
        }
        return result;
    }

    /**
     * Works both on Linux and Mac.
     * @param s - expected format " 1.57, 1.59, 1.69"
     */
    public static List<Property> parseLoadAverage(String s) throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(s, ", ");
        LoadAverageMetricDefinition[] expected = new LoadAverageMetricDefinition[] {
                new LoadAverageLastMinute(),
                new LoadAverageLastFiveMinutes(),
                new LoadAverageLastTenMinutes()
        };

        for(LoadAverageMetricDefinition m: expected) {
            if (st.hasMoreTokens()) {
                String tok = st.nextToken();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
            }
        }
        return result;
    }

    public static List<Property> parseLinuxCpuInfo(String s) throws InvalidExecutionOutputException {

        Object[][] expected = new Object[][] {
                { "us", new CpuUserTime()},
                { "sy",  new CpuKernelTime()},
                { "ni",  new CpuNiceTime()},
                { "id", new CpuIdleTime()},
                { "wa", new CpuIoWaitTime()},
                { "hi", new CpuHardwareInterruptTime()},
                { "si", new CpuSoftwareInterruptTime()},
                { "st", new CpuStolenTime()},
        };

        return parseCpuInfo(s, expected);
    }

    private static List<Property> parseCpuInfo(String s, Object[][] expectedLabelsAndMetrics)
            throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(s, ",");

        while(st.hasMoreTokens()) {
            String tok = st.nextToken();
            for (Object[] e : expectedLabelsAndMetrics) {
                String label = (String) e[0];
                CpuMetricDefinition m = (CpuMetricDefinition) e[1];
                int i = tok.indexOf(label);
                if (i != -1) {
                    tok = tok.substring(0, i).replace("%", "").trim();
                    result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, null, m.getMeasureUnit()));
                }
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
            else if ((i = tok.indexOf("free")) != -1) {
                SwapFree m = new SwapFree();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, 1024, m.getMeasureUnit()));
            }
            else if ((i = tok.indexOf("used")) != -1) {
                SwapUsed m = new SwapUsed();
                tok = tok.substring(0, i).trim();
                result.add(PropertyFactory.createInstance(m.getName(), m.getType(), tok, 1024, m.getMeasureUnit()));
            }

            //
            // we're ignoring avail Mem for the time being
            //
        }

        return result;
    }

    public static List<Property> parseMacCommandOutput(String output) throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(output, "\n");
        while(st.hasMoreTokens()) {

            String line = st.nextToken();

            if (line.startsWith("Load Avg:")) {
                result.addAll(parseLoadAverage(line.substring("Load Avg:".length())));
            }
            else if (line.startsWith("CPU usage:")) {
                result.addAll(parseMacCpuInfo(line.substring("CPU usage:".length())));
            }
            else if (line.startsWith("PhysMem:")) {
                result.addAll(parseMacMemoryInfo(line.substring("PhysMem:".length())));
            }
        }
        return result;
    }

    /**
     * @param s expected format "2.94% user, 10.29% sys, 86.76% idle"
     */
    public static List<Property> parseMacCpuInfo(String s) throws InvalidExecutionOutputException {

        Object[][] expected = new Object[][] {
                { "user", new CpuUserTime()},
                { "sys",  new CpuKernelTime()},
                { "idle", new CpuIdleTime()},
        };
        return parseCpuInfo(s, expected);
    }

    /**
     * Parses "13G used (1470M wired), 2563M unused"
     */
    public static List<Property> parseMacMemoryInfo(String s) throws InvalidExecutionOutputException {

        List<Property> result = new ArrayList<>();
        Object[][] expected = new Object[][] {
                { "used", new PhysicalMemoryUsed()},
                { "unused",  new PhysicalMemoryFree()},
        };
        for(Object[] e: expected) {
            String label = (String)e[0];
            MetricDefinition m = (MetricDefinition)e[1];
            int i = s.indexOf(" " + label);
            if (i == -1) {
                continue;
            }
            String ms = s.substring(0, i);
            i = ms.lastIndexOf(' ');
            ms = i == -1 ? ms : ms.substring(i);
            int multiplicationFactor;
            if (ms.endsWith("G")) {
                multiplicationFactor = 1024 * 1024 * 1024;
            }
            else if (ms.endsWith("M")) {
                multiplicationFactor = 1024 * 1024;
            }
            else {
                throw new InvalidExecutionOutputException(
                        "not handling yet '" + ms.charAt(ms.length() - 1));
            }
            ms = ms.substring(0, ms.length() - 1).trim();
            result.add(PropertyFactory.createInstance(
                    m.getName(), m.getType(), ms, multiplicationFactor, m.getMeasureUnit()));
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
