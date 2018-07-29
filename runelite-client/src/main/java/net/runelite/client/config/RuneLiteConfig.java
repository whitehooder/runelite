/*
 * Copyright (c) 2017, Tyler <https://github.com/tylerthardy>
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
package net.runelite.client.config;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.types.GameSize;

@ConfigGroup("runelite")
public interface RuneLiteConfig extends Config
{
	String CONFIG_GROUP = "runelite";
	String GAME_SIZE = "gameSize";
	String ALWAYS_ON_TOP = "gameAlwaysOnTop";
	String LOCK_WINDOW_SIZE = "lockWindowSize";
	String ENABLE_CUSTOM_CHROME = "uiEnableCustomChrome";
	String CONTAIN_IN_SCREEN = "containInScreen";
	String CLIENT_BOUNDS = "clientBounds";
	String CLIENT_EXTENDED_STATE = "clientExtendedState";
	String CLIENT_FULLSCREEN = "clientFullscreen";
	String SIDEBAR_STATE = "sidebarState";
	String SIDEBAR_SELECTED_TAB = "sidebarSelectedTab";
	String SIDEBAR_WIDTH = "sidebarWidth";

	@ConfigItem(
		keyName = GAME_SIZE,
		name = "Game size",
		description = "The game will resize to this resolution upon starting the client",
		position = 10
	)
	default GameSize gameSize()
	{
		return new GameSize();
	}

	@ConfigItem(
		keyName = GAME_SIZE,
		name = "Game size",
		description = "The game will resize to this resolution upon starting the client"
	)
	void setGameSize(GameSize gameSize);

	@ConfigItem(
		keyName = "automaticResizeType",
		name = "Resize type",
		description = "Choose how the window should resize when opening and closing panels",
		position = 11
	)
	default ExpandResizeType automaticResizeType()
	{
		return ExpandResizeType.KEEP_GAME_SIZE;
	}

	@ConfigItem(
		keyName = LOCK_WINDOW_SIZE,
		name = "Lock window size",
		description = "Determines if the window resizing is allowed or not",
		position = 12
	)
	default boolean lockWindowSize()
	{
		return false;
	}

	@ConfigItem(
		keyName = CONTAIN_IN_SCREEN,
		name = "Contain in screen",
		description = "Makes the client stay contained in the screen when attempted to move out of it.<br>Note: Only works if custom chrome is enabled.",
		position = 13
	)
	default boolean containInScreen()
	{
		return false;
	}

	@ConfigItem(
		keyName = "rememberClientState",
		name = "Remember client state",
		description = "Save the position, size and sidebar visibility of the client after exiting",
		position = 14
	)
	default boolean rememberClientState()
	{
		return true;
	}

	@ConfigItem(
		keyName = ENABLE_CUSTOM_CHROME,
		name = "Enable custom window chrome",
		description = "Use Runelite's custom window title and borders.",
		position = 15
	)
	default boolean enableCustomChrome()
	{
		return true;
	}

	@ConfigItem(
		keyName = ALWAYS_ON_TOP,
		name = "Enable client always on top",
		description = "The game will always be on the top of the screen",
		position = 16
	)
	default boolean gameAlwaysOnTop()
	{
		return false;
	}

	@ConfigItem(
		keyName = "warningOnExit",
		name = "Display warning on exit",
		description = "Toggles a warning popup when trying to exit the client",
		position = 17
	)
	default WarningOnExit warningOnExit()
	{
		return WarningOnExit.LOGGED_IN;
	}

	@ConfigItem(
		keyName = "notificationTray",
		name = "Enable tray notifications",
		description = "Enables tray notifications",
		position = 20
	)
	default boolean enableTrayNotifications()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notificationRequestFocus",
		name = "Request focus on notification",
		description = "Toggles window focus request",
		position = 21
	)
	default boolean requestFocusOnNotification()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notificationSound",
		name = "Enable sound on notifications",
		description = "Enables the playing of a beep sound when notifications are displayed",
		position = 22
	)
	default boolean enableNotificationSound()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notificationGameMessage",
		name = "Enable game message notifications",
		description = "Puts a notification message in the chatbox",
		position = 23
	)
	default boolean enableGameMessageNotification()
	{
		return false;
	}

	@ConfigItem(
		keyName = "notificationFlash",
		name = "Enable flash notification",
		description = "Flashes the game frame as a notification",
		position = 24
	)
	default boolean enableFlashNotification()
	{
		return false;
	}

	@ConfigItem(
		keyName = "notificationFocused",
		name = "Send notifications when focused",
		description = "Toggles idle notifications for when the client is focused",
		position = 25
	)
	default boolean sendNotificationsWhenFocused()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fontType",
		name = "Dynamic Overlay Font",
		description = "Configures what font type is used for in-game overlays such as player name, ground items, etc.",
		position = 30
	)
	default FontType fontType()
	{
		return FontType.SMALL;
	}

	@ConfigItem(
		keyName = "tooltipFontType",
		name = "Tooltip Font",
		description = "Configures what font type is used for in-game tooltips such as food stats, NPC names, etc.",
		position = 31
	)
	default FontType tooltipFontType()
	{
		return FontType.SMALL;
	}

	@ConfigItem(
		keyName = "infoBoxVertical",
		name = "Display infoboxes vertically",
		description = "Toggles the infoboxes to display vertically",
		position = 32
	)
	default boolean infoBoxVertical()
	{
		return false;
	}

	@ConfigItem(
		keyName = "infoBoxWrap",
		name = "Infobox wrap count",
		description = "Configures the amount of infoboxes shown before wrapping",
		position = 33
	)
	default int infoBoxWrap()
	{
		return 4;
	}

	@ConfigItem(
		keyName = "infoBoxSize",
		name = "Infobox size (px)",
		description = "Configures the size of each infobox in pixels",
		position = 34
	)
	default int infoBoxSize()
	{
		return 35;
	}

	@ConfigItem(
		keyName = "sidebarToggleHotkey",
		name = "Toggle sidebar",
		description = "Pressing this key combination will hide or show the sidebar.",
		position = 35
	)
	default Keybind sidebarToggleHotkey()
	{ return new Keybind(KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK); }

	@ConfigItem(
		keyName = "fullscreenToggleHotkey",
		name = "Toggle fullscreen",
		description = "Pressing this key combination will enable or disable fullscreen mode.",
		position = 36
	)
	default Keybind fullscreenToggleHotkey()
	{ return new Keybind(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK); }

	@ConfigItem(
		keyName = CLIENT_BOUNDS,
		name = "Stored client bounds.",
		description = "The stored client location and dimensions.",
		hidden = true
	)
	default Rectangle storedClientBounds()
	{ return null; }

	@ConfigItem(
		keyName = CLIENT_BOUNDS,
		name = "Store new client bounds.",
		description = "Updates the stored client location and dimensions.",
		hidden = true
	)
	void storeClientBounds(Rectangle bounds);

	@ConfigItem(
		keyName = CLIENT_EXTENDED_STATE,
		name = "Stored client extended state.",
		description = "The stored client window maximize state.",
		hidden = true
	)
	default int storedClientExtendedState()
	{ return JFrame.NORMAL; }

	@ConfigItem(
		keyName = CLIENT_EXTENDED_STATE,
		name = "Store new client extended state.",
		description = "Updates the stored client window maximize state.",
		hidden = true
	)
	void storeClientExtendedState(Integer extendedState);

	@ConfigItem(
		keyName = CLIENT_FULLSCREEN,
		name = "The fullscreen state.",
		description = "The stored client fullscreen state.",
		hidden = true
	)
	default boolean storedClientFullscreenState()
	{ return false; }

	@ConfigItem(
		keyName = CLIENT_FULLSCREEN,
		name = "Store new fullscreen state.",
		description = "Update the stored client fullscreen state.",
		hidden = true
	)
	void storeClientFullscreenState(Boolean fullscreenEnabled);

	@ConfigItem(
		keyName = SIDEBAR_STATE,
		name = "The stored sidebar state.",
		description = "The sidebar's visibility last launch.",
		hidden = true
	)
	default boolean storedSidebarState()
	{ return true; }

	@ConfigItem(
		keyName = SIDEBAR_STATE,
		name = "Store new sidebar state.",
		description = "Update the sidebar's visibility.",
		hidden = true
	)
	void storeSidebarState(Boolean sidebarEnabled);

	@ConfigItem(
		keyName = SIDEBAR_SELECTED_TAB,
		name = "The stored selected tab.",
		description = "The tab selected when the client last closed.",
		hidden = true
	)
	default Class<? extends PluginPanel> storedSidebarSelectedTab()
	{ return null; }

	@ConfigItem(
		keyName = SIDEBAR_SELECTED_TAB,
		name = "Store currently selected tab.",
		description = "Update the stored selected tab.",
		hidden = true
	)
	void storeSidebarSelectedTab(Class<? extends PluginPanel> selectedTabPanel);

	@ConfigItem(
		keyName = SIDEBAR_WIDTH,
		name = "Stored sidebar width.",
		description = "The stored split pane's divider state.",
		hidden = true
	)
	default Integer storedSidebarWidth()
	{ return 225; }

	@ConfigItem(
		keyName = SIDEBAR_WIDTH,
		name = "Store new SplitPane divider position.",
		description = "Updates the stored split pane's divider state.",
		hidden = true
	)
	void storeSidebarWidth(Integer width);
}