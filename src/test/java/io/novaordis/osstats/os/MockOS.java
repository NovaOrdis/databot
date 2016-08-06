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

package io.novaordis.osstats.os;

import io.novaordis.utilities.Files;
import io.novaordis.utilities.os.NativeExecutionException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import io.novaordis.utilities.os.OSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/31/16
 */
public class MockOS implements OS {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MockOS.class);

    public static final String NAME = "MockOS";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean breakOnAnyCommand;
    private String nativeExecutionExceptionMessageOnBrokenCommand;
    private Throwable causeOfBrokenCommand;

    private boolean failOnAnyCommand;
    private String stderrOnFailure;
    private String stdoutOnFailure;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockOS() {

        this.breakOnAnyCommand = false;
    }

    // OS implementation -----------------------------------------------------------------------------------------------

    @Override
    public OSConfiguration getConfiguration() {
        throw new RuntimeException("getConfiguration() NOT YET IMPLEMENTED");
    }

    @Override
    public NativeExecutionResult execute(String command) throws NativeExecutionException {

        if (breakOnAnyCommand) {

            throw new NativeExecutionException(nativeExecutionExceptionMessageOnBrokenCommand, causeOfBrokenCommand);
        }

        if (failOnAnyCommand) {

            return new NativeExecutionResult(1, stdoutOnFailure, stderrOnFailure);
        }

        if ("vmstat".equals(command)) {

            File f = new File(System.getProperty("basedir"), "src/test/resources/data/os/vmstat.out");
            assertTrue(f.isFile());
            String content = null;

            try {
                content = Files.read(f);
            }
            catch (Exception ex) {

                log.error("failed to read the content of " + f, ex);
                fail("failed to read the content of " + f);
            }

            return new NativeExecutionResult(0, content, null);
        }
        else {
            throw new RuntimeException("do not know how to handle command " + command);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void breakOnAnyCommand(String msg, Throwable cause) {

        this.breakOnAnyCommand = true;
        this.nativeExecutionExceptionMessageOnBrokenCommand = msg;
        this.causeOfBrokenCommand = cause;
    }

    public void failOnAnyCommand(String stderr, String stdout) {

        this.failOnAnyCommand = true;
        this.stderrOnFailure = stderr;
        this.stdoutOnFailure = stdout;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
