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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class exists mainly to allow us to give custom names to threads handing metric sources.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/14/17
 */
public class MetricSourceThreadFactory implements ThreadFactory {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private final String threadBaseName;
    private final AtomicInteger threadCounter;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param threadBaseName the string to use as base for the thread name. A per-unique-thread counter will be
     *                       appended to the base name, so threads can be uniquely identified by name.
     */
    public MetricSourceThreadFactory(String threadBaseName) {

        this.threadBaseName = threadBaseName;
        this.threadCounter = new AtomicInteger(0);
    }

    // ThreadFactory implementation ------------------------------------------------------------------------------------

    @Override
    public Thread newThread(@SuppressWarnings("NullableProblems") Runnable r) {

        if (r == null) {

            throw new IllegalArgumentException("null runnable");
        }

        String threadName = threadBaseName + " #" + threadCounter.getAndIncrement();
        return new Thread(r, threadName);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
