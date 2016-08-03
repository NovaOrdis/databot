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

package io.novaordis.osstats.metric;

import io.novaordis.events.core.event.MeasureUnit;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public interface MetricDefinition {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * The metric name, the shortest possible string that designates this metric in a conventional context. For example
     * when we are describing a Linux system memory status, we are talking about MemTotal which is defined in
     * /proc/meminfo. By default, it should be the simple name of the class implementing the metric.
     */
    String getName();

    MeasureUnit getMeasureUnit();

    /**
     * The human readable text that explains what this metric represents
     */
    String getDescription();

    /**
     * The types for values corresponding to this metric definition. Typical: Integer, Long, Double.
     */
    Class getType();

}
