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
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.OS;
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
     * The metric name, a human readable string, possibly space separated. For example, the /proc/meminfo MemTotal's
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
     * @return a list of source hints, in the descending order of their priority. This is to make the minimal
     * amount of external invocations.
     */
    List<String> getSources(OS os);

}
