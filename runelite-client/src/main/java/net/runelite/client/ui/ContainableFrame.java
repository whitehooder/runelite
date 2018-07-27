/*
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
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
package net.runelite.client.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContainableFrame extends JFrame
{
	private boolean containedInScreen = false;
	private boolean wasJustMadeVisible = false;

	public ContainableFrame()
	{
		// Prevent substance from using a resize cursor for pointing
		getLayeredPane().setCursor(Cursor.getDefaultCursor());
	}

	public void setContainedInScreen(boolean value)
	{
		containedInScreen = value;

		if (value)
		{
			// Reposition the frame if it is intersecting with the bounds
			setLocation(getX(), getY());
			setBounds(getX(), getY(), getWidth(), getHeight());
		}
	}

	@Override
	public void setLocation(int x, int y)
	{
		if (containedInScreen)
		{
			Rectangle bounds = this.getGraphicsConfiguration().getBounds();
			x = Math.max(x, (int)bounds.getX());
			x = Math.min(x, (int)(bounds.getX() + bounds.getWidth() - this.getWidth()));
			y = Math.max(y, (int)bounds.getY());
			y = Math.min(y, (int)(bounds.getY() + bounds.getHeight() - this.getHeight()));
		}

		super.setLocation(x, y);
	}

	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		if (containedInScreen)
		{
			Rectangle bounds = this.getGraphicsConfiguration().getBounds();
			width = Math.min(width, width - (int)bounds.getX() + x);
			x = Math.max(x, (int)bounds.getX());
			height = Math.min(height, height - (int)bounds.getY() + y);
			y = Math.max(y, (int)bounds.getY());
			width = Math.min(width, (int)(bounds.getX() + bounds.getWidth()) - x);
			height = Math.min(height, (int)(bounds.getY() + bounds.getHeight()) - y);
		}

		super.setBounds(x, y, width, height);
	}

	@Override
	public void setVisible(boolean b)
	{
		if (!isVisible())
			wasJustMadeVisible = true;
		super.setVisible(b);
	}

	private void updateMinimumSize()
	{
		Dimension minSize = getLayout().minimumLayoutSize(getContentPane());
		Dimension getSize = getSize();
		Insets insets = getInsets();
		minSize.width += insets.left + insets.right;
		minSize.height += insets.top + insets.bottom;
		setMinimumSize(minSize);
	}

	/**
	 * Force minimum size of frame to be it's layout manager's minimum size
	 */
	public void revalidateMinimumSize()
	{
		if (isDisplayable())
		{
			if (isUndecorated())
			{
				// Custom chrome or fullscreen enabled and the contents are displayable, so min size can be calculated
				updateMinimumSize();
			}
			else if (isVisible() && wasJustMadeVisible)
			{
				// Wait for system titlebars to be added before updating min size
				SwingUtilities.invokeLater(this::updateMinimumSize);
				wasJustMadeVisible = false;
			}
			else
			{
				updateMinimumSize();
			}
		}
	}

	/**
	 * Expand or contract the frame the given amount of pixels
	 * @param expand Whether to expand (true) or contract (false) the frame
	 * @param widthChange The amount to expand or contract by
	 */
	public void resizeWidth(boolean expand, int widthChange)
	{
		setMinimumSize(new Dimension(0, 0));
		setSize(getWidth() + (expand ? 1 : -1) * widthChange, getHeight());
	}
}
