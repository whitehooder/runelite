package net.runelite.client.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.BoxLayout;

public class VerticalGapLayout extends BoxLayout
{
	private int gapSize = 5;
	private int way = Y_AXIS;

	public VerticalGapLayout(Container container)
	{
		super(container, Y_AXIS);
	}

	public VerticalGapLayout(Container container, int gapSize)
	{
		super(container, Y_AXIS);
		this.gapSize = gapSize;
	}

	public VerticalGapLayout(Container container, int way, int gapSize)
	{
		super(container, way);
		this.gapSize = gapSize;
		this.way = way;
	}

	@Override
	public void layoutContainer(Container container)
	{
		synchronized (container.getTreeLock())
		{
			Component[] children = container.getComponents();
			Insets in = container.getInsets();
			int x = in.left;
			int y = in.top;

			int w = container.getWidth() - (x + in.right);
			int h = container.getHeight() - (y + in.bottom);

//			System.out.println("in: " + in.toString());
//			System.out.println("x: " + x);
//			System.out.println("y: " + y);
//			System.out.println("cw: " + container.getWidth());
//			System.out.println("ch: " + container.getHeight());
//			System.out.println("w: " + w);
//			System.out.println("h: " + h);

			for (int i = 0; i < children.length; i++)
			{
				Dimension pref = children[i].getPreferredSize();
				Dimension min = children[i].getMinimumSize();
				Dimension max = children[i].getMaximumSize();
				Dimension size = new Dimension(
						Math.min(Math.max(min.width, pref.width), max.width),
						Math.min(Math.max(min.height, pref.height), max.height)
				);

				if (way == X_AXIS || way == LINE_AXIS)
				{
					size.height = h;
					children[i].setBounds(x, y, size.width, h);
					x += children[i].getPreferredSize().width + gapSize;
				}
				else
				{
					size.width = w;
					children[i].setBounds(x, y, w, size.height);
					y += children[i].getPreferredSize().height + gapSize;
				}
			}

			if (way == X_AXIS || way == LINE_AXIS)
				container.setPreferredSize(new Dimension(x - gapSize + (in.left + in.right), container.getPreferredSize().height));
			else
				container.setPreferredSize(new Dimension(container.getPreferredSize().width, y - gapSize + (in.top + in.bottom)));
		}

		container.repaint();
	}
}
