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

package io.novaordis.osstats.metric.memory;

import io.novaordis.osstats.metric.MetricDefinition;
import io.novaordis.osstats.metric.source.MetricSource;
import io.novaordis.osstats.metric.source.OSCommand;
import io.novaordis.utilities.os.OS;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public class PhysicalMemoryTotalTest extends MemoryMetricDefinitionTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // getInstance() ---------------------------------------------------------------------------------------------------

    @Test
    public void getInstance() throws Exception {

        PhysicalMemoryTotal m = (PhysicalMemoryTotal) MetricDefinition.getInstance("PhysicalMemoryTotal");
        assertNotNull(m);
    }

    @Test
    public void getName() throws Exception {

        PhysicalMemoryTotal m = new PhysicalMemoryTotal();
        assertEquals("Total Physical Memory", m.getName());
    }

    // sources ---------------------------------------------------------------------------------------------------------

    @Test
    public void sourcesLinux() throws Exception {

        PhysicalMemoryTotal m = getMetricDefinitionToTest();

        List<MetricSource> linuxSources = m.getSources(OS.Linux);
        assertEquals(1, linuxSources.size());

        OSCommand c = (OSCommand) linuxSources.get(0);
        assertEquals("top", c.getCommand());
    }

    @Test
    public void sourcesMac() throws Exception {

        PhysicalMemoryTotal m = getMetricDefinitionToTest();

        List<MetricSource> macSources = m.getSources(OS.MacOS);

        // we don't know yet, this will change
        assertTrue(macSources.isEmpty());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected PhysicalMemoryTotal getMetricDefinitionToTest() throws Exception {
        return new PhysicalMemoryTotal();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}