/*
 * Copyright (c) 2020 Abex
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
package net.runelite.client.plugins.gpu.shader;

import com.jogamp.opengl.GL4;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import joptsimple.internal.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.gpu.GpuPlugin;
import static net.runelite.client.plugins.gpu.GpuPlugin.BLUR_PROGRAM;
import static net.runelite.client.plugins.gpu.GpuPlugin.COMPUTE_PROGRAM;
import static net.runelite.client.plugins.gpu.GpuPlugin.LINUX_VERSION_HEADER;
import static net.runelite.client.plugins.gpu.GpuPlugin.POST_PROCESSING_PROGRAM;
import static net.runelite.client.plugins.gpu.GpuPlugin.PROGRAM;
import static net.runelite.client.plugins.gpu.GpuPlugin.SHADOW_PROGRAM;
import static net.runelite.client.plugins.gpu.GpuPlugin.SMALL_COMPUTE_PROGRAM;
import static net.runelite.client.plugins.gpu.GpuPlugin.UI_PROGRAM;
import static net.runelite.client.plugins.gpu.GpuPlugin.UNORDERED_COMPUTE_PROGRAM;
import static net.runelite.client.plugins.gpu.GpuPlugin.WINDOWS_VERSION_HEADER;
import net.runelite.client.util.OSType;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@Slf4j
public class ShaderTest
{
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void testShaders() throws Exception
	{
		String verifier = System.getProperty("glslang.path");
		Assume.assumeFalse("glslang.path is not set", Strings.isNullOrEmpty(verifier));

		String versionHeader = OSType.getOSType() == OSType.Linux ? LINUX_VERSION_HEADER : WINDOWS_VERSION_HEADER;

		Template[] templates = {
			new Template()
				.add(key ->
				{
					if ("version_header".equals(key))
					{
						return versionHeader;
					}
					return null;
				})
				.addInclude(GpuPlugin.class)
				.enableIncludeTracing(),
		};

		Shader[] shaders = {
			PROGRAM,
			UI_PROGRAM,
			COMPUTE_PROGRAM,
			SMALL_COMPUTE_PROGRAM,
			UNORDERED_COMPUTE_PROGRAM,
			SHADOW_PROGRAM,
			POST_PROCESSING_PROGRAM,
			BLUR_PROGRAM
		};

		for (Template t : templates)
		{
			for (Shader s : shaders)
			{
				verify(t, s);
			}
		}
	}

	@AllArgsConstructor
	private class SourceInclude
	{
		int fromLine, toLine;
		String sourceFile, includeFile;
	}

	private void verify(Template template, Shader shader) throws Exception
	{
		File folder = temp.newFolder();
		List<String> args = new ArrayList<>();
		args.add(System.getProperty("glslang.path"));
		args.add("-l");

		ArrayList<SourceInclude> includes = new ArrayList<>();

		for (Shader.Unit u : shader.units)
		{
			String contents = template.load(u.getFilename());
			String ext;
			switch (u.getType())
			{
				case GL4.GL_VERTEX_SHADER:
					ext = "vert";
					break;
				case GL4.GL_TESS_CONTROL_SHADER:
					ext = "tesc";
					break;
				case GL4.GL_TESS_EVALUATION_SHADER:
					ext = "tese";
					break;
				case GL4.GL_GEOMETRY_SHADER:
					ext = "geom";
					break;
				case GL4.GL_FRAGMENT_SHADER:
					ext = "frag";
					break;
				case GL4.GL_COMPUTE_SHADER:
					ext = "comp";
					break;
				default:
					throw new IllegalArgumentException(u.getType() + "");
			}

			Path dirPath = Paths.get(u.getFilename()).getParent();
			if (dirPath != null)
			{
				File dir = new File(folder, dirPath.toString());
				dir.mkdirs();
			}

			File file = new File(folder, u.getFilename() + "." + ext);
			Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
			args.add(file.getAbsolutePath());

			String[] lines = contents.split("\n");
			int start = 0;
			String includeFile = null;
			for (int i = 0; i < lines.length; i++)
			{
				if (lines[i].startsWith("// #include "))
				{
					start = i;
					includeFile = lines[i].substring(12);
				}
				else if (lines[i].startsWith("// #endinclude"))
				{
					includes.add(new SourceInclude(start, i, u.getFilename(), includeFile));
				}
			}
		}

		ProcessBuilder pb = new ProcessBuilder(args.toArray(new String[0]));
		Process proc = pb.start();

		String glslangOutput;
		try (BufferedReader outReader = new BufferedReader(new InputStreamReader(proc.getInputStream())))
		{
			glslangOutput = outReader.lines().collect(Collectors.joining(System.lineSeparator()));
		}

		if (proc.waitFor() != 0)
		{
			String[] outputLines = glslangOutput.split("\n");

			for (String line : outputLines)
			{
				if (line.startsWith("ERROR: "))
				{
					line = line.substring(7);
					if (line.startsWith(folder.toString() + File.separator))
					{
						line = line.substring(folder.toString().length() + 1);

						int sep = line.indexOf(':');
						if (sep != -1)
						{
							String[] parts = line.split(":");
							String[] sourceFileParts = parts[0].split("\\.");
							String sourceFile = String.join(".", Arrays.copyOfRange(sourceFileParts, 0, sourceFileParts.length - 1));
							int lineNumber = Integer.parseInt(parts[1]);
							String rest = String.join(":", Arrays.copyOfRange(parts, 2, parts.length));

							for (SourceInclude include : includes)
							{
								if (include.sourceFile.equals(sourceFile) &&
									lineNumber >= include.fromLine &&
									lineNumber <= include.toLine)
								{

									sourceFile = include.includeFile;
									lineNumber -= include.fromLine + 1;
									break;
								}
							}

							line = sourceFile + " - line " + lineNumber + " -" + rest;
						}
					}
					System.err.println("ERROR: " + line);
				}
				else
				{
					// Just to make sure the test doesn't skip any useful information
					System.out.println(line);
				}
			}

			Assert.fail();
		}
	}
}
