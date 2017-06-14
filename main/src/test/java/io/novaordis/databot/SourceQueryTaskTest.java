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

package io.novaordis.databot;

import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.utilities.address.Address;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/14/17
 */
public class SourceQueryTaskTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        MockMetricSource ms = new MockMetricSource();
        MockMetricDefinition md = new MockMetricDefinition(ms.getAddress(), "md");
        MockMetricDefinition md2 = new MockMetricDefinition(ms.getAddress(), "md2");

        SourceQueryTask q = new SourceQueryTask(Arrays.asList(md, md2));

        Address a = ms.getAddress();

        assertEquals(a, q.getSourceAddress());
    }

    @Test
    public void constructor_NotAllMetricsBelongToTheSameSource() throws Exception {

        MockMetricSource ms = new MockMetricSource();
        MockMetricDefinition md = new MockMetricDefinition(ms.getAddress(), "md");

        MockMetricSource ms2 = new MockMetricSource();
        MockMetricDefinition md2 = new MockMetricDefinition(ms2.getAddress(), "md2");

        List<MetricDefinition> mds = Arrays.asList(md, md2);

        try {
            new SourceQueryTask(mds);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("metrics do not belong to the same source"));
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
