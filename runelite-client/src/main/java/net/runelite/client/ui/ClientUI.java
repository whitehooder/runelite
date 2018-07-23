/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *	list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *	this list of conditions and the following disclaimer in the documentation
 *	and/or other materials provided with the distribution.
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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.events.ConfigChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.UpdateCheckMode;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ExpandResizeType;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.config.WarningOnExit;
import net.runelite.client.events.*;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.skin.SubstanceRuneLiteLookAndFeel;
import net.runelite.client.util.OSXUtil;
import net.runelite.client.util.SwingUtil;
import org.pushingpixels.substance.internal.SubstanceSynapse;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceTitlePaneUtilities;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

@Slf4j
@Singleton
public class ClientUI
{
	private static final String CONFIG_GROUP = "runelite";
	private static final String CONFIG_CLIENT_BOUNDS = "clientBounds";
	private static final String CONFIG_CLIENT_EXTENDED_STATE = "clientExtendedState";
	private static final String CONFIG_SIDEBAR_VISIBLE = "sidebarVisible";
	private static final String CONFIG_SIDEBAR_SELECTED_TAB = "sidebarSelectedTab";
	private static final String CONFIG_CLIENT_DIVIDER_LOCATION = "clientDividerLocation";
	private static final String CONFIG_CLIENT_FULLSCREEN = "clientFullscreen";
	public static final BufferedImage ICON;
	private static final BufferedImage SIDEBAR_OPEN;
	private static final BufferedImage SIDEBAR_CLOSE;

	static
	{
		BufferedImage icon;
		BufferedImage sidebarOpen;
		BufferedImage sidebarClose;

		try
		{
			synchronized (ImageIO.class)
			{
				icon = ImageIO.read(ClientUI.class.getResourceAsStream("/runelite.png"));
				sidebarOpen = ImageIO.read(ClientUI.class.getResourceAsStream("open.png"));
				sidebarClose = ImageIO.read(ClientUI.class.getResourceAsStream("close.png"));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		ICON = icon;
		SIDEBAR_OPEN = sidebarOpen;
		SIDEBAR_CLOSE = sidebarClose;
	}

	@Getter
	private TrayIcon trayIcon;

	private final RuneLite runelite;
	private final RuneLiteProperties properties;
	private final RuneLiteConfig config;
	private final EventBus eventBus;
	private final MouseManager mouseManager;
	private final CardLayout cardLayout = new CardLayout();
	private Applet client;
	private JFrame frame;
	private JPanel navContainer;
	private PluginPanel pluginPanel;
	private ClientPluginToolbar pluginToolbar;
	private ClientTitleToolbar titleToolbar;
	private JButton currentButton;
	private NavigationButton currentNavButton;
	private boolean customChromeEnabled = true;
	private boolean sidebarEnabled = true;
	private boolean fullscreenEnabled = false;
	private JPanel container;
	private ClientPanel clientPanel;
	private JSplitPane splitPane;
	private NavigationButton sidebarNavigationButton;
	private JButton sidebarNavigationJButton;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientUI(
		RuneLite runelite,
		RuneLiteProperties properties,
		RuneLiteConfig config,
		EventBus eventBus,
		MouseManager mouseManager)
	{
		this.runelite = runelite;
		this.properties = properties;
		this.config = config;
		this.eventBus = eventBus;
		this.mouseManager = mouseManager;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("runelite"))
			return;

		SwingUtilities.invokeLater(() ->
		{
			switch (event.getKey())
			{
				case "gameAlwaysOnTop":
					if (frame.isAlwaysOnTopSupported())
						frame.setAlwaysOnTop(config.gameAlwaysOnTop());
					break;
				case "lockWindowSize":
					frame.setResizable(!config.lockWindowSize());
					break;
//				case "automaticResizeType":
//					frame.setExpandResizeType(config.automaticResizeType());
//					break;
				case "uiEnableCustomChrome":
					setCustomWindowChrome(config.enableCustomChrome());
					break;
//				case "containInScreen":
//					frame.setContainedInScreen(config.containInScreen());
//					break;
				case "gameSize":
					if (client == null)
						break;
					// The upper bounds are defined by the applet's max size
					// The lower bounds are defined by the client's fixed size
					int width = Math.max(Math.min(config.gameSize().width, 7680), Constants.GAME_FIXED_WIDTH);
					int height = Math.max(Math.min(config.gameSize().height, 2160), Constants.GAME_FIXED_HEIGHT);
					final Dimension size = new Dimension(width, height);

					client.setSize(size);
					client.setPreferredSize(size);
					client.getParent().setPreferredSize(size);
					client.getParent().setSize(size);

					if (frame.isVisible())
						frame.pack();
					break;
			}
		});
	}

	@Subscribe
	public void onPluginToolbarButtonAdded(final PluginToolbarButtonAdded event)
	{
		SwingUtilities.invokeLater(() ->
		{
			final NavigationButton navigationButton = event.getButton();
			final PluginPanel pluginPanel = navigationButton.getPanel();

			if (pluginPanel != null)
				navContainer.add(pluginPanel, navigationButton.getTooltip());

			final JButton button = SwingUtil.createSwingButton(navigationButton, 0, (navButton, jButton) ->
			{
				if (navButton.getPanel() == null)
					return;

				if (currentButton == jButton && currentButton.isSelected())
				{
					currentButton.setSelected(false);
					currentNavButton.setSelected(false);

					// Close panel
					if (this.pluginPanel != null)
					{
						this.pluginPanel.onDeactivate();
						this.pluginPanel = null;
					}

					if (navContainer.getParent() != null)
					{
						int navWidth = navContainer.getWidth();
						container.remove(navContainer);
						container.revalidate();

						if (config.automaticResizeType() == ExpandResizeType.KEEP_WINDOW_SIZE)
						{
							Dimension s = frame.getSize();
							frame.setSize(s.width + navWidth, s.height);
						}

						giveClientFocus();
					}

					currentButton = null;
					currentNavButton = null;
				}
				else
				{
					if (currentButton != null)
						currentButton.setSelected(false);
					if (currentNavButton != null)
						currentNavButton.setSelected(false);

					currentButton = jButton;
					currentNavButton = navButton;
					currentButton.setSelected(true);
					currentNavButton.setSelected(true);

					if (navContainer.getParent() != null)
					{
						int navWidth = navContainer.getWidth();
						container.add(navContainer);
						container.revalidate();

						if (config.automaticResizeType() == ExpandResizeType.KEEP_GAME_SIZE)
						{
							Dimension s = frame.getSize();
							frame.setSize(s.width + navWidth, s.height);
						}

						giveClientFocus();
					}

					if (pluginPanel != null)
					{
						this.pluginPanel = pluginPanel;
						cardLayout.show(navContainer, navButton.getTooltip());

						giveClientFocus();
						pluginPanel.onActivate();
					}
				}
			});

			pluginToolbar.addComponent(event.getIndex(), event.getButton(), button);

			String sidebarSelectedTab = configManager.getConfiguration(
					CONFIG_GROUP, CONFIG_SIDEBAR_SELECTED_TAB);
			if (sidebarSelectedTab != null &&
					sidebarSelectedTab.equals(navigationButton.getPanel().getClass().getCanonicalName()))
				button.doClick();
		});
	}

	@Subscribe
	public void onPluginToolbarButtonRemoved(final PluginToolbarButtonRemoved event)
	{
		SwingUtilities.invokeLater(() ->
		{
			pluginToolbar.removeComponent(event.getButton());
			PluginPanel pluginPanel = event.getButton().getPanel();
			if (pluginPanel != null)
				navContainer.remove(pluginPanel);
		});
	}

	@Subscribe
	public void onTitleToolbarButtonAdded(final TitleToolbarButtonAdded event)
	{
		SwingUtilities.invokeLater(() ->
		{
			NavigationButton navButton = event.getButton();
			JButton button = SwingUtil.createSwingButton(
				event.getButton(),
				ClientTitleToolbar.TITLEBAR_SIZE - 6,
				null);

			titleToolbar.addComponent(navButton, button);

			if (!customChromeEnabled)
				pluginToolbar.addComponent(-1, navButton, button);
		});
	}

	@Subscribe
	public void onTitleToolbarButtonRemoved(final TitleToolbarButtonRemoved event)
	{
		SwingUtilities.invokeLater(() ->
		{
			titleToolbar.removeComponent(event.getButton());
			pluginToolbar.removeComponent(event.getButton());
		});
	}

	/**
	 * Initialize UI.
	 *
	 * @param client the client
	 * @throws Exception exception that can occur during creation of the UI
	 */
	public void init(@Nullable final Applet client) throws Exception
	{
		this.client = client;

		SwingUtilities.invokeAndWait(() ->
		{
			// Set some sensible swing defaults
			SwingUtil.setupDefaults();

			// Use substance look and feel
			SwingUtil.setTheme(new SubstanceRuneLiteLookAndFeel());

			// Use custom UI font
			SwingUtil.setFont(FontManager.getRunescapeFont());

			// Create main window
			frame = new ContainableFrame();

			OSXUtil.tryEnableFullscreen(frame);

			frame.setTitle(properties.getTitle());
			frame.setIconImage(ICON);
			frame.getLayeredPane().setCursor(Cursor.getDefaultCursor()); // Prevent substance from using a resize cursor for pointing
			frame.setLocationRelativeTo(frame.getOwner());
			frame.setResizable(!config.lockWindowSize());

			trayIcon = SwingUtil.createTrayIcon(ICON, properties.getTitle(), frame);

			SwingUtil.addGracefulExitCallback(frame,
				() ->
				{
					saveClientState();
					runelite.shutdown();
				},
				() -> client != null
					&& client instanceof Client
					&& showWarningOnExit()
			);

			container = new JPanel(new BorderLayout());

			navContainer = new JPanel();
			navContainer.setLayout(cardLayout);
			// To reduce substance's colorization (tinting)
			navContainer.putClientProperty(SubstanceSynapse.COLORIZATION_FACTOR, 1.0);

			JScrollPane navScrollPane = new JScrollPane(navContainer);
			navScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			container.add(navScrollPane, BorderLayout.CENTER);

			pluginToolbar = new ClientPluginToolbar();
			container.add(pluginToolbar, BorderLayout.LINE_END);

			clientPanel = new ClientPanel(client);
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientPanel, container);
			splitPane.setContinuousLayout(true);
			splitPane.setResizeWeight(1);
			splitPane.setDividerSize(PluginPanel.BORDER_WIDTH);
			splitPane.setUI(new SplitPaneUI());
			frame.add(splitPane);

			titleToolbar = new ClientTitleToolbar();
			titleToolbar.putClientProperty(SubstanceTitlePaneUtilities.EXTRA_COMPONENT_KIND, SubstanceTitlePaneUtilities.ExtraComponentKind.TRAILING);

			// Create hide sidebar button
			sidebarNavigationButton = NavigationButton
					.builder()
					.icon(SIDEBAR_CLOSE)
					.onClick(this::toggleSidebar)
					.build();

			sidebarNavigationJButton = SwingUtil.createSwingButton(sidebarNavigationButton, 0, null);

			titleToolbar.addComponent(sidebarNavigationButton, sidebarNavigationJButton);

			customChromeEnabled = !config.enableCustomChrome();
			setCustomWindowChrome(config.enableCustomChrome());
		});

		if (client != null)
		{
			// Give focus to the game when any mouse click happens in the game area
			client.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{ giveClientFocus(); }
			});

			mouseManager.registerMouseListener(new MouseListener()
			{
				@Override
				public MouseEvent mousePressed(MouseEvent e)
				{
					giveClientFocus();
					return e;
				}
			});
		}

		// Add sidebar toggle key listener to all of RuneLite
		Toolkit.getDefaultToolkit().addAWTEventListener(awtEvent ->
		{
			KeyEvent e = (KeyEvent) awtEvent;
			if (e.getID() != KeyEvent.KEY_PRESSED) return;

			if (config.fullscreenToggleHotkey().matches(e))
			{
				SwingUtilities.invokeLater(this::toggleFullscreen);
				e.consume();
			}
			else if (config.sidebarToggleHotkey().matches(e))
			{
				SwingUtilities.invokeLater(this::toggleSidebar);
				e.consume();
			}
		}, AWTEvent.KEY_EVENT_MASK);
	}

	private boolean showWarningOnExit()
	{
		if (config.warningOnExit() == WarningOnExit.ALWAYS)
			return true;

		if (config.warningOnExit() == WarningOnExit.LOGGED_IN)
			return ((Client) client).getGameState() != GameState.LOGIN_SCREEN;

		return false;
	}

	public void setCustomWindowChrome(boolean enabled)
	{
		// Nothing to change
		if (customChromeEnabled == enabled)
			return;

		customChromeEnabled = enabled;

		boolean wasVisible = frame.isVisible();

		// Required for changing decoration state
		if (frame.isDisplayable())
			frame.dispose();

		frame.setUndecorated(enabled);
		frame.getRootPane().setWindowDecorationStyle(enabled ? JRootPane.FRAME : JRootPane.NONE);

		for (NavigationButton navButton : titleToolbar.getComponentMap().keySet())
			pluginToolbar.removeComponent(navButton);

		if (enabled)
		{
			JComponent titleBar = SubstanceCoreUtilities.getTitlePaneComponent(frame);
			replaceSubstanceTitleBarLayout();
			titleBar.add(titleToolbar);
		}
		else
		{
			for (NavigationButton navButton : titleToolbar.getComponentMap().keySet())
				pluginToolbar.addComponent(-1, navButton, SwingUtil.createSwingButton(navButton, 0, null));
		}

		if (wasVisible)
			showFrame();
	}

	private void replaceSubstanceTitleBarLayout()
	{
		// Substance's default layout manager for the title bar only lays out substance's components
		// This wraps the default manager and lays out the TitleToolbar as well.
		final JComponent titleBar = SubstanceCoreUtilities.getTitlePaneComponent(frame);
		LayoutManager delegate = titleBar.getLayout();
		titleBar.setLayout(new LayoutManager()
		{
			@Override
			public void addLayoutComponent(String name, Component comp)
			{
				delegate.addLayoutComponent(name, comp);
			}

			@Override
			public void removeLayoutComponent(Component comp)
			{
				delegate.removeLayoutComponent(comp);
			}

			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
				return delegate.preferredLayoutSize(parent);
			}

			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
				return delegate.minimumLayoutSize(parent);
			}

			@Override
			public void layoutContainer(Container parent)
			{
				delegate.layoutContainer(parent);
				final int width = titleToolbar.getPreferredSize().width;
				titleToolbar.setBounds(titleBar.getWidth() - 75 - width, 0, width, titleBar.getHeight());
			}
		});
	}

	private void showFrame()
	{
		frame.setVisible(true); // Show frame to render system default titlebar for min size revalidation
		frame.toFront();
		requestFocus();
	}

	/**
	 * Show client UI after everything else is done.
	 *
   	 * @param updateMode AUTO, VANILLA, RUNELITE or NONE
	 * @throws Exception exception that can occur during modification of the UI
	 */
	public void show(UpdateCheckMode updateMode) throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			frame.pack();

			if (config.rememberClientState())
			{
				Boolean sidebarVisible = configManager.getConfiguration(
						CONFIG_GROUP, CONFIG_SIDEBAR_VISIBLE, boolean.class);
				// Visible by default, so only if false, we toggle the sidebar off
				if (sidebarVisible == Boolean.FALSE)
					toggleSidebar();

				Rectangle clientBounds = configManager.getConfiguration(
						CONFIG_GROUP, CONFIG_CLIENT_BOUNDS, Rectangle.class);
				if (clientBounds != null)
				{
					boolean completelyHidden = true;

					GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
					GraphicsDevice[] gs = ge.getScreenDevices();
					outer:
					for (int j = 0; j < gs.length; j++)
					{
						GraphicsDevice gd = gs[j];
						if (gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
						{
							GraphicsConfiguration[] gc = gd.getConfigurations();
							for (int i = 0; i < gc.length; i++)
							{
								Rectangle b = gc[i].getBounds();
								final int MARGIN = 25;
								b.x += MARGIN;
								b.y += MARGIN;
								b.width -= MARGIN * 2;
								b.height -= MARGIN * 2;

								if (clientBounds.intersects(b))
								{
									completelyHidden = false;
									break outer;
								}
							}
						}
					}

					if (!completelyHidden)
						frame.setBounds(clientBounds);
					else
						configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_CLIENT_BOUNDS);
				}

				Integer clientMaximized = configManager.getConfiguration(
						CONFIG_GROUP, CONFIG_CLIENT_EXTENDED_STATE, int.class);
				if (clientMaximized != null)
					frame.setExtendedState(clientMaximized);

				Boolean fullscreenEnabled = configManager.getConfiguration(CONFIG_GROUP, CONFIG_CLIENT_FULLSCREEN, boolean.class);
				if (fullscreenEnabled == Boolean.TRUE)
					toggleFullscreen();

				Integer clientDividerLocation = configManager.getConfiguration(
						CONFIG_GROUP, CONFIG_CLIENT_DIVIDER_LOCATION, int.class);
				if (clientDividerLocation != null)
					splitPane.setDividerLocation(clientDividerLocation);
			}

			showFrame();
		});

		eventBus.post(new ClientUILoaded());

		// Send a message if the client isn't purposefully run as vanilla
		if (updateMode == UpdateCheckMode.AUTO && !(client instanceof Client))
			SwingUtilities.invokeLater(() ->
				JOptionPane.showMessageDialog(frame,
					"RuneLite has not yet been updated to work with the latest\n"
						+ "game update, it will work with reduced functionality until then.",
					"RuneLite is outdated",
						INFORMATION_MESSAGE));
	}

	/**
	 * Paint this component to target graphics
	 *
	 * @param graphics the graphics
	 */
	public void paint(final Graphics graphics)
	{
		frame.paint(graphics);
	}

	/**
	 * Gets component width.
	 *
	 * @return the width
	 */
	public int getWidth()
	{
		return frame.getWidth();
	}

	/**
	 * Gets component height.
	 *
	 * @return the height
	 */
	public int getHeight()
	{
		return frame.getHeight();
	}

	/**
	 * Returns true if this component has focus.
	 *
	 * @return true if component has focus
	 */
	public boolean isFocused()
	{
		return frame.isFocused();
	}

	/**
	 * Request focus on this component and then on client component
	 */
	public void requestFocus()
	{
		if (OSXUtil.isOSX())
			OSXUtil.requestFocus();
		frame.requestFocus();
		giveClientFocus();
	}

	/**
	 * Get offset of game canvas in game window
	 *
	 * @return game canvas offset
	 */
	public Point getCanvasOffset()
	{
		if (client instanceof Client)
			return new Point(
				SwingUtilities.convertPoint(
					((Client) client).getCanvas(),
					0, 0,
					frame));
		return new Point(0, 0);
	}

	public GraphicsConfiguration getGraphicsConfiguration()
	{
		return frame.getGraphicsConfiguration();
	}

	void toggleSidebar()
	{
		sidebarEnabled = !sidebarEnabled;

		if (sidebarEnabled)
		{
			frame.remove(clientPanel);
			splitPane.setLeftComponent(clientPanel);
			frame.add(splitPane);
			sidebarNavigationJButton.setIcon(new ImageIcon(SIDEBAR_CLOSE));
			sidebarNavigationJButton.setToolTipText("Close SideBar");
		}
		else
		{
			frame.remove(splitPane);
			frame.add(clientPanel);
			sidebarNavigationJButton.setIcon(new ImageIcon(SIDEBAR_OPEN));
			sidebarNavigationJButton.setToolTipText("Open SideBar");
		}

		frame.revalidate();

		if (config.automaticResizeType() == ExpandResizeType.KEEP_GAME_SIZE)
		{
			Dimension s = frame.getSize();
			int widthDiff = (container.getWidth() + splitPane.getDividerSize()) * (sidebarEnabled ? -1 : 1);
			frame.setSize(s.width - widthDiff, s.height);
		}

		giveClientFocus();
	}

	void toggleFullscreen()
	{
		fullscreenEnabled = !fullscreenEnabled;

		if (fullscreenEnabled)
			setCustomWindowChrome(false);
		else
			setCustomWindowChrome(config.enableCustomChrome());

		if (OSXUtil.isOSX())
			OSXUtil.toggleFullscreen(frame);
		else
			frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(fullscreenEnabled ? frame : null);

//		if (fullscreenEnabled)
//		{
//			saveClientState();
//			setCustomWindowChrome(false);
//
//			// Required for changing decoration state
//			if (frame.isDisplayable())
//				frame.dispose();
//
//			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//			Rectangle b = frame.getGraphicsConfiguration().getBounds();
////			frame.setMinimumSize(new Dimension(b.width, b.height));
//
//			frame.setUndecorated(true);
//			frame.setResizable(false);
//
//			showFrame();
//
//			// Needs to be invoked later for certain Linux DEs (Gnome & KDE)
//			SwingUtilities.invokeLater(() -> frame.setLocationRelativeTo(null));
//		}
//		else
//		{
//			setCustomWindowChrome(config.enableCustomChrome());
//
//			Integer state = configManager.getConfiguration(CONFIG_GROUP, CONFIG_CLIENT_EXTENDED_STATE, int.class);
//			if (state == null)
//				state = JFrame.NORMAL;
//			frame.setExtendedState(state);
//			frame.setResizable(!config.lockWindowSize());
//
//			frame.revalidate();
//
//			Rectangle clientBounds = configManager.getConfiguration(
//					CONFIG_GROUP, CONFIG_CLIENT_BOUNDS, Rectangle.class);
//			if (clientBounds != null)
//				frame.setBounds(clientBounds);
//		}

		giveClientFocus();
	}

	private void giveClientFocus()
	{
		if (client instanceof Client)
		{
			final Canvas c = ((Client) client).getCanvas();
			c.requestFocusInWindow();
		}
		else if (client != null)
			client.requestFocusInWindow();
	}

	private void saveClientState()
	{
		if (config.rememberClientState())
		{
			// Save the sidebar state
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_SIDEBAR_VISIBLE, sidebarEnabled);
			if (currentNavButton != null)
				configManager.setConfiguration(CONFIG_GROUP, CONFIG_SIDEBAR_SELECTED_TAB, currentNavButton.getPanel().getClass().getCanonicalName());
			else
				configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_SIDEBAR_SELECTED_TAB);

			int state = frame.getExtendedState() ^ JFrame.ICONIFIED; // Filter out iconified because we want the window to show up when opened
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_EXTENDED_STATE, state);
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_BOUNDS, frame.getBounds());
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_DIVIDER_LOCATION, splitPane.getDividerLocation());
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_FULLSCREEN, fullscreenEnabled);
		}
		else
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_CLIENT_EXTENDED_STATE);
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_CLIENT_BOUNDS);
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_SIDEBAR_VISIBLE);
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_SIDEBAR_SELECTED_TAB);
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_CLIENT_DIVIDER_LOCATION);
		}
	}
}
