/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
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

import com.google.common.eventbus.Subscribe;
import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.events.ConfigChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.config.ExpandResizeType;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.config.WarningOnExit;
import net.runelite.client.events.PluginToolbarButtonAdded;
import net.runelite.client.events.PluginToolbarButtonRemoved;
import net.runelite.client.events.TitleToolbarButtonAdded;
import net.runelite.client.events.TitleToolbarButtonRemoved;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.rs.ClientUpdateCheckMode;
import net.runelite.client.ui.skin.SubstanceRuneLiteLookAndFeel;
import net.runelite.client.util.OSXUtil;
import net.runelite.client.util.SwingUtil;
import org.pushingpixels.substance.internal.SubstanceSynapse;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceTitlePaneUtilities;

@Slf4j
@Singleton
public class ClientUI
{
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

	private RuneLite runelite;
	private final RuneLiteProperties properties;
	private final RuneLiteConfig config;
	private final MouseManager mouseManager;
	private final Applet client;
	private final CardLayout cardLayout = new CardLayout();
	private ContainableFrame frame;
	private JPanel navContainer;
	private JSplitPane splitPane;
	private PluginPanel pluginPanel;
	private ClientPanel clientPanel;
	private ClientPluginToolbar pluginToolbar;
	private ClientTitleToolbar titleToolbar;
	private JButton currentButton;
	private NavigationButton currentNavButton;
	private boolean customChromeEnabled = true;
	private boolean sidebarEnabled = true;
	private boolean navContainerEnabled = true;
	private boolean fullscreenEnabled = false;
	private boolean navContainerWasEnabled = navContainerEnabled;
	private NavigationButton sidebarNavigationButton;
	private JButton sidebarNavigationJButton;

	@Inject
	private ClientUI(
		RuneLiteProperties properties,
		RuneLiteConfig config,
		MouseManager mouseManager,
		@Nullable Applet client)
	{
		this.properties = properties;
		this.config = config;
		this.mouseManager = mouseManager;
		this.client = client;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(RuneLiteConfig.CONFIG_GROUP) || frame == null)
			return;

		SwingUtilities.invokeLater(() ->
		{
			switch (event.getKey())
			{
				case RuneLiteConfig.ALWAYS_ON_TOP:
					if (frame.isAlwaysOnTopSupported())
						frame.setAlwaysOnTop(config.gameAlwaysOnTop());
					break;
				case RuneLiteConfig.LOCK_WINDOW_SIZE:
					frame.setResizable(!config.lockWindowSize());
					break;
				case RuneLiteConfig.ENABLE_CUSTOM_CHROME:
					setCustomWindowChrome(config.enableCustomChrome());
					break;
				case RuneLiteConfig.CONTAIN_IN_SCREEN:
					frame.setContainedInScreen(config.containInScreen());
					break;
				case RuneLiteConfig.GAME_SIZE:
					if (client == null)
						break;

					int changeWidth = config.gameSize().width - clientPanel.getSize().width;
					int changeHeight = config.gameSize().height - clientPanel.getSize().height;

					if (changeWidth != 0 || changeHeight != 0)
					{
						Dimension size = frame.getSize();
						size.width += changeWidth;
						size.height += changeHeight;
						frame.setSize(size);
						clientPanel.setSize(config.gameSize());
						frame.revalidate();
						frame.revalidateMinimumSize();
					}
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

				// Current tab clicked again. Close nav panel
				if (currentButton == jButton && currentButton.isSelected())
				{
					currentButton.setSelected(false);
					currentNavButton.setSelected(false);

					if (this.pluginPanel != null)
					{
						this.pluginPanel.onDeactivate();
						this.pluginPanel = null;
					}

					if (navContainerEnabled)
						toggleNavContainer();

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

					if (!navContainerEnabled)
						toggleNavContainer();

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

			if (config.storedSidebarSelectedTab() != null &&
				config.storedSidebarSelectedTab()
						.isInstance(navigationButton.getPanel()))
				button.doClick();
		});
	}

	@Subscribe
	public void onPluginToolbarButtonRemoved(final PluginToolbarButtonRemoved event)
	{
		SwingUtilities.invokeLater(() ->
		{
			pluginToolbar.removeComponent(event.getButton());
			final PluginPanel pluginPanel = event.getButton().getPanel();
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
				pluginToolbar.addComponent(event.getIndex(), navButton, button);
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
	 * @param runelite runelite instance that will be shut down on exit
	 * @throws Exception exception that can occur during creation of the UI
	 */
	public void open(final RuneLite runelite) throws Exception
	{
		this.runelite = runelite;
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

			// Try to enable fullscreen on OSX
			OSXUtil.tryEnableFullscreen(frame);

			frame.setTitle(properties.getTitle());
			frame.setIconImage(ICON);
			frame.setLayout(new BorderLayout(0, 0));
			frame.setLocationRelativeTo(frame.getOwner());
			frame.setResizable(!config.lockWindowSize());

			trayIcon = SwingUtil.createTrayIcon(ICON, properties.getTitle(), frame);

			SwingUtil.addGracefulExitCallback(frame,
				() ->
				{
					saveClientState();
					runelite.shutdown();
				},
				this::showWarningOnExit
			);

			// Set up container for sidebar panels
			navContainer = new JPanel();
			navContainer.setLayout(cardLayout);
			// To reduce substance's colorization (tinting)
			navContainer.putClientProperty(SubstanceSynapse.COLORIZATION_FACTOR, 1.0);
			navContainer.setMinimumSize(new Dimension(PluginPanel.PANEL_MIN_WIDTH, 0));

			clientPanel = new ClientPanel(client, config);

			// Set up SplitPane for resizable sidebar
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientPanel, navContainer);
			splitPane.setContinuousLayout(true);
			splitPane.setResizeWeight(1);
			splitPane.setDividerSize(PluginPanel.BORDER_WIDTH);
			splitPane.setUI(new SplitPaneUI());
			splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e ->
				config.storeSidebarWidth(navContainer.getWidth() + splitPane.getDividerSize()));

			frame.add(splitPane, BorderLayout.CENTER);

			if (config.storedSidebarSelectedTab() == null)
				toggleNavContainer();

			pluginToolbar = new ClientPluginToolbar();
			frame.add(pluginToolbar, BorderLayout.LINE_END);

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

			// Give focus to the game when any mouse click happens in the game area
			if (client != null)
			{
				client.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent e)
					{
						giveClientFocus();
					}
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

			// Add toggle key listeners to the whole client
			Toolkit.getDefaultToolkit().addAWTEventListener(awtEvent ->
			{
				KeyEvent e = (KeyEvent) awtEvent;
				if (e.getID() != KeyEvent.KEY_PRESSED) return;

				if (config.fullscreenToggleHotkey().matches(e))
				{
					toggleFullscreen();
					e.consume();
				}
				else if (config.sidebarToggleHotkey().matches(e))
				{
					toggleSidebar();
					e.consume();
				}
			}, AWTEvent.KEY_EVENT_MASK);

			if (!config.enableCustomChrome())
				setCustomWindowChrome(config.enableCustomChrome());

			if (config.rememberClientState())
			{
				applyStoredClientBounds();
				navContainer.setSize(new Dimension(config.storedSidebarWidth(), navContainer.getHeight()));
				splitPane.setDividerLocation(config.gameSize().width);

				if (!config.storedSidebarState())
					toggleSidebar();

				frame.setExtendedState(config.storedClientExtendedState());
			}

			showFrame();
		});
	}

	public void showFrame()
	{
		if (frame.isVisible()) return;

		SwingUtilities.invokeLater(() ->
		{
			if (config.storedClientFullscreenState())
				toggleFullscreen();

			frame.setVisible(true);
			frame.revalidateMinimumSize();

			// Has to come after
			frame.toFront();
			requestFocus();

			// Send a message if the client isn't purposefully run as vanilla
			if (runelite.getUpdateMode() == ClientUpdateCheckMode.AUTO && !(client instanceof Client))
				JOptionPane.showMessageDialog(frame,
					"RuneLite has not yet been updated to work with the latest\n"
						+ "game update, it will work with reduced functionality until then.",
					"RuneLite is outdated",
					JOptionPane.INFORMATION_MESSAGE);
		});

		log.info("Showing frame {}", frame);
	}

	private boolean showWarningOnExit()
	{
		if (config.warningOnExit() == WarningOnExit.ALWAYS)
			return true;

		if (config.warningOnExit() == WarningOnExit.LOGGED_IN && client instanceof Client)
			return ((Client) client).getGameState() != GameState.LOGIN_SCREEN;

		return false;
	}

	private void setCustomWindowChrome(boolean enabled)
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
			int index = 100;
			for (NavigationButton navButton : titleToolbar.getComponentMap().keySet())
				pluginToolbar.addComponent(
					-1,
					navButton,
					SwingUtil.createSwingButton(navButton, 0, null));
		}

		showFrame();
	}

	private void applyStoredClientBounds()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (config.storedClientBounds() != null)
			{
				boolean completelyHidden = true;

				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gs = ge.getScreenDevices();
				outer:
				for (GraphicsDevice gd : gs)
				{
					if (gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
					{
						GraphicsConfiguration[] gc = gd.getConfigurations();
						for (GraphicsConfiguration aGc : gc)
						{
							Rectangle b = aGc.getBounds();
							final int MARGIN = 25;
							b.x += MARGIN;
							b.y += MARGIN;
							b.width -= MARGIN * 2;
							b.height -= MARGIN * 2;

							if (config.storedClientBounds().intersects(b))
							{
								completelyHidden = false;
								break outer;
							}
						}
					}
				}

				if (!completelyHidden)
					frame.setBounds(config.storedClientBounds());
				else
					config.storeClientBounds(null);
			}
		});
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

	private void toggleSidebar()
	{
		sidebarEnabled = !sidebarEnabled;

		if (sidebarEnabled)
		{
			if (navContainerWasEnabled)
				toggleNavContainer();

			frame.add(pluginToolbar, BorderLayout.EAST);
			sidebarNavigationJButton.setIcon(new ImageIcon(SIDEBAR_CLOSE));
			sidebarNavigationJButton.setToolTipText("Close SideBar");
		}
		else
		{
			navContainerWasEnabled = navContainerEnabled;
			if (navContainerEnabled)
				toggleNavContainer();

			frame.remove(pluginToolbar);
			sidebarNavigationJButton.setIcon(new ImageIcon(SIDEBAR_OPEN));
			sidebarNavigationJButton.setToolTipText("Open SideBar");
		}

		if (config.automaticResizeType() == ExpandResizeType.KEEP_GAME_SIZE)
			frame.resizeWidth(sidebarEnabled, pluginToolbar.getWidth());

		frame.revalidate();
		frame.revalidateMinimumSize();

		giveClientFocus();
	}

	private void toggleNavContainer()
	{
		navContainerEnabled = !navContainerEnabled;

		int diffWidth = navContainer.getWidth() + splitPane.getDividerSize();

		if (config.automaticResizeType() == ExpandResizeType.KEEP_GAME_SIZE)
			frame.resizeWidth(navContainerEnabled, diffWidth);

		if (navContainerEnabled)
		{
			clientPanel.add(client, BorderLayout.CENTER);
			frame.add(splitPane, BorderLayout.CENTER);
		}
		else
		{
			frame.remove(splitPane);
			frame.add(client, BorderLayout.CENTER);
		}

		frame.revalidate();
		frame.revalidateMinimumSize();

		giveClientFocus();
	}

	private void toggleFullscreen()
	{
		fullscreenEnabled = !fullscreenEnabled;
		saveClientState();

		setCustomWindowChrome(fullscreenEnabled ? false : config.enableCustomChrome());

		if (OSXUtil.isOSX())
			OSXUtil.toggleFullscreen(frame);
		else
			frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(fullscreenEnabled ? frame : null);

		if (!fullscreenEnabled)
			applyStoredClientBounds();

		frame.revalidate();
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
			config.storeSidebarState(sidebarEnabled);

			if (currentNavButton != null)
				config.storeSidebarSelectedTab(currentNavButton.getPanel().getClass());
			else
				config.storeSidebarSelectedTab(null);

			// Filter out iconified because we want the window to show up when opened
			config.storeClientExtendedState(frame.getExtendedState() ^ JFrame.ICONIFIED);
			config.storeClientBounds(frame.getBounds());
			config.storeSidebarWidth(navContainer.getWidth() + splitPane.getDividerSize());
			config.storeClientFullscreenState(fullscreenEnabled);
		}
		else
		{
			config.storeClientExtendedState(null);
			config.storeClientBounds(null);
			config.storeSidebarState(null);
			config.storeSidebarSelectedTab(null);
			config.storeSidebarWidth(null);
			config.storeClientFullscreenState(null);
		}
	}
}
