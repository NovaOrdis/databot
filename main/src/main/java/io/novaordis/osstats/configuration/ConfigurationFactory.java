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

import io.novaordis.osstats.env.EnvironmentVariableProvider;
import io.novaordis.osstats.env.EnvironmentVariableProviderImpl;
import io.novaordis.utilities.UserErrorException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The class responsible with building the Configuration instance, by choosing the right implementation based on the
 * type of the file presented as configuration file.
 *
 * The class is also responsible for "guessing" the location of the default configuration file, based on the content
 * of the bin.dir system property, if no -c|--configuration= options is provided. It is the responsibility of the
 * shell wrapper to set it; if not set, it is considered user error.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class ConfigurationFactory {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String CONFIGURATION_FILE_SHORT_OPTION = "-c";
    public static final String CONFIGURATION_FILE_LONG_OPTION = "--configuration";

    public static final String FOREGROUND_SHORT_OPTION = "-fg";
    public static final String FOREGROUND_LONG_OPTION = "--foreground";

    // Static Attributes -----------------------------------------------------------------------------------------------

    private static EnvironmentVariableProvider environmentVariableProvider = new EnvironmentVariableProviderImpl();

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * Configuration files are introduced by -c file-name or --configuration=file-name.
     *
     * @throws UserErrorException on user errors.
     */
    public static Configuration buildInstance(String[] cla) throws Exception {

        String configurationFileName = null;

        List<String> commandLineArguments = new ArrayList<>(Arrays.asList(cla));

        boolean foreground = false;

        for (int i = 0; i < commandLineArguments.size(); i++) {

            String crt = commandLineArguments.get(i);

            if (CONFIGURATION_FILE_SHORT_OPTION.equals(crt) ||
                    crt.startsWith(CONFIGURATION_FILE_LONG_OPTION)) {

                if (CONFIGURATION_FILE_SHORT_OPTION.equals(crt)) {

                    if (i == commandLineArguments.size() - 1) {
                        throw new UserErrorException(
                                "a configuration file name should follow " + CONFIGURATION_FILE_SHORT_OPTION);
                    }

                    configurationFileName = commandLineArguments.get(++i);
                }
                else {

                    //
                    // long option
                    //
                    if (!crt.startsWith(CONFIGURATION_FILE_LONG_OPTION + "=")) {
                        throw new UserErrorException(
                                "correct configuration file syntax is --configuration=<file-name>");
                    }

                    configurationFileName = crt.substring((CONFIGURATION_FILE_LONG_OPTION + "=").length());
                }
            }
            else if (FOREGROUND_SHORT_OPTION.endsWith(crt) || FOREGROUND_LONG_OPTION.endsWith(crt)) {

                foreground = true;
            }
            else if ("test-user-error".equals(crt)) {

                //
                // we do this for testing
                //
                throw new UserErrorException(commandLineArguments.get(i + 1));

            }
            else if ("test-unexpected-error".equals(crt)) {

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
            else {
                throw new UserErrorException("unknown command or option " + crt);
            }
        }

        if (configurationFileName == null) {

            throw new UserErrorException("no configuration file specified, use -c|--configuration");
        }

        //noinspection UnnecessaryLocalVariable
        Configuration configuration = buildInstance(configurationFileName, foreground);
        return configuration;
    }

    // Package protected static ----------------------------------------------------------------------------------------

    static void setEnvironmentVariableProvider(EnvironmentVariableProvider p) {

        environmentVariableProvider = p;
    }

    static EnvironmentVariableProvider getEnvironmentVariableProvider() {

        return environmentVariableProvider;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private ConfigurationFactory() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    static Configuration buildInstance(String fileName, boolean foreground) throws Exception {

        //
        // heuristics to choose the right subclass implementation
        //

        if (fileName.endsWith(".conf")) {

            //
            // properties-based configuration file
            //

            return new PropertiesConfigurationFile(fileName, foreground);
        }
        else {

            String s = fileName;
            int i = s.lastIndexOf('/');
            if (i != -1 && i != s.length() - 1) {
                s = fileName.substring(i + 1);
            }
            throw new UserErrorException("we don't know yet to handle " + s + " configuration files");
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
