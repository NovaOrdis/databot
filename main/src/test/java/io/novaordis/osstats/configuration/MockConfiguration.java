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

package io.novaordis.osstats.configuration;

import io.novaordis.events.api.metric.MetricDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class MockConfiguration implements Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean foreground;
    private String outputFileName;
    private boolean outputFileOverwrite;
    private List<MetricDefinition> metrics;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockConfiguration() {

        this.metrics = new ArrayList<>();
    }

    // Configuration implementation ------------------------------------------------------------------------------------

    @Override
    public boolean isForeground() {

        return foreground;
    }

    @Override
    public int getSamplingIntervalSec() {
        throw new RuntimeException("getSamplingIntervalSec() NOT YET IMPLEMENTED");
    }

    @Override
    public String getOutputFileName() {
        return outputFileName;
    }

    @Override
    public boolean isOutputFileAppend() {
        return outputFileOverwrite;
    }

    @Override
    public List<MetricDefinition> getMetricDefinitions() {

        return metrics;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setForeground(boolean b) {
        this.foreground = b;
    }

    public void setOutputFileName(String s) {
        this.outputFileName = s;
    }

    public void setOutputFileAppend(boolean b) {
        this.outputFileOverwrite = b;
    }

    /**
     * The relative order is preserved.
     */
    public void addMetricDefinition(MetricDefinition md) {

        metrics.add(md);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
