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
import io.novaordis.osstats.metric.source.MetricSource;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public interface MetricDefinition {

    // Constants -------------------------------------------------------------------------------------------------------

    Logger log = LoggerFactory.getLogger(MetricDefinition.class);

    // Static ----------------------------------------------------------------------------------------------------------

    static MetricDefinition getInstance(String s) throws UserErrorException {

        //
        // TODO naive implementation, come up with something better
        //

        String[] packages = {

                "io.novaordis.osstats.metric.cpu",
                "io.novaordis.osstats.metric.memory",
                "io.novaordis.osstats.metric.loadavg",
        };

        String fqcn = null;
        Class c = null;

        for(String p : packages) {

            fqcn = p + "." + s;

            try {

                c = Class.forName(fqcn);
                if (c != null) {
                    break;
                }
            }
            catch (Exception e) {

                log.debug("no such metric implementation: " + fqcn);
            }
        }

        if (c == null) {
            throw new UserErrorException("unknown metric " + s);
        }

        try {
            return (MetricDefinition)c.newInstance();
        }
        catch(Exception e) {

            throw new UserErrorException(fqcn + " exists, but it cannot be instantiated", e);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * The metric name, a human readable string, possibly space separated. For example, the /proc/meminfo PhysicalMemoryTotal's
     * name is "Total Memory".
     */
    String getName();

    /**
     * May return null if the metric is non-dimensional (for example load average).
     */
    MeasureUnit getMeasureUnit();

    /**
     * The human readable text that explains what this metric represents
     */
    String getDescription();

    /**
     * The types for values corresponding to this metric definition. Typical: Integer, Long, Double.
     */
    Class getType();

    /**
     * @return a list of sources for this metric, in the descending order of their priority. The data collection layer
     * will use this information to minimize the number of native calls or the number of file reads: if all required
     * metrics can be obtained from a common source, only run that specific native command (or read that file).
     *
     * If no sources for the specified OS instance exist, the method will return an empty list, never null.
     *
     * @param osName one of OS.Linux, OS.MacOS, OS.Windows
     */
    List<MetricSource> getSources(String osName);

    /**
     * Add a source for this metric. Subsequent additions establish priority: the first added source (for a specific
     * os) takes precedence over the second added source, for the same os, etc. If a source is already present, it won't
     * be added and the method will return false.
     *
     * @param osName one of OS.Linux, OS.MacOS, OS.Windows
     *
     * @return true if the source was indeed added (no duplicate found)
     */
    boolean addSource(String osName, MetricSource source);

}
