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

package io.novaordis.osstats;

import io.novaordis.events.api.measure.MeasureUnit;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricDefinitionBase;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/4/16
 */
abstract class MockMetricDefinitionBase extends MetricDefinitionBase implements MetricDefinition {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // MetricDefinition implementation ---------------------------------------------------------------------------------

    @Override
    public MeasureUnit getMeasureUnit() {
        throw new RuntimeException("getMeasureUnit() NOT YET IMPLEMENTED");
    }

    @Override
    public String getDescription() {
        throw new RuntimeException("getDescription() NOT YET IMPLEMENTED");
    }

    @Override
    public Class getType() {
        throw new RuntimeException("getType() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
