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
package net.runelite.client.ui;

import java.awt.AWTError;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GapLayout implements LayoutManager2, Serializable
{
	private class ContainerMetrics
	{
		public Dimension
			minSize = new Dimension(),
			maxSize = new Dimension(),
			prefSize = new Dimension();
	}

	public enum Direction
	{ VERTICAL, HORIZONTAL }

	private int gapSize = 5;
	private Direction direction = Direction.VERTICAL;

	private HashMap<Container, ContainerMetrics> managedContainers = new HashMap<>();

	/**
	 * Constructs a <code>GapLayout</code> object with default values (VERTICAL, 5).
	 *
	 */
	public GapLayout()
	{ }

	/**
	 * Constructs a <code>GapLayout</code> object with the given direction and the default gapSize (5).
	 *
	 * @param direction The direction for the components to flow.
	 *
	 * @exception AWTError If way has an invalid value.
	 */
	public GapLayout(Direction direction)
	{
		setDirection(direction);
	}

	/**
	 * Constructs a <code>GapLayout</code> object with the given direction and the default gapSize (5).
	 *
	 * @param gapSize The pixel width for the gaps between all components.
	 * @exception AWTError If way has an invalid value.
	 */
	public GapLayout(int gapSize)
	{
		setGapSize(gapSize);
	}

	/**
	 * Constructs a <code>GapLayout</code> object with the given gapSize and the default direction (VERTICAL).
	 *
	 * @param direction The direction for the components to flow.
	 * @param gapSize The pixel width for the gaps between all components.
	 */
	public GapLayout(Direction direction, int gapSize)
	{
		setDirection(direction);
		setGapSize(gapSize);
	}

	public int getGapSize()
	{
		return gapSize;
	}

	public void setGapSize(int gapSize)
	{
		this.gapSize = gapSize;
	}

	public Direction getDirection()
	{
		return direction;
	}

	public void setDirection(Direction direction)
	{
		this.direction = direction;
	}

	private Dimension getMaximumSizeOf(Component c)
	{
		Dimension max = c.getMaximumSize();
		if (max == null)
			max = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		return max;
	}

	private Dimension getMinimumSizeOf(Component c)
	{
		Dimension min = c.getMinimumSize();
		if (min == null)
			min = new Dimension(0, 0);
		return min;
	}

	/**
	 * Gets the preferred <code>Dimension</code> confined by its min and max dimensions.
	 *
	 * @param c The <code>Component</code>
	 *
	 * @return The confined preferred dimensions.
	 */
	private Dimension getPreferredSizeOf(Component c)
	{
		return getPreferredSizeOf(c, getMinimumSizeOf(c), getMaximumSizeOf(c));
	}

	/**
	 * Gets the preferred <code>Dimension</code> confined by the given min and max dimensions.
	 *
	 * @param c The <code>Component</code>
	 * @param min The minimum <code>Dimension</code> to return
	 * @param max The maximum <code>Dimension</code> to return
	 *
	 * @return The confined preferred dimensions.
	 */
	private Dimension getPreferredSizeOf(Component c, Dimension min, Dimension max)
	{
		Dimension pref = c.getPreferredSize();

		if (pref == null)
			pref = new Dimension(0, 0);

		return confineDimension(pref, min, max);
	}

	private Dimension confineDimension(Dimension dimension, Dimension min, Dimension max)
	{
		return new Dimension(
			Math.min(max.width, Math.max(min.width, dimension.width)),
			Math.min(max.height, Math.max(min.height, dimension.height)));
	}

	private ContainerMetrics getMetrics(Container container)
	{
		ContainerMetrics cm = managedContainers.get(container);
		if (cm != null)
			return calculateContainerMetrics(container);
		return cm;
	}

	/**
	 * Returns the preferred size of the layout.
	 *
	 * @param container The container that needs to be laid out.
	 *
	 * @return The dimension of the layout.
	 */
	@Override
	public Dimension preferredLayoutSize(Container container)
	{
		Dimension pref = getMetrics(container).prefSize;
		if (direction == Direction.HORIZONTAL)
			return new Dimension(pref.width, 0);
		else if (direction == Direction.VERTICAL)
			return new Dimension(0, pref.height);
		return null;
	}

	/**
	 * Returns the minimum size of the layout.
	 *
	 * @param container The container that needs to be laid out.
	 *
	 * @return The dimension of the layout.
	 */
	@Override
	public Dimension minimumLayoutSize(Container container)
	{
		return getMetrics(container).minSize;
	}

	/**
	 * Returns the maximum size of the layout gived the components
	 * in the given container.
	 *
	 * @param container The container that needs to be laid out.
	 *
	 * @return The dimension of the layout.
	 */
	@Override
	public Dimension maximumLayoutSize(Container container)
	{
		return getMetrics(container).maxSize;
	}

	private int toInt(long value)
	{
		if (value > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		if (value < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		return (int) value;
	}

	private ContainerMetrics calculateContainerMetrics(Container container)
	{
		ContainerMetrics cm = new ContainerMetrics();

		long minW = 0, minH = 0,
			maxW = 0, maxH = 0,
			prefW = 0, prefH = 0;

		synchronized (container.getTreeLock())
		{
			Insets in = container.getInsets();
			int inW = in.left + in.right;
			int inH = in.top + in.bottom;

			int totalGapSize = 0;
			if (container.getComponentCount() > 1)
				totalGapSize = gapSize * (container.getComponentCount() - 1);

			if (direction == Direction.HORIZONTAL)
			{
				minW = minW + totalGapSize;
				maxW = maxW + totalGapSize;
				prefW = prefW + totalGapSize;

				for (Component c : container.getComponents())
				{
					Dimension min = getMinimumSizeOf(c);
					Dimension max = getMaximumSizeOf(c);
					Dimension pref = getPreferredSizeOf(c);

					minW = minW + min.width;
					maxW = maxW + max.width;
					prefW = prefW + pref.width;

					minH = Math.max(minH, min.height);
					maxH = Math.max(maxH, max.height);
					prefH = Math.max(prefH, pref.height);
				}
			}
			else if (direction == Direction.VERTICAL)
			{
				minH = minH + totalGapSize;
				maxH = maxH + totalGapSize;
				prefH = prefH + totalGapSize;

				for (Component c : container.getComponents())
				{
					Dimension min = getMinimumSizeOf(c);
					Dimension max = getMaximumSizeOf(c);
					Dimension pref = getPreferredSizeOf(c);

					minW = Math.max(minW, min.width);
					maxW = Math.max(maxW, max.width);
					prefW = Math.max(prefW, pref.width);

					minH = minH + min.height;
					maxH = maxH + max.height;
					prefH = prefH + pref.height;
				}
			}

			cm.minSize.width = toInt(minW + inW);
			cm.maxSize.width = toInt(maxW + inW);
			cm.prefSize.width = toInt(prefW + inW);

			cm.minSize.height = toInt(minH + inH);
			cm.maxSize.height = toInt(maxH + inH);
			cm.prefSize.height = toInt(prefH + inH);
		}

		managedContainers.put(container, cm);
		return cm;
	}

	@Override
	public void layoutContainer(Container container)
	{
		synchronized (container.getTreeLock())
		{
			Dimension cSize = container.getSize();
			Insets in = container.getInsets();

			int inW = in.left + in.right;
			int inH = in.top + in.bottom;

			int x = in.left;
			int y = in.top;
			int w = Math.max(0, cSize.width - inW);
			int h = Math.max(0, cSize.height - inH);

			if (direction == Direction.HORIZONTAL)
			{
				for (Component c : container.getComponents())
				{
					Dimension size = getPreferredSizeOf(c);
					c.setBounds(x, y, size.width, h);
					x += size.width + gapSize;
				}
			}
			else if (direction == Direction.VERTICAL)
			{
				for (Component c : container.getComponents())
				{
					Dimension size = getPreferredSizeOf(c);
					c.setBounds(x, y, w, size.height);
					y += size.height + gapSize;
				}
			}

			if (direction == Direction.HORIZONTAL)
				cSize.width = x - gapSize + inW;
			else if (direction == Direction.VERTICAL)
				cSize.height = y - gapSize + inH;
			container.setSize(cSize);
		}
	}

	@Override
	public void addLayoutComponent(String s, Component component)
	{ }

	@Override
	public void removeLayoutComponent(Component component)
	{ }

	@Override
	public void addLayoutComponent(Component component, Object o)
	{ }

	@Override
	public float getLayoutAlignmentX(Container container)
	{ return 0; }

	@Override
	public float getLayoutAlignmentY(Container container)
	{ return 0; }

	@Override
	public void invalidateLayout(Container container)
	{
		calculateContainerMetrics(container);
	}
}
