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
package com.lmg.eventbus.event;

public class DeadEndEvent extends Event<DeadEndHandler>
{
	private Event<?> originalEvent = null;
	
	public DeadEndEvent(Event<?> originalEvent)
	{
		this.originalEvent = originalEvent;
	}
	
	public Event<?> getOriginalEvent()
	{
		return originalEvent;
	}
	
	public Class<DeadEndHandler> getAssociatedType() 
	{
		return DeadEndHandler.class;
	}

	public void dispatch(DeadEndHandler handler) 
	{
		handler.onDeadEvent(this);
	}	
}
