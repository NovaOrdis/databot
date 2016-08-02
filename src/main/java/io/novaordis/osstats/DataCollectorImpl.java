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

import io.novaordis.events.core.event.GenericTimedEvent;
import io.novaordis.events.core.event.Property;
import io.novaordis.events.core.event.TimedEvent;
import io.novaordis.osstats.os.InvalidExecutionOutputException;
import io.novaordis.osstats.os.linux.Vmstat;
import io.novaordis.utilities.os.NativeExecutionException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class DataCollectorImpl implements DataCollector {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(DataCollectionTimerTask.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private OS os;

    // Constructors ----------------------------------------------------------------------------------------------------

    public DataCollectorImpl(OS os) {
        this.os = os;
    }

    // DataCollectorImpl implementation --------------------------------------------------------------------------------

    @Override
    public TimedEvent read() {

        long readingBegins = System.currentTimeMillis();
        List<Property> properties = readProperties();
        long readingEnds = System.currentTimeMillis();
        long t = readingBegins + (readingEnds - readingBegins) / 2;

        // It is possible to get an empty property list. This happens when the underlying layer fails to take a
        // reading. The underlying layer warned already, so we just generate an empty event, it'll show up in the
        // data set.

        return new GenericTimedEvent(t, properties);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * @return a list of properties. Order matters, and the event will preserve the order as it is processed downstream.
     * A reading or parsing failure will be logged as warning and an empty property list will be returned.
     */
    List<Property> readProperties() {

        //
        // the current implementation reads the O/S level statistics as provided by Linux vmstat
        //

        String vmstatOutput;

        try {

            NativeExecutionResult result = os.execute("vmstat");

            if (result.isSuccess()) {
                vmstatOutput = result.getStdout();
            } else {
                throw new RuntimeException("readProperties() failure NOT YET IMPLEMENTED");
            }
        }
        catch(NativeExecutionException e) {

            throw new RuntimeException("readProperties() NativeExecutionException NOT YET IMPLEMENTED");
        }

        try {
            //noinspection UnnecessaryLocalVariable
            List<Property> properties = Vmstat.parseCommandOutput(vmstatOutput);
            return properties;
        }
        catch(InvalidExecutionOutputException e) {
            log.warn("failed to parse vmstam output, " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
