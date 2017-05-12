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

package io.novaordis.osstats.os.linux;

import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.PropertyFactory;
import io.novaordis.events.api.measure.MeasureUnit;
import io.novaordis.events.api.measure.MemoryMeasureUnit;
import io.novaordis.osstats.os.InvalidExecutionOutputException;
import io.novaordis.utilities.os.OS;
import io.novaordis.utilities.os.OSConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/31/16
 */
public class Vmstat {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String OS_MEMORY_PAGE_SIZE = "OS_MEMORY_PAGE_SIZE";

    public static final Object[][] CONTENT = new Object[][] {

            // vmstat output literal, property name,       type,          multiplication factor NAME, measure unit

            {"r",    "Runnable Process Count",              Integer.class, null,                null},
            {"b",    "Uninterruptible Sleep Process Count", Integer.class, null,                null},
            {"swpd", "Memory Swapped Out to Disk",          Long.class,    OS_MEMORY_PAGE_SIZE, MemoryMeasureUnit.BYTE},

    };

    // Static ----------------------------------------------------------------------------------------------------------

    public static List<Property> parseCommandOutput(String output) throws InvalidExecutionOutputException {

        int i = output.indexOf('\n');

        if (i == -1) {
            throw new InvalidExecutionOutputException("not a multi-line output");
        }

        // drop the categories
        int j = output.indexOf('\n', i + 1);

        if (j == -1) {
            throw new InvalidExecutionOutputException("not a multi-line output");
        }

        String hs = output.substring(i + 1, j);

        int k = output.indexOf('\n', j + 1);
        if (k == -1) {
            k = output.length();
        }

        String vs = output.substring(j + 1, k);

        StringTokenizer headers = new StringTokenizer(hs, " ");
        StringTokenizer values = new StringTokenizer(vs, " ");

        List<Property> properties = new ArrayList<>();

        OSConfiguration osConfiguration;

        try {
            osConfiguration = OS.getInstance().getConfiguration();
        }
        catch(Exception e) {
            throw new InvalidExecutionOutputException("failed to get the OS configuration", e);
        }

        for(i = 0; i < CONTENT.length; i ++) {

            //
            // resolve multiplication factors
            //

            Double multiplicationFactor = null;
            String multiplicationFactorName = (String)CONTENT[i][3];

            if (OS_MEMORY_PAGE_SIZE.equals(multiplicationFactorName)) {
                multiplicationFactor = (double)osConfiguration.getMemoryPageSize();
            }

            Property p = parseProperty(
                    headers.nextToken(),
                    values.nextToken(),
                    (String)CONTENT[i][0],
                    (String)CONTENT[i][1],
                    (Class)CONTENT[i][2],
                    multiplicationFactor,
                    (MeasureUnit)CONTENT[i][4]);

            properties.add(p);
        }

        return properties;
    }

    /**
     * @param value the value to convert to the given type and write into the property (after multiplication with
     *              the multiplication factor, if present)
     * @param multiplicationFactor the double to multiply the given value to obtain the value to write into the
     *                             property. May be null, in which case it is ignored.
     */
    public static Property parseProperty(
            String header, String value, String expectedHeader, String name, Class type,
            Double multiplicationFactor, MeasureUnit measureUnit) throws InvalidExecutionOutputException {

        if (!expectedHeader.equals(header)) {

            throw new InvalidExecutionOutputException(
                    "expecting header '"+ expectedHeader + "' but got '" + header + "'");
        }

        try {
            return PropertyFactory.createInstance(name, type, value, multiplicationFactor, measureUnit);
        }
        catch(IllegalArgumentException e) {
            throw new InvalidExecutionOutputException(e.getMessage());
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
