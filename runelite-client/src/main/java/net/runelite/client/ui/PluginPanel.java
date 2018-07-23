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

import lombok.AccessLevel;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class PluginPanel extends JPanel
{
	public static final int MIN_WIDTH = 100;
	public static final int PREFERRED_WIDTH = 225;
	public static final int BORDER_WIDTH = 10;
	private static final EmptyBorder BORDER = new EmptyBorder(BORDER_WIDTH, 0, BORDER_WIDTH, 0);

	@Getter(AccessLevel.PROTECTED)
	private final JScrollPane scrollPane;

	protected PluginPanel()
	{
		setBorder(BORDER);
		setLayout(new BorderLayout(5, 5));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setMinimumSize(new Dimension(MIN_WIDTH, Integer.MAX_VALUE));

		scrollPane = new JScrollPane(this);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PREFERRED_WIDTH, super.getPreferredSize().height);
	}

	public void onActivate()
	{
	}

	public void onDeactivate()
	{
	}
}
