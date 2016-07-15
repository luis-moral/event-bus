/**
 * Copyright (C) 2016 Luis Moral Guerrero <luis.moral@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmg.eventbus;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmg.eventbus.event.DeadEndEvent;
import com.lmg.eventbus.event.Event;
import com.lmg.eventbus.event.EventHandler;

public class EventBus
{
	private Map<Class<Event<? extends EventHandler>>, Set<? extends EventHandler>> eventHandlerMap = null;	
	private String name = null;
	private int threads;
	
	private ExecutorService executorService = null;
	
	public EventBus()
	{
		this(UUID.randomUUID().toString(), 1);
	}
	
	public EventBus(String name, int threads)
	{
		this.name = name;
		this.threads = threads;
		
		executorService = Executors.newFixedThreadPool(threads, new EventBusThreadFactory());
		
		eventHandlerMap = new ConcurrentHashMap<Class<Event<? extends EventHandler>>, Set<? extends EventHandler>>();
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getThreads()
	{
		return threads;
	}
		
	/**
	 * Registers a new handler for an event type.
	 * 
	 * @param <E> generic Event type to register.
	 * @param <H> generic EventHandler type to register.
	 * 
	 * @param clazz of the event to handle.
	 * @param eventHandler to handle the event. 
	 */
	@SuppressWarnings("unchecked")	
	public<E extends Event<H>, H extends EventHandler> void register(Class<E> clazz, H eventHandler)
	{
		// Gets the Set for this event type
		Set<H> eventHandlerSet = (Set<H>) eventHandlerMap.get(clazz);
		
		// If the Set does not exist, creates a new one
		if (eventHandlerSet == null)
		{
			eventHandlerSet = Collections.newSetFromMap(new ConcurrentHashMap<H, Boolean>());
			
			eventHandlerMap.put((Class<Event<? extends EventHandler>>) clazz, eventHandlerSet);
		}
		
		eventHandlerSet.add(eventHandler);
	}
	
	@SuppressWarnings("unchecked")
	public<E extends Event<H>, H extends EventHandler> void unregister(Class<E> clazz, H eventHandler)
	{
		// Gets the Set for this event type
		Set<H> eventHandlerSet = (Set<H>) eventHandlerMap.get(clazz);
		
		// If exists, removes it from the Set
		if (eventHandlerSet != null) eventHandlerSet.remove(eventHandler);
	}
	
	@SuppressWarnings("unchecked")
	public<E extends Event<H>, H extends EventHandler> void fire(E event)
	{
		boolean handled = false;		
		
		// Gets the Set for this event type
		Set<H> eventHandlerSet = (Set<H>) eventHandlerMap.get(event.getClass());
		
		// If there is any registered handler
		if (eventHandlerSet != null && !eventHandlerSet.isEmpty())
		{			
			Iterator<H> iterator = eventHandlerSet.iterator();
			
			while (iterator.hasNext())
			{
				H eventHandler = iterator.next();
				
				event.dispatch(eventHandler);
				
				if (!handled) handled = true;
			}
		}
		
		// If the event have not been handled and its not the DeadEndEvent
		if (!handled && !(event instanceof DeadEndEvent))
		{
			// Fires the DeadEndEvent
			fire(new DeadEndEvent(event));
		}
	}
	
	public<E extends Event<H>, H extends EventHandler> Future<?> fireAsync(E event)
	{
		return fireAsync(event, null);
	}
	
	public<E extends Event<H>, H extends EventHandler> Future<?> fireAsync(E event, AsyncEventHandler<E, H> handler)
	{		
		return executorService.submit(new AsyncEventRunnable<E, H>(this, event, handler));
	}
	
	/**
	 * Unregisters all handlers
	 */
	public void clear()
	{
		eventHandlerMap.clear();
	}
	
	private class EventBusThreadFactory implements ThreadFactory
	{
		private AtomicInteger id = null;
		
		public EventBusThreadFactory()
		{
			id = new AtomicInteger(0);
		}
		
		public Thread newThread(Runnable r) 
		{			
			return new Thread(r, "EventBusThread-" + id.getAndAdd(1));
		}		
	}
	
	private class AsyncEventRunnable<E extends Event<H>, H extends EventHandler> implements Runnable
	{
		private EventBus eventBus = null;
		private E event = null;
		private AsyncEventHandler<E, H> handler = null;
		
		public AsyncEventRunnable(EventBus eventBus, E event, AsyncEventHandler<E, H> handler)
		{
			this.eventBus = eventBus;
			this.event = event;
			this.handler = handler;
		}
		
		public void run()
		{
			eventBus.fire(event);
			
			if (handler != null) handler.eventFinished(event);
		}
	}
}
