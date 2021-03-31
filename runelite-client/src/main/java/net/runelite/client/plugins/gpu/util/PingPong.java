package net.runelite.client.plugins.gpu.util;

import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import com.jogamp.opengl.GL4;

public class PingPong
{
	// These are unnecessary, but OpenGL requires them for glTexImage2D even when no pixel data is supplied
	private static final int unusedFormat = GL_RGBA;
	private static final int unusedType = GL_FLOAT;

	private final GL4 gl;
	private final int internalFormat;

	private boolean hasShutdown = false;

	public int[] fbo;
	public int[] tex;

	public PingPong(GL4 gl, int internalFormat, int width, int height)
	{
		this.gl = gl;
		this.internalFormat = internalFormat;

		fbo = new int[2];
		gl.glGenFramebuffers(2, fbo, 0);

		initTextures(width, height);
	}

	public void shutdown()
	{
		if (!hasShutdown)
		{
			shutdownTextures();
			gl.glDeleteFramebuffers(2, fbo, 0);
			hasShutdown = true;
		}
	}

	public void resize(int width, int height)
	{
		shutdownTextures();
		initTextures(width, height);
	}

	private void initTextures(int width, int height)
	{
		tex = new int[2];
		gl.glGenTextures(2, tex, 0);

		for (int i = 0; i < 2; i++)
		{
			gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo[i]);
			gl.glBindTexture(GL_TEXTURE_2D, tex[i]);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, unusedFormat, unusedType, null);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex[i], 0);
		}
	}

	private void shutdownTextures()
	{
		gl.glDeleteTextures(2, tex, 0);
	}
}
