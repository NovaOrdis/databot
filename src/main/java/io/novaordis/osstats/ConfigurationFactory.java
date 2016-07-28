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

import io.novaordis.utilities.UserErrorException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class ConfigurationFactory {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @throws UserErrorException on user errors.
     */
    public static Configuration buildInstance(String[] cla) throws Exception {

        Configuration configuration = null;

        List<String> commandLineArguments = new ArrayList<>(Arrays.asList(cla));

        for (int i = 0; i < commandLineArguments.size(); i++) {

            String crt = commandLineArguments.get(i);

            if ("test-user-error".equals(crt)) {
                //
                // we do this for testing
                //
                throw new UserErrorException(commandLineArguments.get(i + 1));

            } else if ("test-unexpected-error".equals(crt)) {
                //
                //  we do this for testing
                //
                String exceptionClassName = commandLineArguments.get(i + 1);
                String message = commandLineArguments.get(i + 2);
                Constructor c = Class.forName(exceptionClassName).getConstructor(String.class);
                //noinspection UnnecessaryLocalVariable
                Exception e = (Exception) c.newInstance(message);
                throw e;
            }
        }

        return configuration;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private ConfigurationFactory() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
