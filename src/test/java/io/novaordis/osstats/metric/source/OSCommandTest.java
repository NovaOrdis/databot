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

package io.novaordis.osstats.metric.source;

import io.novaordis.osstats.DataCollectionException;
import io.novaordis.osstats.os.MockOS;
import io.novaordis.utilities.os.NativeExecutionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/5/16
 */
public abstract class OSCommandTest extends MetricSourceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(OSCommandTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void executeCommandAndReturnStdout_NativeCommandExecutionThrowsException() throws Exception {

        OSCommand c = getMetricSourceToTest();

        MockOS mos = new MockOS();

        mos.throwNativeExecutionExceptionOnAnyCommand("SYNTHETIC1", new RuntimeException("SYNTHETIC2"));

        try {
            c.executeCommandAndReturnStdout(mos);
            fail("should throw exception");
        }
        catch(DataCollectionException e) {

            NativeExecutionException nee = (NativeExecutionException)e.getCause();
            String msg = nee.getMessage();
            log.info(msg);
            assertEquals("SYNTHETIC1", msg);
            RuntimeException re = (RuntimeException)nee.getCause();
            assertEquals("SYNTHETIC2", re.getMessage());
        }
    }

    @Test
    public void executeCommandAndReturnStdout_NativeCommandExecutionFails() throws Exception {

        OSCommand c = getMetricSourceToTest();

        MockOS mos = new MockOS();

        mos.failOnAnyCommand("SYNTHETIC-STDERR", "SYNTHETIC-STDOUT");

        try {
            c.executeCommandAndReturnStdout(mos);
            fail("should throw exception");
        }
        catch(DataCollectionException e) {

            String msg = e.getMessage();
            log.info(msg);

            assertEquals(msg, c.getCommand() + " execution failed: SYNTHETIC-STDERR");
        }
    }

    @Test
    public void executeCommandAndReturnStdout() throws Exception {

        OSCommand c = getMetricSourceToTest();

        MockOS mos = new MockOS();

        mos.setCommandOutput(c.getCommand() + " " + c.getArguments(), "SYNTHETIC-OUTPUT", "SYNTHETIC-STDERR");

        String stdout = c.executeCommandAndReturnStdout(mos);

        assertEquals("SYNTHETIC-OUTPUT", stdout);
    }

    // equals() --------------------------------------------------------------------------------------------------------

    @Test
    public void equals() throws Exception {

        OSCommand c = getMetricSourceToTest("arg1 arg2");
        OSCommand c2 = getMetricSourceToTest("arg1 arg2");

        assertEquals(c, c2);
        assertEquals(c2, c);
    }

    @Test
    public void equals2() throws Exception {

        OSCommand c = getMetricSourceToTest(null);
        OSCommand c2 = getMetricSourceToTest(null);

        assertEquals(c, c2);
        assertEquals(c2, c);
    }

    @Test
    public void equals3() throws Exception {

        OSCommand c = getMetricSourceToTest(null);
        OSCommand c2 = getMetricSourceToTest(" ");

        assertEquals(c, c2);
        assertEquals(c2, c);
    }

    @Test
    public void equals4() throws Exception {

        OSCommand c = getMetricSourceToTest(" ");
        OSCommand c2 = getMetricSourceToTest(null);

        assertEquals(c, c2);
        assertEquals(c2, c);
    }

    @Test
    public void equals5() throws Exception {

        OSCommand c = getMetricSourceToTest("");
        OSCommand c2 = getMetricSourceToTest(" ");

        assertEquals(c, c2);
        assertEquals(c2, c);
    }

    @Test
    public void equals6() throws Exception {

        OSCommand c = getMetricSourceToTest(" a   b  c d     ");
        OSCommand c2 = getMetricSourceToTest("a b c d");

        assertEquals(c, c2);
        assertEquals(c2, c);
    }

    @Test
    public void notEqual() throws Exception {

        OSCommand c = getMetricSourceToTest("arg1");
        OSCommand c2 = getMetricSourceToTest("arg2");

        assertNotEquals(c, c2);
        assertNotEquals(c2, c);
    }

    @Test
    public void hashCodeTest() throws Exception {

        OSCommand c = getMetricSourceToTest("arg1");
        OSCommand c2 = getMetricSourceToTest("arg2");

        assertNotEquals(c.hashCode(), c2.hashCode());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected OSCommand getMetricSourceToTest() throws Exception {
        return getMetricSourceToTest("");
    }

    protected abstract OSCommand getMetricSourceToTest(String arguments) throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
