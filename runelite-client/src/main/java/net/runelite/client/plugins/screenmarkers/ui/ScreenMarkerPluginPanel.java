/*
 * Copyright (c) 2018, Kamiel, <https://github.com/Kamielvf>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
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
package net.runelite.client.plugins.screenmarkers.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.plugins.screenmarkers.ScreenMarkerOverlay;
import net.runelite.client.plugins.screenmarkers.ScreenMarkerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.GapLayout;
import net.runelite.client.ui.components.PluginErrorPanel;

@Singleton
public class ScreenMarkerPluginPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private static final Color DEFAULT_BORDER_COLOR = Color.GREEN;
	private static final Color DEFAULT_FILL_COLOR = new Color(0, 255, 0, 0);

	private static final int DEFAULT_BORDER_THICKNESS = 3;

	private final JLabel addMarker = new JLabel(ADD_ICON);
	private final JLabel title = new JLabel();
	private final PluginErrorPanel noMarkersPanel = new PluginErrorPanel();
	private JPanel centerPanel;
	private JScrollPane centerScrollPane;
	private JPanel markerView;

	@Inject
	private ScreenMarkerPlugin plugin;

	@Getter
	private Color selectedColor = DEFAULT_BORDER_COLOR;

	@Getter
	private Color selectedFillColor = DEFAULT_FILL_COLOR;

	@Getter
	private int selectedBorderThickness = DEFAULT_BORDER_THICKNESS;

	@Getter
	private ScreenMarkerCreationPanel creationPanel;

	static
	{
		try
		{
			synchronized (ImageIO.class)
			{
				ADD_ICON = new ImageIcon(ImageIO.read(ScreenMarkerPlugin.class.getResourceAsStream("add_icon.png")));
				ADD_HOVER_ICON = new ImageIcon(ImageIO.read(ScreenMarkerPlugin.class.getResourceAsStream("add_hover_icon.png")));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void init()
	{
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(BORDER_WIDTH, 0, 0, 0));

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

		title.setText("Screen Markers");
		title.setForeground(Color.WHITE);

		northPanel.add(title, BorderLayout.WEST);
		northPanel.add(addMarker, BorderLayout.EAST);
		northPanel.setBorder(new EmptyBorder(0, 0, BORDER_WIDTH, BORDER_WIDTH));

		centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		centerPanel.setBorder(new EmptyBorder(0, 0, BORDER_WIDTH, BORDER_WIDTH));

		centerScrollPane = new JScrollPane(centerPanel,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		HierarchyListener scrollbarToggleListener = e ->
		{
			JScrollBar sb = (JScrollBar) e.getSource();
			int rightWidth = BORDER_WIDTH;
			if (sb.isVisible())
			{
				if (sb.getWidth() != 0)
					rightWidth -= sb.getWidth();
				else
					rightWidth -= sb.getPreferredSize().width;
			}
			centerPanel.setBorder(new EmptyBorder(0, 0, BORDER_WIDTH, rightWidth));
			centerPanel.repaint();
		};
		centerScrollPane.getVerticalScrollBar().addHierarchyListener(scrollbarToggleListener);

		markerView = new JPanel();
		markerView.setLayout(new GapLayout(10));
		markerView.setBackground(ColorScheme.DARK_GRAY_COLOR);

		for (final ScreenMarkerOverlay marker : plugin.getScreenMarkers())
		{
			markerView.add(new ScreenMarkerPanel(plugin, marker));
		}

		noMarkersPanel.setContent("Screen Markers", "Highlight a region on your screen.");

		if (plugin.getScreenMarkers().isEmpty())
		{
			markerView.add(noMarkersPanel);
			title.setVisible(false);
		}

		creationPanel = new ScreenMarkerCreationPanel(plugin);

		addMarker.setToolTipText("Add new screen marker");
		addMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				setCreation(true);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addMarker.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addMarker.setIcon(ADD_ICON);
			}
		});

		centerPanel.add(markerView, BorderLayout.CENTER);

		add(northPanel, BorderLayout.PAGE_START);
		add(centerScrollPane, BorderLayout.CENTER);
	}

	public void rebuild()
	{
		removeAll();
		repaint();
		revalidate();
		init();
	}

	/* Enables/Disables new marker creation mode */
	public void setCreation(boolean on)
	{
		if (on)
		{
			markerView.remove(noMarkersPanel);
			title.setVisible(true);
		}
		else
		{
			boolean empty = plugin.getScreenMarkers().isEmpty();
			if (empty)
				markerView.add(noMarkersPanel);
			title.setVisible(!empty);
		}

		addMarker.setVisible(!on);

		if (on)
		{
			markerView.add(creationPanel);
			creationPanel.lockConfirm();
			plugin.setMouseListenerEnabled(true);
			centerScrollPane.validate();
			JScrollBar v = centerScrollPane.getVerticalScrollBar();
			v.setValue(v.getMaximum());
		}
	}
}
