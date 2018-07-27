/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;

public abstract class PluginPanel extends JPanel
{
	public static final int PANEL_MIN_WIDTH = 150;
	public static final int BORDER_WIDTH = 10;

	protected PluginPanel()
	{
		this(true);
	}

	protected PluginPanel(boolean wrap)
	{
		super();
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setMinimumSize(new Dimension(PANEL_MIN_WIDTH, 0));

//		Timer timer  = new Timer(1000, e -> System.out.println("navWidth: " + getWidth()));
//		timer.setRepeats(true);
//		timer.start();

//		final JPanel northPanel = new JPanel();
//		northPanel.setLayout(new BorderLayout());
//		northPanel.add(this, BorderLayout.NORTH);
//		northPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
	}

	public void onActivate()
	{
	}

	public void onDeactivate()
	{
	}
}
