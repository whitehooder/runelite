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
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import joptsimple.internal.Strings;
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
				.addInclude(GpuPlugin.class),
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

	private void verify(Template template, Shader shader) throws Exception
	{
		File folder = temp.newFolder();
		List<String> args = new ArrayList<>();
		args.add(System.getProperty("glslang.path"));
		args.add("-l");
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
		}

		ProcessBuilder pb = new ProcessBuilder(args.toArray(new String[0]));
		pb.inheritIO();
		Process proc = pb.start();
		if (proc.waitFor() != 0)
		{
			Assert.fail();
		}
	}
}
