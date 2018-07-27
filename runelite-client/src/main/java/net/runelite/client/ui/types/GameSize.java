package net.runelite.client.ui.types;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import net.runelite.api.Constants;

/**
 * Dimension implementation with limits suitable for the game frame
 *
 * The upper bounds are defined by the applet's max size
 * The lower bounds are defined by the client's fixed size
 */
public class GameSize extends Dimension
{
	public GameSize()
	{
		super(Constants.GAME_FIXED_SIZE);
	}

	public GameSize(Dimension dimension)
	{
		this(dimension.width, dimension.height);
	}

	public GameSize(int width, int height)
	{
		super(
			Math.max(Math.min(width, 7680), Constants.GAME_FIXED_WIDTH),
			Math.max(Math.min(height, 2160), Constants.GAME_FIXED_HEIGHT));
	}

	@Override
	public void setSize(int width, int height)
	{
		setSize(
			Math.max(Constants.GAME_FIXED_WIDTH, width),
			Math.max(Constants.GAME_FIXED_HEIGHT, height));
	}

	@Override
	public void setSize(Dimension dimension)
	{
		this.setSize(dimension.width, dimension.height);
	}

	@Override
	public void setSize(Dimension2D dimension2D)
	{
		setSize(dimension2D.getWidth(), dimension2D.getHeight());
	}

	public void setWidth(int width)
	{
		setSize(width, getHeight());
	}

	public void setHeight(int height)
	{
		setSize(getWidth(), height);
	}
}
