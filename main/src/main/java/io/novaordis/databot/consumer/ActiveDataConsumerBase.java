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

package io.novaordis.databot.consumer;

import io.novaordis.databot.DataConsumerException;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.ShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * An abstract base class embedding its own thread.
 *
 * The active consumer will shut down upon receiving a ShutdownEvent on the queue.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 6/13/17
 */
public abstract class ActiveDataConsumerBase extends DataConsumerBase implements Runnable {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ActiveDataConsumerBase.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private volatile boolean started;

    private volatile Thread thread;

    // Constructors ----------------------------------------------------------------------------------------------------

    protected ActiveDataConsumerBase(BlockingQueue<Event> eventQueue) {

        super(eventQueue);
    }

    // DataConsumer ----------------------------------------------------------------------------------------------------

    @Override
    public synchronized void start() throws DataConsumerException {

        if (started) {

            log.warn(this + " already started");
            return;
        }

        super.start();

        if (thread != null) {

            throw new IllegalStateException(this + " is already started");
        }

        thread = new Thread(this, getName() + " Thread");
        thread.start();
        started = true;

        log.debug(this + " was started");
    }

    @Override
    public boolean isStarted() {

        return started;
    }

    @Override
    public synchronized void stop() {

        if (!started) {

            log.debug(this + " already stopped");
            return;
        }

        //
        // send a ShutdownEvent down the queue
        //

        BlockingQueue<Event> eventQueue = getEventQueue();

        boolean sent = eventQueue.offer(new ShutdownEvent());

        //
        // event was not accepted
        //

        if (!sent) {

            throw new RuntimeException("NOT YET IMPLEMENTED: ShutdownEvent on accepted on the queue");
        }

        log.debug(this + " stop was initiated, will fully shutdown after the ShutdownEvent propagates through the queue ...");
    }

    // Runnable implementation -----------------------------------------------------------------------------------------

    @Override
    public final void run() {

        while(started) {

            try {

                log.debug(this + " blocking to take an event from the event queue");

                BlockingQueue<Event> eventQueue = getEventQueue();

                Event e = eventQueue.take();

                if (e instanceof ShutdownEvent) {


                    //
                    // shutting down
                    //

                    log.debug(this + " shutting down ...");

                    started = false;

                    thread = null;
                }

                //
                // send the event to subclass (even if it's ShutdownEvent)
                //

                handleEvent(e);

            }
            catch(InterruptedException e) {

                //
                // warn and go back
                //

                log.warn(thread + " interrupted while attempting to take events from the event queue");
            }
            catch(Throwable t) {

                log.error(this + " processing failure: " + toLogMessage(t), t);
            }
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    /**
     * Give the sub-class a chance to advertise its name, to be used as part of the thread name.
     */
    protected abstract String getName();

    /**
     * Passes the event to subclass for processing. The method is not supposed to throw unchecked exceptions.
     *
     * If it does, the upper layer will handle them by logging and looping over.
     */
    protected abstract void handleEvent(Event event);

    String toLogMessage(Throwable t) {

        if (t == null) {

            return "null";
        }

        String message = t.getMessage();

        if (message == null) {

            return t.getClass().getSimpleName() + " with no message";
        }
        else {

            return message + " (" + t.getClass().getSimpleName() + ")";
        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
