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

package io.novaordis.databot.configuration;

import io.novaordis.databot.DataConsumer;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricSourceDefinition;
import io.novaordis.events.api.metric.MetricSourceFactory;
import io.novaordis.utilities.address.Address;

import java.util.List;

/**
 * The command line/configuration file configuration.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public interface Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    int DEFAULT_SAMPLING_INTERVAL_SEC = 10;

    int DEFAULT_EVENT_QUEUE_SIZE = 1000;

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    //
    // Global Options --------------------------------------------------------------------------------------------------
    //

    /**
     * @return true if the process runs in foreground, and thus getOutputFileName() value is ignored.
     */
    boolean isForeground();

    /**
     * @return the sampling interval, in seconds. If not specified, the default is 10 seconds.
     */
    int getSamplingIntervalSec();

    /**
     * The maximum number of events that can be maintained at one time in the in-memory blocking queue, after which
     * the production is throttled down.
     *
     *
     */
    int getEventQueueSize();

    //
    // Metric Sources --------------------------------------------------------------------------------------------------
    //

    /**
     * May return null, which means that no special metric source factory is configured, and the DataBot instance will
     * use the default, internal factory.
     */
    MetricSourceFactory getMetricSourceFactory();

    /**
     * @return the metric source definition in the order in which they were declared in the external configuration,
     * either explicitely in a "sources" section or equivalent, or implicitly, in-line in the metric definitions. The
     * implementations must guarantee that the returned MetricSourceDefinition represent <b>distinct</b> metric sources,
     * meaning they have distinct addresses.
     */
    List<MetricSourceDefinition> getMetricSourceDefinitions();

    /**
     * @return the number of distinct metric sources declared in the external configuration, either explicitely in a
     * "sources" section or equivalent, or implicitly, in-line in the metric definitions.
     */
    int getMetricSourceCount();

    //
    // Data Consumers --------------------------------------------------------------------------------------------------
    //

    /**
     * @return the data consumers to consume the events. Order is important, the data consumers should be returned in
     * the order in which they were declared in the external configuration.
     */
    List<DataConsumer> getDataConsumers();

    //
    // Metrics ---------------------------------------------------------------------------------------------------------
    //

    /**
     * @return the list of metric definitions to collect and log. Order is important, the metric definitions should be
     * returned in the order in which they were declared in the external configuration.
     */
    List<MetricDefinition> getMetricDefinitions();

    /**
     * @return all definitions associated with the given address, or an empty list if no such definitions exist.
     */
    List<MetricDefinition> getMetricDefinitions(Address a);

}
