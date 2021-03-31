/*
 * Copyright (c) 2021, Hooder <https://github.com/aHooder>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.gpu.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DaylightCycle
{
	EARTH_BASED("Real-life by region", Type.EARTH_BASED, 24 * 60),
	CYCLE_20_MIN("1 day = 20 minutes", Type.EARTH_BASED, 20),
	CYCLE_1_HOUR("1 day = 1 hour", Type.EARTH_BASED, 60),
	CYCLE_6_HOURS("1 day = 6 hours", Type.EARTH_BASED, 6 * 60),
	ALWAYS_DAY("Always day", Type.CIRCULAR),
	ALWAYS_NIGHT("Always night", Type.CIRCULAR),
	CUSTOM_SUN_ANGLES("Custom sun angles", Type.STATIC);

	@RequiredArgsConstructor
	public enum Type
	{
		EARTH_BASED,
		CIRCULAR,
		STATIC
	}

	private final String name;
	private final Type cycleType;
	private final long minutesPerDay;

	DaylightCycle(String name, Type cycleType)
	{
		this.name = name;
		this.cycleType = cycleType;
		this.minutesPerDay = 0;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
