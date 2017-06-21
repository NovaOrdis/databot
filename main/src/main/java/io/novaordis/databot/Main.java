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

package io.novaordis.databot;

import io.novaordis.databot.configuration.Configuration;
import io.novaordis.databot.configuration.ConfigurationFactory;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.logging.StderrVerboseLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class Main {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // Static ----------------------------------------------------------------------------------------------------------

    public static void main(String[] args) {

        try {

            StderrVerboseLogging.init();

            Configuration conf = ConfigurationFactory.buildInstance(args);

            CountDownLatch exitLatch = new CountDownLatch(1);

            DataBot d = new DataBot(conf);

            d.setExitLatch(exitLatch);

            d.start();

            exitLatch.await();

            log.debug("databot stopped");

        }
        catch(Throwable t) {

            String msg = t.getMessage();

            if (t instanceof UserErrorException) {

                Console.error(msg);
            }
            else {

                //
                // we don't expect this, provide more context
                //

                String details = "internal failure: " + t.getClass().getSimpleName();

                if (msg != null) {

                    details += ": " + msg;
                }

                details += " (consult logs for more details)";
                Console.error(details);
            }
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
