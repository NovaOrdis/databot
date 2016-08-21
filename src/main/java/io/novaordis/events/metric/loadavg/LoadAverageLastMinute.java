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

package io.novaordis.events.metric.loadavg;

import io.novaordis.events.metric.source.Top;
import io.novaordis.utilities.os.OS;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public class LoadAverageLastMinute extends LoadAverageMetricDefinitionBase implements LoadAverageMetricDefinition {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public LoadAverageLastMinute() {

        addSource(OS.Linux, new Top("-b -n 1 -p 0"));
        addSource(OS.MacOS, new Top("-l 1 -n 0"));
    }

    // LoadAverageMetricDefinition implementation ----------------------------------------------------------------------

    @Override
    public String getDescription() {

        return "CPU and IO utilization during the last minute.";
    }

    @Override
    public String getSimpleLabel() {
        return "Last Minute Load Average";
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
