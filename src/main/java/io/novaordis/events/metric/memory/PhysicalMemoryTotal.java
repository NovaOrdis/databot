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
 * See https://kb.novaordis.com/index.php/Proc-meminfo#MemTotal
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public class PhysicalMemoryTotal extends MemoryMetricDefinitionBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public PhysicalMemoryTotal() {

        addSource(OS.Linux, new Top("-b -n 1 -p 0"));
    }

    // MemoryMetricDefinition implementation ---------------------------------------------------------------------------

    @Override
    public String getSimpleLabel() {
        return "Total Physical Memory";
    }

    @Override
    public String getDescription() {

        return
                "Total amount of usable physical memory, which is the amount of physical memory installed on the " +
                        "system minus a number of reserved bits and the kernel binary code.";
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
