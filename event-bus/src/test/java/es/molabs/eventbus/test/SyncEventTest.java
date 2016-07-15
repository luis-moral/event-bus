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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import es.molabs.eventbus.EventBus;
import es.molabs.eventbus.test.event.SimpleEvent;
import es.molabs.eventbus.test.event.SimpleHandler;

@RunWith(MockitoJUnitRunner.class)
public class SyncEventTest 
{
	private EventBus eventBus = null;
	
	@Test
	public void testSyncEvent()
	{
		// Mocks a Handler
		SimpleHandler eventHandler = Mockito.mock(SimpleHandler.class);
		SimpleEvent event = new SimpleEvent();
		
		// Registers the handler in the event bus
		eventBus.register(SimpleEvent.class, eventHandler);
		
		// Fires the event
		eventBus.fire(event);
		
		// Unregisters the handler from the event bus
		eventBus.unregister(SimpleEvent.class, eventHandler);
		
		// Fires the event again
		eventBus.fire(event);
		
		// Checks that handler has been called one time
		Mockito.verify(eventHandler, Mockito.times(1)).handleSimpleEvent(event);
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
}
