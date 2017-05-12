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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/29/16
 */
public class MockPrintStream extends PrintStream {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MockPrintStream.class);


    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private LinkedList<String> lines;
    private String openLine;
    private boolean closed;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockPrintStream() {

        super(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new RuntimeException("write() NOT YET IMPLEMENTED");
            }
        });

        lines = new LinkedList<>();
    }

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    public void println(String s) {

        print(s + "\n");
    }

    @Override
    public void print(String s) {

        StringTokenizer st = new StringTokenizer(s, "\n", true);

        while(st.hasMoreTokens()) {

            String tok = st.nextToken();

            if ("\n".equals(tok)) {

                if (openLine != null) {
                    lines.add(openLine);
                    openLine = null;
                }
                else {
                    lines.add("");
                }
            }
            else {

                if (openLine == null) {
                    openLine = tok;
                }
                else {
                    openLine += tok;
                }
            }
        }
    }

    @Override
    public void close() {

        this.closed = true;
        log.info(this + " closed");
    }


    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * retrieves (and removes) the current line or null if no line is available
     */
    public String getLine() {

        if (lines.isEmpty()) {
            return null;
        }

        return lines.removeFirst();
    }

    public boolean isClosed() {
        return closed;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
