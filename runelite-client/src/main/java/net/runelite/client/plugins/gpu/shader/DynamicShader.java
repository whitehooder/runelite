package net.runelite.client.plugins.gpu.shader;

import com.jogamp.opengl.GL4;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DynamicShader
{
	private final GL4 gl;
	private final Template template;
	private final Shader shader;
	private final UniformInitFunction uniformInitFunction;

	@FunctionalInterface
	public interface UniformInitFunction
	{
		void init(int program);
	}

	private int counter;
	public int id;

	public DynamicShader add(int type, String name)
	{
		shader.add(type, name);
		return this;
	}

	/**
	 * Increment the internal reference counter and init the program on the first increment.
	 * If the program fails to compile, the reference counter is reset to zero before throwing.
	 */
	public void acquire() throws ShaderException
	{
		counter++;
		if (counter == 1)
		{
			try
			{
				id = shader.compile(gl, template);
				uniformInitFunction.init(id);
			}
			catch (ShaderException ex)
			{
				counter = 0;
				throw ex;
			}
		}
	}

	/**
	 * Decrement the internal reference counter and delete the program if the counter reaches zero.
	 */
	public void release()
	{
		counter--;
		if (counter <= 0)
		{
			reset();
		}
	}

	public void reset()
	{
		if (id != 0)
		{
			gl.glDeleteProgram(id);
			id = 0;
		}
		counter = 0;
	}
}
