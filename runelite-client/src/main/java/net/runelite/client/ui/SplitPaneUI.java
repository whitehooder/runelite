package net.runelite.client.ui;

import net.runelite.client.util.SwingUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SplitPaneUI extends BasicSplitPaneUI
{
	@Override
	public BasicSplitPaneDivider createDefaultDivider()
	{ return new SplitPaneDivider(this); }
}

class SplitPaneDivider extends BasicSplitPaneDivider
{
	private final long FPS = 30;
	private final long FADE_TRANSITION_TIME = 200;
	private long previousTimeMillis = System.currentTimeMillis();
	private double currentTransitionTime = 0;
	private boolean mouseDown = false;

	public SplitPaneDivider(BasicSplitPaneUI ui)
	{
		super(ui);
		Timer timer = new Timer((int) (1000 / FPS), e -> repaint());
		timer.start();

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				mouseDown = true;
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent)
			{
				mouseDown = false;
			}
		});
	}

	@Override
	public void paint(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics;
		g.setColor(ColorScheme.DARK_GRAY_COLOR);

		int w = getSize().width;
		int h = getSize().height;
		g.fillRect(0, 0, w, h);

		if (isMouseOver() || mouseDown)
			currentTransitionTime += System.currentTimeMillis() - previousTimeMillis;
		else
			currentTransitionTime = 0;
		previousTimeMillis = System.currentTimeMillis();

		float ratio = (float) Math.min(1, currentTransitionTime / FADE_TRANSITION_TIME);
		Color blended = SwingUtil.colorLerp(ColorScheme.MEDIUM_GRAY_COLOR, ColorScheme.BRAND_ORANGE, ratio);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(blended);

		int dotCount = 3;
		int dotDiameter = 4;
		int dotSpacing = 5;

		int dotLength = dotCount * dotDiameter + (dotCount - 1) * dotSpacing;
		int startY = (h / 2) - (dotLength / 2);
		int x = (w - dotDiameter) / 2;

		for (int y = startY; y < (h / 2) + (dotLength / 2); y += dotDiameter + dotSpacing)
			g.fillOval(x, y, dotDiameter, dotDiameter);

		super.paint(g);
	}
}