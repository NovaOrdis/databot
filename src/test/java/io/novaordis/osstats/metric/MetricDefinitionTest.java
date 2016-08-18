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

package io.novaordis.osstats.metric;

import io.novaordis.events.core.event.MeasureUnit;
import io.novaordis.osstats.metric.source.MetricSource;
import io.novaordis.osstats.metric.source.MockMetricSource;
import io.novaordis.osstats.os.MockOS;
import io.novaordis.utilities.UserErrorException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/3/16
 */
public abstract class MetricDefinitionTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MetricDefinitionTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void getName() throws Exception {

        MetricDefinition d = getMetricDefinitionToTest();

        //
        // default behavior
        //
        assertEquals(d.getClass().getSimpleName(), d.getName());
    }

    @Test
    public void getDescription() throws Exception {

        MetricDefinition d = getMetricDefinitionToTest();

        String desc = d.getDescription();
        assertNotNull(desc);
        assertFalse(desc.isEmpty());
    }

    // getLabel() ------------------------------------------------------------------------------------------------------

    @Test
    public void getLabel() throws Exception {

        MetricDefinition d = getMetricDefinitionToTest();

        String label = d.getLabel();

        assertNotNull(label);

        //
        // if the metric definition includes a measure unit, the measure unit must be present in the label,
        // parantheses-enclosed
        //

        MeasureUnit mu = d.getMeasureUnit();

        if (mu == null) {

            //
            // no measure unit in label
            //

            assertFalse(label.contains("("));
            assertFalse(label.contains(")"));

            assertEquals(label, d.getSimpleLabel());
        }
        else {

            String muLabelFragment = " (" + mu.getLabel() + ")";
            assertTrue(label.endsWith(muLabelFragment));
            assertTrue(label.contains(d.getSimpleLabel()));
        }
    }

    @Test
    public void getSimpleLabel() throws Exception {

        MetricDefinition d = getMetricDefinitionToTest();

        String label = d.getLabel();
        String simpleLabel = d.getSimpleLabel();

        assertNotNull(simpleLabel);
        assertTrue(label.startsWith(simpleLabel));
    }

    // metric source tests ---------------------------------------------------------------------------------------------

    @Test
    public void getSources_NullOSName() throws Exception {

        MetricDefinition d = getMetricDefinitionToTest();

        try {
            d.getSources(null);
            fail("should throw exception");
        }
        catch(IllegalArgumentException iae) {
            log.info(iae.getMessage());
        }
    }

    @Test
    public void getSources_UnknownOS() throws Exception {

        MetricDefinition d = getMetricDefinitionToTest();

        List<MetricSource> source = d.getSources(MockOS.NAME);

        assertTrue(source.isEmpty());
    }

    @Test
    public void addSource() throws Exception {

        MetricDefinition d = getMetricDefinitionToTest();

        MockOS mos = new MockOS();

        assertTrue(d.getSources(mos.getName()).isEmpty());

        MetricSource source = new MockMetricSource();

        assertTrue(d.addSource(mos.getName(), source));

        List<MetricSource> sources = d.getSources(mos.getName());

        assertEquals(1, sources.size());
        assertEquals(source, sources.get(0));

        MetricSource source2 = new MockMetricSource();

        assertTrue(d.addSource(mos.getName(), source2));

        //
        // make sure the order is preserved
        //

        List<MetricSource> sources2 = d.getSources(mos.getName());
        assertEquals(2, sources2.size());
        assertEquals(source, sources2.get(0));
        assertEquals(source2, sources2.get(1));

        //
        // attempt to add a duplicate
        //

        assertFalse(d.addSource(mos.getName(), source));

        List<MetricSource> sources3 = d.getSources(mos.getName());
        assertEquals(2, sources3.size());
        assertEquals(source, sources3.get(0));
        assertEquals(source2, sources3.get(1));
    }

    // getInstance() ---------------------------------------------------------------------------------------------------

    @Test
    public void getInstance_UnknownInstance() throws Exception {

        try {
            MetricDefinition.getInstance("we are pretty sure there's no such metric");
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    protected abstract MetricDefinition getMetricDefinitionToTest() throws Exception;

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
