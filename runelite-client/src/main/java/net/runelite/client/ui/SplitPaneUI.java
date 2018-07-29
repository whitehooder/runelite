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

import lombok.Getter;
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

	@Override
	public SplitPaneDivider getDivider()
	{
		return (SplitPaneDivider) this.divider;
	}
}

class SplitPaneDivider extends BasicSplitPaneDivider
{
	private final long FPS = 30;
	private final long FADE_TRANSITION_TIME = 200;
	private long previousTimeMillis = System.currentTimeMillis();
	private double currentTransitionTime = 0;
	@Getter
	private boolean beingDragged = false;

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
				beingDragged = true;
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent)
			{
				beingDragged = false;
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

		if (isMouseOver() || beingDragged)
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