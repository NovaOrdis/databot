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

import io.novaordis.utilities.os.NativeExecutionException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import io.novaordis.utilities.os.OSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

    private boolean throwNativeExecutionExceptionOnAnyCommand;
    private String nativeExecutionExceptionMessageOnBrokenCommand;
    private Throwable causeOfBrokenCommand;

    private boolean failOnAnyCommand;
    private String stderrOnFailure;
    private String stdoutOnFailure;

    // <command-and-argument, [stdout][stderr]>
    private Map<String, String[]> commandsAndOutputs;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockOS() {

        this.throwNativeExecutionExceptionOnAnyCommand = false;
        this.failOnAnyCommand = false;
        this.commandsAndOutputs = new HashMap<>();

        log.debug(this + " constructed");
    }

    // OS implementation -----------------------------------------------------------------------------------------------

    @Override
    public OSConfiguration getConfiguration() {
        throw new RuntimeException("getConfiguration() NOT YET IMPLEMENTED");
    }

    @Override
    public NativeExecutionResult execute(String commandAndArguments) throws NativeExecutionException {

        if (throwNativeExecutionExceptionOnAnyCommand) {

            throw new NativeExecutionException(nativeExecutionExceptionMessageOnBrokenCommand, causeOfBrokenCommand);
        }

        if (failOnAnyCommand) {

            return new NativeExecutionResult(1, stdoutOnFailure, stderrOnFailure, true, true);
        }

        String[] outputs = commandsAndOutputs.get(commandAndArguments);

        if (outputs != null) {

            return new NativeExecutionResult(0, outputs[0], outputs[1], true, true);
        }
        else {

            //
            // a random command
            //

            return new NativeExecutionResult(0, "MOCK-OUTPUT", "MOCK-STDERR", true, true);
        }
    }

    @Override
    public NativeExecutionResult execute(File directory, String command) throws NativeExecutionException {
        throw new RuntimeException("execute() NOT YET IMPLEMENTED");
    }

    @Override
    public String getName() {
        return NAME;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setCommandOutput(String commandAndArguments, String stdout, String stderr) {

        commandsAndOutputs.put(commandAndArguments, new String[] {stdout, stderr});
    }

    public void throwNativeExecutionExceptionOnAnyCommand(String msg, Throwable cause) {

        this.throwNativeExecutionExceptionOnAnyCommand = true;
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
