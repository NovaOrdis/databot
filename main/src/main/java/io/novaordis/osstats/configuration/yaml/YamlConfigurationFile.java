/*
 * Copyright (c) 2017 Nova Ordis LLC
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

package io.novaordis.osstats.configuration.yaml;

import io.novaordis.osstats.configuration.ConfigurationBase;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * A configuration instance backed by a property file.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class YamlConfigurationFile extends ConfigurationBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(YamlConfigurationFile.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public YamlConfigurationFile(boolean foreground, String fileName) throws UserErrorException {

        super(foreground, fileName);
    }

    // Configuration implementation ------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected void load(InputStream is) throws UserErrorException {
//
//        String s = properties.getProperty(SAMPLING_INTERVAL_PROPERTY_NAME);
//
//        if (s != null) {
//
//            //
//            // if null, we rely on the built-in values, set in the constructor
//            //
//
//            try {
//                samplingInterval = Integer.parseInt(s);
//            }
//            catch(Exception e) {
//                throw new UserErrorException("invalid sampling interval value: \"" + s + "\"", e);
//            }
//        }
//
//        s = properties.getProperty(OUTPUT_FILE_PROPERTY_NAME);
//
//        if (s != null) {
//
//            outputFileName = s;
//        }
//
//        s = properties.getProperty(OUTPUT_FILE_APPEND_PROPERTY_NAME);
//
//        if (s != null) {
//
//            String ls = s;
//            ls = ls.trim().toLowerCase();
//
//            if ("true".equals(ls) || "yes".equals(ls)) {
//                this.outputFileAppend = true;
//            }
//            else if ("false".equals(ls) || "no".equals(ls)) {
//                this.outputFileAppend = false;
//            }
//            else {
//                throw new UserErrorException(
//                        "invalid '" + OUTPUT_FILE_APPEND_PROPERTY_NAME + "' boolean value: \"" + s + "\"");
//            }
//        }
//
//        s = properties.getProperty(METRICS_PROPERTY_NAME);
//
//        if (s != null) {
//
//            StringTokenizer st = new StringTokenizer(s, ", ");
//            while(st.hasMoreTokens()) {
//                String tok = st.nextToken();
//                MetricDefinition md = MetricDefinition.getInstance(tok);
//                metrics.add(md);
//            }
//        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
