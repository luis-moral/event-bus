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
package es.molabs.eventbus.test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import es.molabs.eventbus.AsyncEventHandler;
import es.molabs.eventbus.EventBus;
import es.molabs.eventbus.test.event.SimpleEvent;
import es.molabs.eventbus.test.event.SimpleHandler;

@RunWith(MockitoJUnitRunner.class)
public class AsyncEventTest 
{
	private EventBus eventBus = null;
	
	@Test
	public void testAsyncEventFinishedHandler() throws Throwable
	{
		// Mocks an end of asynchronous event handler
		TestAsyncEventHandler asyncEventFinishedHandler = Mockito.mock(TestAsyncEventHandler.class);
				
		// Creates a event
		SimpleEvent event = new SimpleEvent();
				
		// Fires the asynchronous event
		Future<?> future = eventBus.fireAsync(event, asyncEventFinishedHandler);
				
		// Waits 1 second
		future.get(1000, TimeUnit.MILLISECONDS);
				
		// Checks that handler has been called one time		
		Mockito.verify(asyncEventFinishedHandler, Mockito.times(1)).eventFinished(event);
	}
	
	@Test
	public void testAsyncEvent() throws Throwable
	{
		// Mocks a handler
		SimpleHandler eventHandler = Mockito.mock(SimpleHandler.class);		
		
		// Mocks an end of asynchronous event handler
		TestAsyncEventHandler asyncEventFinishedHandler = Mockito.mock(TestAsyncEventHandler.class);
		
		// Creates a event
		SimpleEvent event = new SimpleEvent();
		
		// Registers the handler in the event bus
		eventBus.register(SimpleEvent.class, eventHandler);
		
		// Fires the asynchronous event
		Future<?> one = eventBus.fireAsync(event, asyncEventFinishedHandler);
		Future<?> two = eventBus.fireAsync(event, asyncEventFinishedHandler);
		Future<?> three = eventBus.fireAsync(event, asyncEventFinishedHandler);
		
		// Waits 1 second per event
		one.get(1000, TimeUnit.MILLISECONDS);
		two.get(1000, TimeUnit.MILLISECONDS);
		three.get(1000, TimeUnit.MILLISECONDS);
		
		// Checks that each handler has been called three times		
		Mockito.verify(eventHandler, Mockito.times(3)).handleSimpleEvent(event);
		Mockito.verify(asyncEventFinishedHandler, Mockito.times(3)).eventFinished(event);
	}
	
	@Before
	public void setUp()
	{
		eventBus = new EventBus();
	}
	
	@After
	public void tearDown()
	{
		eventBus.clear();
		eventBus = null;
	}
	
	private class TestAsyncEventHandler implements AsyncEventHandler<SimpleEvent, SimpleHandler>
	{
		public void eventFinished(SimpleEvent event) 
		{
		}
	}
}
