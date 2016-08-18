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

package io.novaordis.osstats.metric.cpu;

import io.novaordis.osstats.metric.source.Top;
import io.novaordis.utilities.os.OS;

/**
 * See https://kb.novaordis.com/index.php/Vmstat#ni
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public class CpuNiceTime extends CpuMetricDefinitionBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public CpuNiceTime() {

        addSource(OS.Linux, new Top("-b -n 1 -p 0"));
    }

    // CpuMetricDefinition implementation ------------------------------------------------------------------------------

    @Override
    public String getSimpleLabel() {
        return "CPU Nice Time";
    }

    @Override
    public String getDescription() {
        return "Percentage of total CPU time spent running niced user processes.";
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
