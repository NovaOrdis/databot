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

import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public interface Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    int DEFAULT_SAMPLING_INTERVAL_SEC = 10;
    String DEFAULT_OUTPUT_FILE_NAME = "/tmp/os-stats.csv";
    boolean DEFAULT_OUTPUT_FILE_APPEND = true;

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return true if the process runs in foreground, and thus getOutputFileName() value is ignored.
     */
    boolean isForeground();

    /**
     * @return the sampling interval, in seconds. If not specified, the default is 10 seconds.
     */
    int getSamplingIntervalSec();

    /**
     * @return the name of the output file. If not specified, the default value is /tmp/os-stats.csv.  Note that if
     * --foreground option is used, the output will forcibly send to /dev/stdout, regardless of the value of the output
     * file.
     */
    String getOutputFileName();

    /**
     * @return true if the process is to append to a file that already exists, or false if to overwrite.
     */

    boolean isOutputFileAppend();

    /**
     * @return the list of metric definitions to collect and log.
     */
    List<MetricDefinition> getMetricDefinitions();

}
