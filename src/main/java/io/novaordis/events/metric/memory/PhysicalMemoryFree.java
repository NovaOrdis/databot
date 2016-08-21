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

package io.novaordis.events.metric.memory;

import io.novaordis.events.metric.source.Top;
import io.novaordis.utilities.os.OS;

/**
 * See https://kb.novaordis.com/index.php/Proc-meminfo#MemFree
 *
 * https://kb.novaordis.com/index.php/Proc-meminfo#Physical_Memory_Used_by_Processes
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public class PhysicalMemoryFree extends MemoryMetricDefinitionBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public PhysicalMemoryFree() {

        addSource(OS.Linux, new Top("-b -n 1 -p 0"));
        addSource(OS.MacOS, new Top("-l 1 -n 0"));
    }

    // MemoryMetricDefinition implementation ---------------------------------------------------------------------------

    @Override
    public String getSimpleLabel() {
        return "Free Physical Memory";
    }

    @Override
    public String getDescription() {

        return "The amount of physical memory left unused by the system.";
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
