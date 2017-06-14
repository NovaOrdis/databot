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
import io.novaordis.events.api.metric.MockAddress;
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
    public void constructor_NotAllMetricsBelongToTheSameSource() throws Exception {

        MockAddress ma = new MockAddress("A");
        MockMetricDefinition md = new MockMetricDefinition(ma, "md");

        MockAddress ma2 = new MockAddress("B");
        MockMetricDefinition md2 = new MockMetricDefinition(ma2, "md2");

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

    @Test
    public void constructor() throws Exception {

        MockAddress ma = new MockAddress();
        MockMetricDefinition md = new MockMetricDefinition(ma, "md");
        MockMetricDefinition md2 = new MockMetricDefinition(ma, "md2");

        SourceQueryTask q = new SourceQueryTask(Arrays.asList(md, md2));

        assertEquals(ma, q.getSourceAddress());

        List<MetricDefinition> metricDefinitions = q.getMetricDefinitions();
        assertEquals(2, metricDefinitions.size());
        assertEquals(md, metricDefinitions.get(0));
        assertEquals(md2, metricDefinitions.get(1));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
