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

package io.novaordis.databot.configuration;

import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.logging.LoggerConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

/**
 * Used for testing.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/11/17
 */
public class DefaultConfiguration extends ConfigurationBase {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    public DefaultConfiguration() throws UserErrorException {

        super(true, null);
    }

    // ConfigurationBase overrides -------------------------------------------------------------------------------------

    @Override
    protected void load(InputStream is) throws UserErrorException {

        //
        // noop, we preserve the defaults
        //
    }

    @Override
    public Set<LoggerConfiguration> getConfiguration() {

        return Collections.emptySet();
    }

    @Override
    public File getFile() {

        return null;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
