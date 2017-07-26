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

package io.novaordis.databot.task;

import io.novaordis.databot.MockMetricDefinition;
import io.novaordis.databot.MockMetricSource;
import io.novaordis.databot.MockProperty;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.events.api.metric.MetricDefinition;
import io.novaordis.events.api.metric.MetricSourceException;
import io.novaordis.events.api.metric.MockAddress;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_NullMetricSource() throws Exception {

        MockAddress ma = new MockAddress("A");
        MockMetricDefinition md = new MockMetricDefinition(ma, "md");
        List<MetricDefinition> mds = Collections.singletonList(md);

        try {

            new SourceQueryTask(null, mds);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("null metric source"));
        }
    }

    @Test
    public void constructor_NotAllMetricsBelongToTheSameSource() throws Exception {

        MockAddress ma = new MockAddress("A");

        MockMetricSource ms = new MockMetricSource(ma);

        MockMetricDefinition md = new MockMetricDefinition(ma, "md");

        MockAddress ma2 = new MockAddress("B");
        MockMetricDefinition md2 = new MockMetricDefinition(ma2, "md2");

        List<MetricDefinition> mds = Arrays.asList(md, md2);

        try {

            new SourceQueryTask(ms, mds);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("is not associated with source"));
        }
    }

    @Test
    public void constructor_AllMetricsBelongToTheSameSourceWhichIsDifferentFromTheSourceToQuery() throws Exception {

        MockAddress a = new MockAddress("A");
        MockMetricSource s = new MockMetricSource(a);

        MockAddress ma = new MockAddress("B");
        MockMetricDefinition md = new MockMetricDefinition(ma, "md");

        MockAddress ma2 = new MockAddress("B");
        MockMetricDefinition md2 = new MockMetricDefinition(ma2, "md2");

        List<MetricDefinition> mds = Arrays.asList(md, md2);

        try {

            new SourceQueryTask(s, mds);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("is not associated with source"));
        }
    }

    @Test
    public void constructor_EmptyMetricDefinitionList() throws Exception {

        MockAddress ma = new MockAddress();

        MockMetricSource ms = new MockMetricSource(ma);

        SourceQueryTask q = new SourceQueryTask(ms, Collections.emptyList());

        List<MetricDefinition> metricDefinitions = q.getMetricDefinitions();
        assertTrue(metricDefinitions.isEmpty());
    }

    @Test
    public void constructor() throws Exception {

        MockAddress ma = new MockAddress();

        MockMetricDefinition md = new MockMetricDefinition(ma, "md");
        MockMetricDefinition md2 = new MockMetricDefinition(ma, "md2");

        MockMetricSource ms = new MockMetricSource(ma);

        SourceQueryTask q = new SourceQueryTask(ms, Arrays.asList(md, md2));

        assertEquals(ma, q.getSourceAddress());

        List<MetricDefinition> metricDefinitions = q.getMetricDefinitions();
        assertEquals(2, metricDefinitions.size());
        assertEquals(md, metricDefinitions.get(0));
        assertEquals(md2, metricDefinitions.get(1));
    }

    // call() ------------------------------------------------------------------------------------------------------------

    @Test
    public void call() throws Exception {

        MockAddress ma = new MockAddress("mock-source");
        MockMetricDefinition md = new MockMetricDefinition(ma, "mock-definition-1");
        MockMetricDefinition md2 = new MockMetricDefinition(ma, "mock-definition-2");

        MockMetricSource ms = new MockMetricSource(ma);

        //
        // populate the metric source with "expected" values
        //
        ms.addReadingForMetric("mock-definition-1", "mock-value-1");
        ms.addReadingForMetric("mock-definition-2", "mock-value-2");

        SourceQueryTask q = new SourceQueryTask(ms, Arrays.asList(md, md2));

        List<Property> result = q.call();

        assertEquals(2, result.size());

        StringProperty sp = (StringProperty)result.get(0);
        assertEquals("mock-definition-1", sp.getName());
        assertEquals("mock-value-1", sp.getValue());
        StringProperty sp2 = (StringProperty)result.get(1);
        assertEquals("mock-definition-2", sp2.getName());
        assertEquals("mock-value-2", sp2.getValue());
    }

    @Test
    public void call_CollectionFailsWithCheckedException() throws Exception {

        MockAddress ma = new MockAddress("mock-source");
        MockMetricDefinition md = new MockMetricDefinition(ma, "mock-definition-1");
        MockMetricDefinition md2 = new MockMetricDefinition(ma, "mock-definition-2");

        MockMetricSource ms = new MockMetricSource(ma);

        //
        // configure the metric source to fail
        //
        ms.breakOnCollectWithMetricSourceException("SYNTHETIC");

        SourceQueryTask q = new SourceQueryTask(ms, Arrays.asList(md, md2));

        try {

            q.call();
            fail("should have thrown exception");
        }
        catch(MetricSourceException e) {

            assertNull(e.getCause());
            String msg = e.getMessage();
            assertEquals("SYNTHETIC", msg);
        }
    }

    @Test
    public void call_CollectionFailsWithUncheckedException() throws Exception {

        MockAddress ma = new MockAddress("mock-source");
        MockMetricDefinition md = new MockMetricDefinition(ma, "mock-definition-1");
        MockMetricDefinition md2 = new MockMetricDefinition(ma, "mock-definition-2");

        MockMetricSource ms = new MockMetricSource(ma);

        //
        // configure the metric source to fail
        //
        ms.breakOnCollectWithUncheckedException("SYNTHETIC");

        SourceQueryTask q = new SourceQueryTask(ms, Arrays.asList(md, md2));

        try {

            q.call();
            fail("should have thrown exception");
        }
        catch(MetricSourceException e) {

            RuntimeException e2 = (RuntimeException)e.getCause();
            String msg = e2.getMessage();
            assertEquals("SYNTHETIC", msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
