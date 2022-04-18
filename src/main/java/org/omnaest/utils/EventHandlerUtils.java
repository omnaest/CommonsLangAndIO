package org.omnaest.utils;

import org.omnaest.utils.events.DistributingEventHandlerManager;
import org.omnaest.utils.events.EventHandlerManager;

/**
 * @author omnaest
 */
public class EventHandlerUtils
{
    public static <E> EventHandlerManager<E> newEventHandlerManager()
    {
        return new DistributingEventHandlerManager<>();
    }

}
