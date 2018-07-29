/*
 * Copyright (c) 2018, Whitehooder <https://github.com/whitehooder>
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
package net.runelite.client.ui.types;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import net.runelite.api.Constants;

/**
 * Dimension implementation with limits suitable for the game frame
 *
 * The upper bounds are defined by the applet's max size
 * The lower bounds are defined by the client's fixed size
 */
public class GameSize extends Dimension
{
	public GameSize()
	{
		super(Constants.GAME_FIXED_SIZE);
	}

	public GameSize(Dimension dimension)
	{
		this(dimension.width, dimension.height);
	}

	public GameSize(int width, int height)
	{
		super(
			Math.max(Math.min(width, 7680), Constants.GAME_FIXED_WIDTH),
			Math.max(Math.min(height, 2160), Constants.GAME_FIXED_HEIGHT));
	}

	@Override
	public void setSize(int width, int height)
	{
		setSize(
			Math.max(Constants.GAME_FIXED_WIDTH, width),
			Math.max(Constants.GAME_FIXED_HEIGHT, height));
	}

	@Override
	public void setSize(Dimension dimension)
	{
		this.setSize(dimension.width, dimension.height);
	}

	@Override
	public void setSize(Dimension2D dimension2D)
	{
		setSize(dimension2D.getWidth(), dimension2D.getHeight());
	}

	public void setWidth(int width)
	{
		setSize(width, getHeight());
	}

	public void setHeight(int height)
	{
		setSize(getWidth(), height);
	}
}
