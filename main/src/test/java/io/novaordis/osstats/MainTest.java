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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public class MainTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private PrintStream originalErrorStream;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Before
    public void setUp() {

        originalErrorStream = System.err;
    }

    @After
    public void tearDown() {

        //
        // restore the original error stream
        //
        System.setErr(originalErrorStream);
    }

    @Test
    public void main_UserErrorException() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream mockError = new PrintStream(baos);
        System.setErr(mockError);

        Main.main(new String[] {"test-user-error", "test message"});

        //
        // UserErrorMessage
        //

        String errorMessage = new String(baos.toByteArray()).trim();
        assertTrue(errorMessage.equals("[error]: test message"));
    }

    @Test
    public void main_UnexpectedException() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream mockError = new PrintStream(baos);
        System.setErr(mockError);

        Main.main(new String[] {
                "test-unexpected-error",
                "io.novaordis.osstats.MockRuntimeException",
                "other test message"});
        //
        // unexpected Exception
        //

        String errorMessage = new String(baos.toByteArray());

        assertTrue(errorMessage.startsWith("[error]: "));
        assertTrue(errorMessage.contains("internal failure:"));
        assertTrue(errorMessage.contains("MockRuntimeException"));
        assertTrue(errorMessage.contains("other test message"));
        assertTrue(errorMessage.contains("(consult logs for more details)"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
