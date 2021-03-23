/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.gpu.template;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Template
{
	private final List<Function<String, String>> resourceLoaders = new ArrayList<>();

	public String process(Path basePath, String str)
	{
		StringBuilder sb = new StringBuilder();
		for (String line : str.split("\r?\n"))
		{
			if (line.startsWith("#include "))
			{
				String resource = line.substring(9);
				String resourceStr = null;
				if (basePath != null)
				{
					resourceStr = load(Paths.get(basePath.toString(), resource).toFile().getPath());
				}

				if (resourceStr == null)
				{
					// Try non-relative path
					resourceStr = load(resource);
				}

				if (resourceStr == null)
				{
					// If all else fails, leave the original line in for easier debugging
					resourceStr = line;
				}

				sb.append(resourceStr);
			}
			else
			{
				sb.append(line).append('\n');
			}
		}
		return sb.toString();
	}

	public String load(String filename)
	{
		for (Function<String, String> loader : resourceLoaders)
		{
			String value = loader.apply(filename);
			if (value != null)
			{
				Path basePath = Paths.get(filename).getParent();
				return process(basePath, value);
			}
		}

		return null;
	}

	public Template add(Function<String, String> fn)
	{
		resourceLoaders.add(fn);
		return this;
	}

	public Template addInclude(Class<?> clazz)
	{
		return add(f ->
		{
			InputStream is = clazz.getResourceAsStream(f);
			if (is != null)
			{
				return inputStreamToString(is);
			}
			return null;
		});
	}

	private static String inputStreamToString(InputStream in)
	{
		try
		{
			return CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
