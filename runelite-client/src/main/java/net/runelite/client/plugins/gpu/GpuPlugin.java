/*
 * Copyright (c) 2018-2021, Adam <Adam@sigterm.info>, Hooder <https://github.com/aHooder>
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
package net.runelite.client.plugins.gpu;

import com.google.common.primitives.Ints;
import com.google.inject.Provides;
import com.jogamp.nativewindow.awt.AWTGraphicsConfiguration;
import com.jogamp.nativewindow.awt.JAWTWindow;
import static com.jogamp.opengl.GL.GL_ALWAYS;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_BGRA;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT16;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DRAW_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_FUNC_ADD;
import static com.jogamp.opengl.GL.GL_FUNC_REVERSE_SUBTRACT;
import static com.jogamp.opengl.GL.GL_LESS;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_MAX_SAMPLES;
import static com.jogamp.opengl.GL.GL_MULTISAMPLE;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_NONE;
import static com.jogamp.opengl.GL.GL_ONE;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_READ_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_RENDERBUFFER;
import static com.jogamp.opengl.GL.GL_RGB;
import static com.jogamp.opengl.GL.GL_RGB8;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA16F;
import static com.jogamp.opengl.GL.GL_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE2;
import static com.jogamp.opengl.GL.GL_TEXTURE3;
import static com.jogamp.opengl.GL.GL_TEXTURE4;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRIANGLE_STRIP;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_CLAMP_TO_BORDER;
import static com.jogamp.opengl.GL2ES2.GL_COLOR_ATTACHMENT1;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_OUTPUT;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_NOTIFICATION;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SOURCE_API;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_TYPE_OTHER;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_INT;
import static com.jogamp.opengl.GL2ES2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_2D_MULTISAMPLE;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_BORDER_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_STATIC_COPY;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BARRIER_BIT;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLFBODrawable;
import com.jogamp.opengl.GLProfile;
import static com.jogamp.opengl.math.FloatUtil.E;
import static com.jogamp.opengl.math.FloatUtil.HALF_PI;
import static com.jogamp.opengl.math.FloatUtil.PI;
import static com.jogamp.opengl.math.FloatUtil.TWO_PI;
import static com.jogamp.opengl.math.FloatUtil.pow;
import static com.jogamp.opengl.math.FloatUtil.sqrt;
import com.jogamp.opengl.math.Matrix4;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import jogamp.nativewindow.SurfaceScaleUtils;
import jogamp.nativewindow.jawt.x11.X11JAWTWindow;
import jogamp.nativewindow.macosx.OSXUtil;
import jogamp.newt.awt.NewtFactoryAWT;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.BufferProvider;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Model;
import net.runelite.api.NodeCache;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Texture;
import net.runelite.api.TextureProvider;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import static net.runelite.client.plugins.gpu.util.GLUtil.glDeleteBuffer;
import static net.runelite.client.plugins.gpu.util.GLUtil.glDeleteFrameBuffer;
import static net.runelite.client.plugins.gpu.util.GLUtil.glDeleteTexture;
import static net.runelite.client.plugins.gpu.util.GLUtil.glDeleteVertexArrays;
import static net.runelite.client.plugins.gpu.util.GLUtil.glGenBuffers;
import static net.runelite.client.plugins.gpu.util.GLUtil.glGenFrameBuffer;
import static net.runelite.client.plugins.gpu.util.GLUtil.glGenTexture;
import static net.runelite.client.plugins.gpu.util.GLUtil.glGenVertexArrays;
import static net.runelite.client.plugins.gpu.util.GLUtil.glGetInteger;
import net.runelite.client.plugins.gpu.config.ProjectionDebugMode;
import net.runelite.client.plugins.gpu.config.TextureResolution;
import net.runelite.client.plugins.gpu.config.TintMode;
import net.runelite.client.plugins.gpu.config.UIScalingMode;
import net.runelite.client.plugins.gpu.template.Template;
import net.runelite.client.plugins.gpu.util.ColorAttachmentList;
import net.runelite.client.plugins.gpu.util.GLBuffer;
import net.runelite.client.plugins.gpu.util.GpuFloatBuffer;
import net.runelite.client.plugins.gpu.util.GpuIntBuffer;
import net.runelite.client.plugins.gpu.util.ReferenceCounter;
import net.runelite.client.plugins.gpu.util.ShaderException;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.OSType;
import net.runelite.client.util.SunCalc;
import org.jocl.CL;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.clCreateFromGLBuffer;

@PluginDescriptor(
	name = "GPU",
	description = "Utilizes the GPU",
	enabledByDefault = false,
	tags = {"fog", "draw distance"},
	loadInSafeMode = false
)
@Slf4j
public class GpuPlugin extends Plugin implements DrawCallbacks
{
	static final String CONFIG_GROUP_KEY = "gpu";

	// This is the maximum number of triangles the compute shaders support
	static final int MAX_TRIANGLE = 4096;
	static final int SMALL_TRIANGLE_COUNT = 512;
	private static final int FLAG_SCENE_BUFFER = Integer.MIN_VALUE;
	private static final int DEFAULT_DISTANCE = 25;
	static final int MAX_DISTANCE = 90;
	static final int MAX_FOG_DEPTH = 100;

	@Inject
	private Client client;

	@Inject
	private OpenCLManager openCLManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private GpuPluginConfig config;

	@Inject
	private TextureManager textureManager;

	@Inject
	private SceneUploader sceneUploader;

	@Inject
	private DrawManager drawManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ConfigManager configManager;

	enum ComputeMode
	{
		NONE,
		OPENGL,
		OPENCL
	}

	private ComputeMode computeMode = ComputeMode.NONE;

	private Canvas canvas;
	private JAWTWindow jawtWindow;
	private GL4 gl;
	private GLContext glContext;
	private GLDrawable glDrawable;

	static final Shader PROGRAM = new Shader()
		.add(GL4.GL_VERTEX_SHADER, "vert.glsl")
		.add(GL4.GL_FRAGMENT_SHADER, "frag.glsl");

	static final Shader UI_PROGRAM = new Shader()
		.add(GL4.GL_VERTEX_SHADER, "vertui.glsl")
		.add(GL4.GL_FRAGMENT_SHADER, "fragui.glsl");

	static final Shader COMPUTE_PROGRAM = new Shader()
		.add(GL4.GL_COMPUTE_SHADER, "compute/comp.glsl");

	static final Shader SMALL_COMPUTE_PROGRAM = new Shader()
		.add(GL4.GL_COMPUTE_SHADER, "compute/comp_small.glsl");

	static final Shader UNORDERED_COMPUTE_PROGRAM = new Shader()
		.add(GL4.GL_COMPUTE_SHADER, "compute/comp_unordered.glsl");

	static final Shader SHADOW_PROGRAM = new Shader()
		.add(GL4.GL_VERTEX_SHADER, "shadows/vert.glsl")
		.add(GL4.GL_FRAGMENT_SHADER, "shadows/frag.glsl");

	static final Shader POST_PROCESSING_PROGRAM = new Shader()
		.add(GL4.GL_FRAGMENT_SHADER, "post_processing/frag.glsl");

	static final Shader GAUSSIAN_BLUR_PROGRAM = new Shader()
		.add(GL4.GL_FRAGMENT_SHADER, "post_processing/gaussian_blur.glsl");

	static final String LINUX_VERSION_HEADER =
		"#version 420\n" +
			"#extension GL_ARB_compute_shader : require\n" +
			"#extension GL_ARB_shader_storage_buffer_object : require\n" +
			"#extension GL_ARB_explicit_attrib_location : require\n";
	static final String WINDOWS_VERSION_HEADER = "#version 430\n";

	static final Template template;
	static
	{
		String versionHeader = OSType.getOSType() == OSType.Linux ? LINUX_VERSION_HEADER : WINDOWS_VERSION_HEADER;
		template = new Template();
		template.add(key ->
		{
			if ("version_header".equals(key))
			{
				return versionHeader;
			}
			return null;
		});
		template.addInclude(GpuPlugin.class);
	}

	private int glProgram;
	private int glUiProgram;
	private int glComputeProgram;
	private int glSmallComputeProgram;
	private int glUnorderedComputeProgram;
	private int glShadowProgram;
	private int glPostProcessingProgram;
	private int glGaussianBlurProgram;

	// Reference counters for shader programs
	private ReferenceCounter refCountPostProcessingProgram = new ReferenceCounter();
	private ReferenceCounter refCountGaussianBlurProgram = new ReferenceCounter();

	private int vaoHandle;

	private int interfaceTexture;

	private int vaoUiHandle;
	private int vboUiHandle;

	private int vaoQuad;
	private int vboQuad;

	private int fboSceneHandle;
	private int[] texSceneHandles;
	private int[] rboSceneHandles;
	private int fboPostProcessingHandle;
	private int[] texPostProcessingHandles;
	private ColorAttachmentList fboSceneColorAttachmentList;

	// Shadows
	private int SHADOW_WIDTH;
	private int SHADOW_HEIGHT;
	private int fboDepthMap;
	private int texDepthMap;

	// Translucent shadows
	private int fboShadowColor;
	private int texShadowColorDepthMap;
	private int texShadowColorMap;

	// Post-processing
	private int texPostProcessingColor;
	private int texPostProcessingBloom;
	private int[] fboScenePingPong;
	private int[] texScenePingPong;

	// scene vertex buffer
	private final GLBuffer sceneVertexBuffer = new GLBuffer();
	// scene uv buffer
	private final GLBuffer sceneUvBuffer = new GLBuffer();

	private final GLBuffer tmpVertexBuffer = new GLBuffer(); // temporary scene vertex buffer
	private final GLBuffer tmpUvBuffer = new GLBuffer(); // temporary scene uv buffer
	private final GLBuffer tmpModelBufferLarge = new GLBuffer(); // scene model buffer, large
	private final GLBuffer tmpModelBufferSmall = new GLBuffer(); // scene model buffer, small
	private final GLBuffer tmpModelBufferUnordered = new GLBuffer(); // scene model buffer, unordered
	private final GLBuffer tmpOutBuffer = new GLBuffer(); // target vertex buffer for compute shaders
	private final GLBuffer tmpOutUvBuffer = new GLBuffer(); // target uv buffer for compute shaders

	private int textureArrayId;

	private final GLBuffer uniformBuffer = new GLBuffer();
	private final float[] textureOffsets = new float[128];

	private GpuIntBuffer vertexBuffer;
	private GpuFloatBuffer uvBuffer;

	private GpuIntBuffer modelBufferUnordered;
	private GpuIntBuffer modelBufferSmall;
	private GpuIntBuffer modelBuffer;

	private int unorderedModels;

	/**
	 * number of models in small buffer
	 */
	private int smallModels;

	/**
	 * number of models in large buffer
	 */
	private int largeModels;

	/**
	 * offset in the target buffer for model
	 */
	private int targetBufferOffset;

	/**
	 * offset into the temporary scene vertex buffer
	 */
	private int tempOffset;

	/**
	 * offset into the temporary scene uv buffer
	 */
	private int tempUvOffset;

	private int lastCanvasWidth;
	private int lastCanvasHeight;
	private int lastStretchedCanvasWidth;
	private int lastStretchedCanvasHeight;
	private int lastAnisotropicFilteringLevel = -1;

	// State variables
	private boolean enableShadows;
	private boolean enableShadowTranslucency;
	private boolean useFbo;
	private boolean invalidateFbo;
	private boolean enableMultisampling;
	private boolean enablePostProcessing;
	private boolean enableBloom;
	private BlurKernel shadowBlurKernel;

	private int yaw;
	private int pitch;
	// fields for non-compute draw
	private boolean drawingModel;
	private int modelX, modelY, modelZ;
	private int modelOrientation;

	// Main uniforms
	private int uniBrightness;
	private int uniColorBlindMode;
	private int uniDrawDistance;
	private int uniEnableShadowTranslucency;
	private int uniEnableShadows;
	private int uniFogColor;
	private int uniFogDepth;
	private int uniLightSpaceProjectionMatrix;
	private int uniProjectionMatrix;
	private int uniSmoothBanding;
	private int uniTex;
	private int uniTexSamplingMode;
	private int uniTexSourceDimensions;
	private int uniTexTargetDimensions;
	private int uniTextureLightMode;
	private int uniTextureOffsets;
	private int uniTextures;
	private int uniTintMode;
	private int uniDistanceFadeMode;
	private int uniUiAlphaOverlay;
	private int uniUiColorBlindMode;
	private int uniUseFog;
	private int uniEnableDebug;

	// TODO: shared uniform block between main and shadow programs

	// Shadow uniforms
	private int uniShadowMappingTechnique;
	private int uniShadowBrightness;
	private int uniShadowDepthMap;
	private int uniShadowColorDepthMap;
	private int uniShadowColorMap;
	private int uniShadowMappingKernelSize;
	private int uniShadowOpacity;
	private int uniShadowColorIntensity;
	private int uniShadowLightSpaceProjectionMatrix;
	private int uniShadowRenderPass;
	private int uniShadowSmoothBanding;
	private int uniShadowTextureLightMode;
	private int uniShadowTextureOffsets;
	private int uniShadowTextures;
	private int uniShadowPitch;
	private int uniShadowYaw;
	private int uniShadowDistance;

	// Compute uniforms
	private int uniBlockLarge;
	private int uniBlockMain;
	private int uniBlockSmall;

	// Post-processing uniforms
	private int uniPostProcessingColorTexture;
	private int uniPostProcessingBloomTexture;
	private int uniGaussianBlurDirection;
	private int uniGaussianBlurKernel;
	private int uniGaussianBlurKernelSize;

	@Override
	protected void startUp()
	{
		clientThread.invoke(() ->
		{
			try
			{
				drawingModel = false;

				canvas = client.getCanvas();

				if (!canvas.isDisplayable())
				{
					return false;
				}

				canvas.setIgnoreRepaint(true);

				vertexBuffer = new GpuIntBuffer();
				uvBuffer = new GpuFloatBuffer();

				modelBufferUnordered = new GpuIntBuffer();
				modelBufferSmall = new GpuIntBuffer();
				modelBuffer = new GpuIntBuffer();

				if (log.isDebugEnabled())
				{
					System.setProperty("jogl.debug", "true");
				}

				GLProfile.initSingleton();

				invokeOnMainThread(() ->
				{
					GLProfile glProfile = GLProfile.get(GLProfile.GL4);

					GLCapabilities glCaps = new GLCapabilities(glProfile);
					AWTGraphicsConfiguration awtConfig = AWTGraphicsConfiguration.create(canvas.getGraphicsConfiguration(), glCaps, glCaps);

					jawtWindow = NewtFactoryAWT.getNativeWindow(canvas, awtConfig);
					canvas.setFocusable(true);

					GLDrawableFactory glDrawableFactory = GLDrawableFactory.getFactory(glProfile);

					jawtWindow.lockSurface();
					try
					{
						glDrawable = glDrawableFactory.createGLDrawable(jawtWindow);
						glDrawable.setRealized(true);

						glContext = glDrawable.createContext(null);
						if (log.isDebugEnabled())
						{
							// Debug config on context needs to be set before .makeCurrent call
							glContext.enableGLDebugMessage(true);
						}
					}
					finally
					{
						jawtWindow.unlockSurface();
					}

					int res = glContext.makeCurrent();
					if (res == GLContext.CONTEXT_NOT_CURRENT)
					{
						throw new GLException("Unable to make context current");
					}

					// Surface needs to be unlocked on X11 window otherwise input is blocked
					if (jawtWindow instanceof X11JAWTWindow && jawtWindow.getLock().isLocked())
					{
						jawtWindow.unlockSurface();
					}

					this.gl = glContext.getGL().getGL4();
					gl.setSwapInterval(0);

					if (log.isDebugEnabled())
					{
						gl.glEnable(GL_DEBUG_OUTPUT);

						// Suppress warning messages which flood the log on NVIDIA systems.
						gl.getContext().glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DEBUG_TYPE_OTHER,
							GL_DEBUG_SEVERITY_NOTIFICATION, 0, null, 0, false);
					}

					initGLBuffers();
					try
					{
						initAllPrograms();
					}
					catch (ShaderException ex)
					{
						throw new RuntimeException(ex);
					}
				});

				client.setDrawCallbacks(this);
				client.setGpu(true);

				// force rebuild of main buffer provider to enable alpha channel
				client.resizeCanvas();

				// increase size of model cache for dynamic objects since we are extending scene size
				NodeCache cachedModels2 = client.getCachedModels2();
				cachedModels2.setCapacity(256);
				cachedModels2.setRemainingCapacity(256);
				cachedModels2.reset();

				if (client.getGameState() == GameState.LOGGED_IN)
				{
					invokeOnMainThread(this::uploadScene);
				}
			}
			catch (Throwable e)
			{
				log.error("Error starting GPU plugin", e);

				SwingUtilities.invokeLater(() ->
				{
					try
					{
						pluginManager.setPluginEnabled(this, false);
						pluginManager.stopPlugin(this);
					}
					catch (PluginInstantiationException ex)
					{
						log.error("error stopping plugin", ex);
					}
				});

				shutDown();
			}
			return true;
		});
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(() ->
		{
			client.setGpu(false);
			client.setDrawCallbacks(null);

			invokeOnMainThread(() ->
			{
				openCLManager.cleanup();

				if (gl != null)
				{
					if (textureArrayId != -1)
					{
						textureManager.freeTextureArray(gl, textureArrayId);
						textureArrayId = -1;
					}

					openCLManager.cleanup();

					shutdownAllPrograms();
					shutdownGLBuffers();
				}

				if (jawtWindow != null)
				{
					if (!jawtWindow.getLock().isLocked())
					{
						jawtWindow.lockSurface();
					}

					if (glContext != null)
					{
						glContext.destroy();
					}

					// this crashes on osx when the plugin is turned back on, don't know why
					// we'll just leak the window...
					if (OSType.getOSType() != OSType.MacOS)
					{
						NewtFactoryAWT.destroyNativeWindow(jawtWindow);
					}
				}
			});

			GLProfile.shutdown();

			jawtWindow = null;
			gl = null;
			glDrawable = null;
			glContext = null;

			vertexBuffer = null;
			uvBuffer = null;

			modelBufferSmall = null;
			modelBuffer = null;
			modelBufferUnordered = null;

			lastAnisotropicFilteringLevel = -1;

			// force main buffer provider rebuild to turn off alpha channel
			client.resizeCanvas();
		});
	}

	@Provides
	GpuPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GpuPluginConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(CONFIG_GROUP_KEY))
		{
			clientThread.invokeLater(() ->
			{
				Runnable revertChange = () ->
				{
					// TODO: Although this reverts the config, it doesn't display the change until the config panel is reloaded
					configManager.setConfiguration(event.getGroup(), event.getKey(), event.getOldValue());

					SwingUtilities.invokeLater(() ->
						JOptionPane.showMessageDialog(jawtWindow.getAWTComponent(),
							"Your GPU appears to not support this feature.",
							"OpenGL Error", JOptionPane.ERROR_MESSAGE));
				};
				boolean enable = Boolean.TRUE.toString().equals(event.getNewValue());
				invokeOnMainThread(() ->
				{
					if (event.getKey().equals("enableShadows"))
					{
						if (enable)
						{
							tryShaderInit(this::initShadowProgram, revertChange);
						}
						else
						{
							shutdownShadowFbo();
						}
					}
					else if (event.getKey().equalsIgnoreCase("enableShadowTranslucency"))
					{
						if (enable)
						{
							if (enableShadows)
							{
								initShadowColorFilterFbo();
							}
						}
						else
						{
							shutdownShadowColorFilterFbo();
						}
					}
					else if (event.getKey().equals("shadowResolution"))
					{
						if (enableShadows)
						{
							shutdownShadowFbo();
							initShadowFbo();
						}
					}
					else if (event.getKey().equals("enableBloom") || event.getKey().equals("antiAliasingMode"))
					{
						if (event.getKey().equals("enableBloom"))
						{
							if (enable)
							{
								tryShaderInit(this::acquireGaussianBlurProgram, revertChange);
							}
							else
							{
								releaseGaussianBlurProgram();
							}
						}

						invalidateFbo = true;
						useFbo = true;
					}
					else if (event.getKey().equals("useComputeShaders"))
					{
						if (enable)
						{
							tryShaderInit(this::initComputePrograms, revertChange);
						}
						else
						{
							shutdownComputePrograms();
						}
					}
				});
			});
		}
	}

	private void initAllPrograms() throws ShaderException
	{
		initBasePrograms();

		if (config.useComputeShaders())
		{
			tryShaderInit(this::initComputePrograms, () ->
				configManager.setConfiguration(CONFIG_GROUP_KEY, "useComputeShaders", false));
		}

		if (config.enableShadows())
		{
			tryShaderInit(this::initShadowProgram, () ->
				configManager.setConfiguration(CONFIG_GROUP_KEY, "enableShadows", false));
		}

		enablePostProcessing = config.enableBloom();
		if (enablePostProcessing)
		{
			tryShaderInit(this::acquireGaussianBlurProgram, () ->
			{
				// Disable all toggles which need post-processing to work
				configManager.setConfiguration(CONFIG_GROUP_KEY, "enableBloom", false);
			});
		}
	}

	private void shutdownAllPrograms()
	{
		shutdownBasePrograms();
		shutdownComputePrograms();
		shutdownShadowProgram();
		releaseGaussianBlurProgram();
	}

	private void initBasePrograms() throws ShaderException
	{
		textureArrayId = -1;
		lastCanvasWidth = lastCanvasHeight = -1;
		lastStretchedCanvasWidth = lastStretchedCanvasHeight = -1;

		glProgram = PROGRAM.compile(gl, template, false);
		glUiProgram = UI_PROGRAM.compile(gl, template);

		initBaseVaos();
		initBaseUniforms();
		validateMainProgram();

		initInterfaceTexture();

		useFbo = true;
		invalidateFbo = true;
	}

	private void validateMainProgram() throws ShaderException
	{
		gl.glUseProgram(glProgram);

		gl.glUniform1i(uniTextures, 1); // texture sampler array is bound to texture1
		gl.glUniform1i(uniShadowDepthMap, 2);
		gl.glUniform1i(uniShadowColorMap, 3);
		gl.glUniform1i(uniShadowColorDepthMap, 4);

		Shader.validate(gl, glProgram);

		gl.glUseProgram(0);
		gl.glActiveTexture(GL_TEXTURE0);
	}

	private void shutdownBasePrograms()
	{
		if (glProgram != 0)
		{
			gl.glDeleteProgram(glProgram);
			glProgram = 0;
		}

		if (glUiProgram != 0)
		{
			gl.glDeleteProgram(glUiProgram);
			glUiProgram = 0;
		}

		destroyGlBuffer(uniformBuffer);

		shutdownInterfaceTexture();
		shutdownBaseVaos();
		shutdownSceneFbo();
	}

	private void initComputePrograms() throws ShaderException
	{
		computeMode = config.useComputeShaders()
			? (OSType.getOSType() == OSType.MacOS ? ComputeMode.OPENCL : ComputeMode.OPENGL)
			: ComputeMode.NONE;

		unorderedModels = smallModels = largeModels = 0;

		if (computeMode == ComputeMode.OPENGL)
		{
			glComputeProgram = COMPUTE_PROGRAM.compile(gl, template);
			glSmallComputeProgram = SMALL_COMPUTE_PROGRAM.compile(gl, template);
			glUnorderedComputeProgram = UNORDERED_COMPUTE_PROGRAM.compile(gl, template);

			uniBlockSmall = gl.glGetUniformBlockIndex(glSmallComputeProgram, "uniforms");
			uniBlockLarge = gl.glGetUniformBlockIndex(glComputeProgram, "uniforms");
		}
		else if (computeMode == ComputeMode.OPENCL)
		{
			openCLManager.init(gl);
		}
	}

	private void shutdownComputePrograms()
	{
		if (glComputeProgram != 0)
		{
			gl.glDeleteProgram(glComputeProgram);
			glComputeProgram = 0;
		}

		if (glSmallComputeProgram != 0)
		{
			gl.glDeleteProgram(glSmallComputeProgram);
			glSmallComputeProgram = 0;
		}

		if (glUnorderedComputeProgram != 0)
		{
			gl.glDeleteProgram(glUnorderedComputeProgram);
			glUnorderedComputeProgram = 0;
		}
	}

	private void initShadowProgram() throws ShaderException
	{
		glShadowProgram = SHADOW_PROGRAM.compile(gl, template);

		uniShadowLightSpaceProjectionMatrix = gl.glGetUniformLocation(glShadowProgram, "lightSpaceProjectionMatrix");
		uniShadowRenderPass = gl.glGetUniformLocation(glShadowProgram, "renderPass");
		uniShadowBrightness = gl.glGetUniformLocation(glShadowProgram, "brightness");
		uniShadowSmoothBanding = gl.glGetUniformLocation(glShadowProgram, "smoothBanding");
		uniShadowTextures = gl.glGetUniformLocation(glShadowProgram, "textures");
		uniShadowTextureOffsets = gl.glGetUniformLocation(glShadowProgram, "textureOffsets");
		uniShadowTextureLightMode = gl.glGetUniformLocation(glShadowProgram, "textureLightMode");

		initShadowFbo();
	}

	private void shutdownShadowProgram()
	{
		if (glShadowProgram != 0)
		{
			gl.glDeleteProgram(glShadowProgram);
			glShadowProgram = 0;
		}

		shutdownShadowFbo();
	}

	private void acquirePostProcessingProgram() throws ShaderException
	{
		if (refCountPostProcessingProgram.increment())
		{
			glPostProcessingProgram = POST_PROCESSING_PROGRAM.compile(gl, template);

			uniPostProcessingColorTexture = gl.glGetUniformLocation(glGaussianBlurProgram, "texColor");
			uniPostProcessingBloomTexture = gl.glGetUniformLocation(glGaussianBlurProgram, "texBloom");
		}
	}

	private void releasePostProcessingProgram()
	{
		if (refCountPostProcessingProgram.decrement())
		{
			gl.glDeleteProgram(glPostProcessingProgram);
			glPostProcessingProgram = 0;
		}
	}

	private void acquireGaussianBlurProgram() throws ShaderException
	{
		if (refCountGaussianBlurProgram.increment())
		{
			glGaussianBlurProgram = GAUSSIAN_BLUR_PROGRAM.compile(gl, template);

			uniGaussianBlurDirection = gl.glGetUniformLocation(glGaussianBlurProgram, "direction");
			uniGaussianBlurKernel = gl.glGetUniformLocation(glGaussianBlurProgram, "halfKernel");
			uniGaussianBlurKernelSize = gl.glGetUniformLocation(glGaussianBlurProgram, "kernelSize");
		}
	}

	private void releaseGaussianBlurProgram()
	{
		if (refCountGaussianBlurProgram.decrement())
		{
			gl.glDeleteProgram(glGaussianBlurProgram);
			glGaussianBlurProgram = 0;
		}
	}

	private void initBaseUniforms()
	{
		// Uniform block
		initGlBuffer(uniformBuffer);

		IntBuffer uniformBuf = GpuIntBuffer.allocateDirect(8 + 2048 * 4);
		uniformBuf.put(new int[8]); // uniform block
		final int[] pad = new int[2];
		for (int i = 0; i < 2048; i++)
		{
			uniformBuf.put(Perspective.SINE[i]);
			uniformBuf.put(Perspective.COSINE[i]);
			uniformBuf.put(pad); // ivec2 alignment in std140 is 16 bytes
		}
		uniformBuf.flip();

		updateBuffer(uniformBuffer, GL_UNIFORM_BUFFER, uniformBuf.limit() * Integer.BYTES, uniformBuf, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

		// Regular uniforms
		uniProjectionMatrix = gl.glGetUniformLocation(glProgram, "projectionMatrix");
		uniBrightness = gl.glGetUniformLocation(glProgram, "brightness");
		uniSmoothBanding = gl.glGetUniformLocation(glProgram, "smoothBanding");
		uniUseFog = gl.glGetUniformLocation(glProgram, "useFog");
		uniFogColor = gl.glGetUniformLocation(glProgram, "fogColor");
		uniFogDepth = gl.glGetUniformLocation(glProgram, "fogDepth");
		uniDrawDistance = gl.glGetUniformLocation(glProgram, "drawDistance");
		uniColorBlindMode = gl.glGetUniformLocation(glProgram, "colorBlindMode");
		uniTextureLightMode = gl.glGetUniformLocation(glProgram, "textureLightMode");
		uniTintMode = gl.glGetUniformLocation(glProgram, "tintMode");
		uniDistanceFadeMode = gl.glGetUniformLocation(glProgram, "distanceFadeMode");

		uniTex = gl.glGetUniformLocation(glUiProgram, "tex");
		uniTexSamplingMode = gl.glGetUniformLocation(glUiProgram, "samplingMode");
		uniTexTargetDimensions = gl.glGetUniformLocation(glUiProgram, "targetDimensions");
		uniTexSourceDimensions = gl.glGetUniformLocation(glUiProgram, "sourceDimensions");
		uniUiColorBlindMode = gl.glGetUniformLocation(glUiProgram, "colorBlindMode");
		uniUiAlphaOverlay = gl.glGetUniformLocation(glUiProgram, "alphaOverlay");
		uniTextures = gl.glGetUniformLocation(glProgram, "textures");
		uniTextureOffsets = gl.glGetUniformLocation(glProgram, "textureOffsets");

		uniBlockMain = gl.glGetUniformBlockIndex(glProgram, "uniforms");

		// Shadow uniforms
		uniEnableShadows = gl.glGetUniformLocation(glProgram, "enableShadows");
		uniEnableShadowTranslucency = gl.glGetUniformLocation(glProgram, "enableShadowTranslucency");
		uniShadowOpacity = gl.glGetUniformLocation(glProgram, "shadowOpacity");
		uniShadowColorIntensity = gl.glGetUniformLocation(glProgram, "shadowColorIntensity");
		uniShadowMappingTechnique = gl.glGetUniformLocation(glProgram, "shadowMappingTechnique");

		uniShadowDepthMap = gl.glGetUniformLocation(glProgram, "shadowDepthMap");
		uniShadowColorMap = gl.glGetUniformLocation(glProgram, "shadowColorMap");
		uniShadowColorDepthMap = gl.glGetUniformLocation(glProgram, "shadowColorDepthMap");
		uniLightSpaceProjectionMatrix = gl.glGetUniformLocation(glProgram, "lightSpaceProjectionMatrix");

		// Miscellaneous
		uniEnableDebug = gl.glGetUniformLocation(glProgram, "enableDebug");
		uniShadowMappingKernelSize = gl.glGetUniformLocation(glProgram, "shadowMappingKernelSize");
		uniShadowPitch = gl.glGetUniformLocation(glProgram, "shadowPitch");
		uniShadowYaw = gl.glGetUniformLocation(glProgram, "shadowYaw");
		uniShadowDistance = gl.glGetUniformLocation(glProgram, "shadowDistance");
	}

	private void initSceneFbo(int width, int height)
	{
		invalidateFbo = false;

		final int colorFormat = GL_RGBA16F;
		final int maxSamples = glGetInteger(gl, GL_MAX_SAMPLES);
		final int aaSamples = Math.min(config.antiAliasingMode().getSamples(), maxSamples);
		enableBloom = config.enableBloom();
		enableMultisampling = aaSamples > 1;
		enablePostProcessing = enableBloom;
		useFbo = enableMultisampling || enablePostProcessing;

		// TODO: state variables need to be controlled separately, i.e. when shader programs are enabled

		if (!useFbo)
		{
			return;
		}

		final boolean requireScenePingPongFbo = enableBloom;

		fboSceneColorAttachmentList = new ColorAttachmentList(2);
		int sceneIdx = fboSceneColorAttachmentList.add(GL_COLOR_ATTACHMENT0);
		int bloomIdx = enableBloom ? fboSceneColorAttachmentList.add(GL_COLOR_ATTACHMENT1) : -1;

		// Create and bind the FBO
		fboSceneHandle = glGenFrameBuffer(gl);
		gl.glBindFramebuffer(GL_FRAMEBUFFER, fboSceneHandle);

		// Create texture handles
		texSceneHandles = new int[fboSceneColorAttachmentList.length];
		gl.glGenTextures(texSceneHandles.length, texSceneHandles, 0);

		if (enableMultisampling)
		{
			// Create multisampling render buffers for each color attachment
			rboSceneHandles = new int[texSceneHandles.length];
			gl.glGenRenderbuffers(texSceneHandles.length, rboSceneHandles, 0);

			gl.glBindRenderbuffer(GL_RENDERBUFFER, rboSceneHandles[sceneIdx]);
			gl.glRenderbufferStorageMultisample(GL_RENDERBUFFER, aaSamples, colorFormat, width, height);
			gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rboSceneHandles[sceneIdx]);

			gl.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texSceneHandles[sceneIdx]);
			gl.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, aaSamples, colorFormat, width, height, true);
			gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, texSceneHandles[sceneIdx], 0);

			if (enableBloom)
			{
				// Multisampling of bloom isn't really useful, but it seems like that's the only way not requiring another render pass
				gl.glBindRenderbuffer(GL_RENDERBUFFER, rboSceneHandles[bloomIdx]);
				gl.glRenderbufferStorageMultisample(GL_RENDERBUFFER, aaSamples, colorFormat, width, height);
				gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_RENDERBUFFER, rboSceneHandles[bloomIdx]);

				gl.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texSceneHandles[bloomIdx]);
				gl.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, aaSamples, colorFormat, width, height, true);
				gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D_MULTISAMPLE, texSceneHandles[bloomIdx], 0);
			}

			// Reset
			gl.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
			gl.glBindRenderbuffer(GL_RENDERBUFFER, 0);

			if (enablePostProcessing)
			{
				// Create non-multisampled FBO for post-processing
				fboPostProcessingHandle = glGenFrameBuffer(gl);
				gl.glBindFramebuffer(GL_FRAMEBUFFER, fboPostProcessingHandle);

				// Create texture handles
				texPostProcessingHandles = new int[fboSceneColorAttachmentList.length];
				gl.glGenTextures(texPostProcessingHandles.length, texPostProcessingHandles, 0);

				gl.glBindTexture(GL_TEXTURE_2D, texPostProcessingHandles[sceneIdx]);
				gl.glTexImage2D(GL_TEXTURE_2D, 0, colorFormat, width, height, 0, GL_RGBA, GL_FLOAT, null);
				gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texPostProcessingHandles[sceneIdx], 0);

				if (enableBloom)
				{
					gl.glBindTexture(GL_TEXTURE_2D, texPostProcessingHandles[bloomIdx]);
					gl.glTexImage2D(GL_TEXTURE_2D, 0, colorFormat, width, height, 0, GL_RGBA, GL_FLOAT, null);
					gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, texPostProcessingHandles[bloomIdx], 0);
				}

				// Reset
				gl.glBindTexture(GL_TEXTURE_2D, 0);
			}
		}
		else
		{
			gl.glBindTexture(GL_TEXTURE_2D, texSceneHandles[sceneIdx]);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, colorFormat, width, height, 0, GL_RGBA, GL_FLOAT, null);
			gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texSceneHandles[sceneIdx], 0);

			if (enableBloom)
			{
				gl.glBindTexture(GL_TEXTURE_2D, texSceneHandles[bloomIdx]);
				gl.glTexImage2D(GL_TEXTURE_2D, 0, colorFormat, width, height, 0, GL_RGBA, GL_FLOAT, null);
				gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, texSceneHandles[bloomIdx], 0);
			}

			// Reset
			gl.glBindTexture(GL_TEXTURE_2D, 0);

			// Without multisampling, we don't need the extra FBO and textures for post-processing
			fboPostProcessingHandle = fboSceneHandle;
			texPostProcessingHandles = texSceneHandles;
		}

		if (requireScenePingPongFbo)
		{
			fboScenePingPong = new int[2];
			texScenePingPong = new int[2];
			gl.glGenFramebuffers(2, fboScenePingPong, 0);
			gl.glGenTextures(2, texScenePingPong, 0);
			for (int i = 0; i < 2; i++)
			{
				gl.glBindFramebuffer(GL_FRAMEBUFFER, fboScenePingPong[i]);
				gl.glBindTexture(GL_TEXTURE_2D, texScenePingPong[i]);
				gl.glTexImage2D(GL_TEXTURE_2D, 0, colorFormat, width, height, 0, GL_RGBA, GL_FLOAT, null);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
				gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texScenePingPong[i], 0);
			}
		}

		// Instruct OpenGL to draw to all of our color attachments
		gl.glDrawBuffers(fboSceneColorAttachmentList.length, fboSceneColorAttachmentList.attachments, 0);

		// Reset
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	private void shutdownSceneFbo()
	{
		if (texSceneHandles != null)
		{
			if (texSceneHandles == texPostProcessingHandles)
			{
				texPostProcessingHandles = null;
			}
			gl.glDeleteTextures(texSceneHandles.length, texSceneHandles, 0);
			texSceneHandles = null;
		}

		if (fboSceneHandle != 0)
		{
			if (fboSceneHandle == fboPostProcessingHandle)
			{
				fboPostProcessingHandle = 0;
			}
			glDeleteFrameBuffer(gl, fboSceneHandle);
			fboSceneHandle = 0;
		}

		if (rboSceneHandles != null)
		{
			gl.glDeleteRenderbuffers(rboSceneHandles.length, rboSceneHandles, 0);
			rboSceneHandles = null;
		}

		if (texPostProcessingHandles != null)
		{
			gl.glDeleteTextures(texPostProcessingHandles.length, texPostProcessingHandles, 0);
			texPostProcessingHandles = null;
		}

		if (fboPostProcessingHandle != 0)
		{
			glDeleteFrameBuffer(gl, fboPostProcessingHandle);
			fboPostProcessingHandle = 0;
		}

		if (fboScenePingPong != null)
		{
			gl.glDeleteFramebuffers(2, fboScenePingPong, 0);
			fboScenePingPong = null;
		}

		if (texScenePingPong != null)
		{
			gl.glDeleteTextures(2, texScenePingPong, 0);
			texScenePingPong = null;
		}
	}

	private void initBaseVaos()
	{
		// Create VAO
		vaoHandle = glGenVertexArrays(gl);

		// Create UI VAO
		vaoUiHandle = glGenVertexArrays(gl);
		// Create UI buffer
		vboUiHandle = glGenBuffers(gl);
		gl.glBindVertexArray(vaoUiHandle);

		FloatBuffer vboUiBuf = GpuFloatBuffer.allocateDirect(5 * 4);
		vboUiBuf.put(new float[]{
			// positions     // texture coords
			1f, 1f, 0.0f, 1.0f, 0f, // top right
			1f, -1f, 0.0f, 1.0f, 1f, // bottom right
			-1f, -1f, 0.0f, 0.0f, 1f, // bottom left
			-1f, 1f, 0.0f, 0.0f, 0f  // top left
		});
		vboUiBuf.rewind();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboUiHandle);
		gl.glBufferData(GL_ARRAY_BUFFER, vboUiBuf.capacity() * Float.BYTES, vboUiBuf, GL_STATIC_DRAW);

		// position attribute
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
		gl.glEnableVertexAttribArray(0);

		// texture coord attribute
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		gl.glEnableVertexAttribArray(1);

		// unbind VBO
		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private void shutdownBaseVaos()
	{
		glDeleteVertexArrays(gl, vaoHandle);
		vaoHandle = 0;

		glDeleteBuffer(gl, vboUiHandle);
		vboUiHandle = 0;

		glDeleteVertexArrays(gl, vaoUiHandle);
		vaoUiHandle = 0;
	}

	private void initGLBuffers()
	{
		initGlBuffer(sceneVertexBuffer);
		initGlBuffer(sceneUvBuffer);
		initGlBuffer(tmpVertexBuffer);
		initGlBuffer(tmpUvBuffer);
		initGlBuffer(tmpModelBufferLarge);
		initGlBuffer(tmpModelBufferSmall);
		initGlBuffer(tmpModelBufferUnordered);
		initGlBuffer(tmpOutBuffer);
		initGlBuffer(tmpOutUvBuffer);
	}

	private void initGlBuffer(GLBuffer glBuffer)
	{
		glBuffer.glBufferId = glGenBuffers(gl);
	}

	private void shutdownGLBuffers()
	{
		destroyGlBuffer(sceneVertexBuffer);
		destroyGlBuffer(sceneUvBuffer);

		destroyGlBuffer(tmpVertexBuffer);
		destroyGlBuffer(tmpUvBuffer);
		destroyGlBuffer(tmpModelBufferLarge);
		destroyGlBuffer(tmpModelBufferSmall);
		destroyGlBuffer(tmpModelBufferUnordered);
		destroyGlBuffer(tmpOutBuffer);
		destroyGlBuffer(tmpOutUvBuffer);
	}

	private void destroyGlBuffer(GLBuffer glBuffer)
	{
		if (glBuffer.glBufferId != -1)
		{
			glDeleteBuffer(gl, glBuffer.glBufferId);
			glBuffer.glBufferId = -1;
		}
		glBuffer.size = -1;

		if (glBuffer.cl_mem != null)
		{
			CL.clReleaseMemObject(glBuffer.cl_mem);
			glBuffer.cl_mem = null;
		}
	}

	private void updateShadowResolution()
	{
		int[] buf = new int[1];
		gl.glGetIntegerv(gl.GL_MAX_TEXTURE_SIZE, buf, 0);
		int maxTexSize = buf[0];

		TextureResolution res = config.shadowResolution();
		SHADOW_WIDTH = res.getWidth();
		SHADOW_HEIGHT = res.getHeight();

		if (maxTexSize < SHADOW_WIDTH || maxTexSize < SHADOW_HEIGHT)
		{
			log.debug("Can't apply selected shadow resolution. Using your GPUs max resolution of " + maxTexSize);
			SHADOW_WIDTH = maxTexSize;
			SHADOW_HEIGHT = maxTexSize;
		}
	}

	private void initShadowFbo()
	{
		// Only need to do this here, since this calls the color filter's init too
		updateShadowResolution();

		enableShadows = true;

		// Border clamping color
		FloatBuffer borderColor = FloatBuffer.wrap(new float[]{1.0f, 1.0f, 1.0f, 1.0f});

		// Create framebuffer
		fboDepthMap = glGenFrameBuffer(gl);

		// Create texture
		texDepthMap = glGenTexture(gl);
		gl.glBindTexture(GL_TEXTURE_2D, texDepthMap);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16,
			SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
		gl.glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);

		// Bind texture to framebuffer
		gl.glBindFramebuffer(GL_FRAMEBUFFER, fboDepthMap);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texDepthMap, 0);

		// We're only using the depth map, so disable the rest
		gl.glDrawBuffer(GL_NONE);
		gl.glReadBuffer(GL_NONE);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);

		if (config.enableShadowTranslucency())
		{
			initShadowColorFilterFbo();
		}
	}

	private void shutdownShadowFbo()
	{
		if (enableShadows)
		{
			enableShadows = false;
			if (fboDepthMap != 0)
			{
				glDeleteFrameBuffer(gl, fboDepthMap);
				fboDepthMap = 0;
			}
			if (texDepthMap != 0)
			{
				glDeleteTexture(gl, texDepthMap);
				texDepthMap = 0;
			}

			if (enableShadowTranslucency)
			{
				shutdownShadowColorFilterFbo();
			}
		}
	}

	private void initShadowColorFilterFbo()
	{
		invokeOnMainThread(() ->
		{
			enableShadowTranslucency = true;

			// Border clamping color
			FloatBuffer borderColor = FloatBuffer.wrap(new float[]{1.0f, 1.0f, 1.0f, 1.0f});

			// Create framebuffer
			fboShadowColor = glGenFrameBuffer(gl);

			// Create texture
			texShadowColorDepthMap = glGenTexture(gl);
			gl.glBindTexture(GL_TEXTURE_2D, texShadowColorDepthMap);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16,
				SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
			gl.glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);

			texShadowColorMap = glGenTexture(gl);
			gl.glBindTexture(GL_TEXTURE_2D, texShadowColorMap);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8,
				SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL_RGB, GL_FLOAT, null);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

			// Bind texture to FBO
			gl.glBindFramebuffer(GL_FRAMEBUFFER, fboShadowColor);
			gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texShadowColorDepthMap, 0);
			gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texShadowColorMap, 0);

			gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		});
	}

	private void shutdownShadowColorFilterFbo()
	{
		if (enableShadowTranslucency)
		{
			enableShadowTranslucency = false;
			if (fboShadowColor != 0)
			{
				glDeleteFrameBuffer(gl, fboShadowColor);
				fboShadowColor = 0;
			}
			if (texShadowColorDepthMap != 0)
			{
				glDeleteTexture(gl, texShadowColorDepthMap);
				texShadowColorDepthMap = 0;
			}
			if (texShadowColorMap != 0)
			{
				glDeleteTexture(gl, texShadowColorMap);
				texShadowColorMap = 0;
			}
		}
	}

	private void initInterfaceTexture()
	{
		interfaceTexture = glGenTexture(gl);
		gl.glBindTexture(GL_TEXTURE_2D, interfaceTexture);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glBindTexture(GL_TEXTURE_2D, 0);
	}

	private void shutdownInterfaceTexture()
	{
		glDeleteTexture(gl, interfaceTexture);
		interfaceTexture = 0;
	}

	@Override
	public void drawScene(int cameraX, int cameraY, int cameraZ, int cameraPitch, int cameraYaw, int plane)
	{
		yaw = client.getCameraYaw();
		pitch = client.getCameraPitch();

		final Scene scene = client.getScene();
		scene.setDrawDistance(getDrawDistance());

		invokeOnMainThread(() ->
		{
			// UBO. Only the first 32 bytes get modified here, the rest is the constant sin/cos table.
			// We can reuse the vertex buffer since it isn't used yet.
			vertexBuffer.clear();
			vertexBuffer.ensureCapacity(32);
			IntBuffer uniformBuf = vertexBuffer.getBuffer();
			uniformBuf
				.put(yaw)
				.put(pitch)
				.put(client.getCenterX())
				.put(client.getCenterY())
				.put(client.getScale())
				.put(cameraX)
				.put(cameraY)
				.put(cameraZ);
			uniformBuf.flip();

			gl.glBindBuffer(GL_UNIFORM_BUFFER, uniformBuffer.glBufferId);
			gl.glBufferSubData(GL_UNIFORM_BUFFER, 0, uniformBuf.limit() * Integer.BYTES, uniformBuf);
			gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

			gl.glBindBufferBase(GL_UNIFORM_BUFFER, 0, uniformBuffer.glBufferId);
			uniformBuf.clear();
		});
	}

	@Override
	public void postDrawScene()
	{
		invokeOnMainThread(this::postDraw);
	}

	private void postDraw()
	{
		if (computeMode == ComputeMode.NONE)
		{
			// Upload buffers
			vertexBuffer.flip();
			uvBuffer.flip();

			IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
			FloatBuffer uvBuffer = this.uvBuffer.getBuffer();

			updateBuffer(tmpVertexBuffer, GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, GL_DYNAMIC_DRAW, 0L);
			updateBuffer(tmpUvBuffer, GL_ARRAY_BUFFER, uvBuffer.limit() * Float.BYTES, uvBuffer, GL_DYNAMIC_DRAW, 0L);
			return;
		}

		// Upload buffers
		vertexBuffer.flip();
		uvBuffer.flip();
		modelBuffer.flip();
		modelBufferSmall.flip();
		modelBufferUnordered.flip();

		IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
		FloatBuffer uvBuffer = this.uvBuffer.getBuffer();
		IntBuffer modelBuffer = this.modelBuffer.getBuffer();
		IntBuffer modelBufferSmall = this.modelBufferSmall.getBuffer();
		IntBuffer modelBufferUnordered = this.modelBufferUnordered.getBuffer();

		// temp buffers
		updateBuffer(tmpVertexBuffer, GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpUvBuffer, GL_ARRAY_BUFFER, uvBuffer.limit() * Float.BYTES, uvBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);

		// model buffers
		updateBuffer(tmpModelBufferLarge, GL_ARRAY_BUFFER, modelBuffer.limit() * Integer.BYTES, modelBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpModelBufferSmall, GL_ARRAY_BUFFER, modelBufferSmall.limit() * Integer.BYTES, modelBufferSmall, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpModelBufferUnordered, GL_ARRAY_BUFFER, modelBufferUnordered.limit() * Integer.BYTES, modelBufferUnordered, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);

		// Output buffers
		updateBuffer(tmpOutBuffer,
			GL_ARRAY_BUFFER,
			targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
			null,
			GL_STREAM_DRAW,
			CL_MEM_WRITE_ONLY);
		updateBuffer(tmpOutUvBuffer,
			GL_ARRAY_BUFFER,
			targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
			null,
			GL_STREAM_DRAW,
			CL_MEM_WRITE_ONLY);

		if (computeMode == ComputeMode.OPENCL)
		{
			// The docs for clEnqueueAcquireGLObjects say all pending GL operations must be completed before calling
			// clEnqueueAcquireGLObjects, and recommends calling glFinish() as the only portable way to do that.
			// However no issues have been observed from not calling it, and so will leave disabled for now.
			// gl.glFinish();

			openCLManager.compute(
				unorderedModels, smallModels, largeModels,
				sceneVertexBuffer, sceneUvBuffer,
				tmpVertexBuffer, tmpUvBuffer,
				tmpModelBufferUnordered, tmpModelBufferSmall, tmpModelBufferLarge,
				tmpOutBuffer, tmpOutUvBuffer,
				uniformBuffer);
			return;
		}

		/*
		 * Compute is split into three separate programs: 'unordered', 'small', and 'large'
		 * to save on GPU resources. Small will sort <= 512 faces, large will do <= 4096.
		 */

		// Bind UBO to compute programs
		gl.glUniformBlockBinding(glSmallComputeProgram, uniBlockSmall, 0);
		gl.glUniformBlockBinding(glComputeProgram, uniBlockLarge, 0);

		// unordered
		gl.glUseProgram(glUnorderedComputeProgram);

		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferUnordered.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);

		gl.glDispatchCompute(unorderedModels, 1, 1);

		// small
		gl.glUseProgram(glSmallComputeProgram);

		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferSmall.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);

		gl.glDispatchCompute(smallModels, 1, 1);

		// large
		gl.glUseProgram(glComputeProgram);

		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferLarge.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		gl.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);

		gl.glDispatchCompute(largeModels, 1, 1);
	}

	@Override
	public void drawScenePaint(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z,
							   SceneTilePaint paint, int tileZ, int tileX, int tileY,
							   int zoom, int centerX, int centerY)
	{
		if (computeMode == ComputeMode.NONE)
		{
			targetBufferOffset += sceneUploader.upload(paint,
				tileZ, tileX, tileY,
				vertexBuffer, uvBuffer,
				Perspective.LOCAL_TILE_SIZE * tileX,
				Perspective.LOCAL_TILE_SIZE * tileY,
				true
			);
		}
		else if (paint.getBufferLen() > 0)
		{
			final int localX = tileX * Perspective.LOCAL_TILE_SIZE;
			final int localY = 0;
			final int localZ = tileY * Perspective.LOCAL_TILE_SIZE;

			GpuIntBuffer b = modelBufferUnordered;
			++unorderedModels;

			b.ensureCapacity(8);
			IntBuffer buffer = b.getBuffer();
			buffer.put(paint.getBufferOffset());
			buffer.put(paint.getUvBufferOffset());
			buffer.put(2);
			buffer.put(targetBufferOffset);
			buffer.put(FLAG_SCENE_BUFFER);
			buffer.put(localX).put(localY).put(localZ);

			targetBufferOffset += 2 * 3;
		}
	}

	@Override
	public void drawSceneModel(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z,
							   SceneTileModel model, int tileZ, int tileX, int tileY,
							   int zoom, int centerX, int centerY)
	{
		if (computeMode == ComputeMode.NONE)
		{
			targetBufferOffset += sceneUploader.upload(model,
				tileX, tileY,
				vertexBuffer, uvBuffer,
				tileX << Perspective.LOCAL_COORD_BITS, tileY << Perspective.LOCAL_COORD_BITS, true);
		}
		else if (model.getBufferLen() > 0)
		{
			final int localX = tileX * Perspective.LOCAL_TILE_SIZE;
			final int localY = 0;
			final int localZ = tileY * Perspective.LOCAL_TILE_SIZE;

			GpuIntBuffer b = modelBufferUnordered;
			++unorderedModels;

			b.ensureCapacity(8);
			IntBuffer buffer = b.getBuffer();
			buffer.put(model.getBufferOffset());
			buffer.put(model.getUvBufferOffset());
			buffer.put(model.getBufferLen() / 3);
			buffer.put(targetBufferOffset);
			buffer.put(FLAG_SCENE_BUFFER);
			buffer.put(localX).put(localY).put(localZ);

			targetBufferOffset += model.getBufferLen();
		}
	}

	@Override
	public void draw(int overlayColor)
	{
		invokeOnMainThread(() -> drawFrame(overlayColor));
	}

	private void resize(int canvasWidth, int canvasHeight)
	{
		if (canvasWidth != lastCanvasWidth || canvasHeight != lastCanvasHeight)
		{
			lastCanvasWidth = canvasWidth;
			lastCanvasHeight = canvasHeight;

			gl.glBindTexture(GL_TEXTURE_2D, interfaceTexture);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, canvasWidth, canvasHeight, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, null);
			gl.glBindTexture(GL_TEXTURE_2D, 0);

			if (OSType.getOSType() == OSType.MacOS && glDrawable instanceof GLFBODrawable)
			{
				// GLDrawables created with createGLDrawable() do not have a resize listener
				// I don't know why this works with Windows/Linux, but on OSX
				// it prevents JOGL from resizing its FBOs and underlying GL textures. So,
				// we manually trigger a resize here.
				GLFBODrawable glfboDrawable = (GLFBODrawable) glDrawable;
				glfboDrawable.resetSize(gl);
			}
		}
	}

	private void drawFrame(int overlayColor)
	{
		if (jawtWindow.getAWTComponent() != client.getCanvas())
		{
			// We inject code in the game engine mixin to prevent the client from doing canvas replacement,
			// so this should not ever be hit
			log.warn("Canvas invalidated!");
			shutDown();
			startUp();
			return;
		}

		if (client.getGameState() == GameState.LOADING || client.getGameState() == GameState.HOPPING)
		{
			// While the client is loading it doesn't draw
			return;
		}

		final int canvasHeight = client.getCanvasHeight();
		final int canvasWidth = client.getCanvasWidth();

		final int viewportHeight = client.getViewportHeight();
		final int viewportWidth = client.getViewportWidth();

		resize(canvasWidth, canvasHeight);

		if (useFbo)
		{
			final Dimension stretchedDimensions = client.getStretchedDimensions();

			final int stretchedCanvasWidth = client.isStretchedEnabled() ? stretchedDimensions.width : canvasWidth;
			final int stretchedCanvasHeight = client.isStretchedEnabled() ? stretchedDimensions.height : canvasHeight;

			// Re-create FBO when necessary
			if (invalidateFbo || lastStretchedCanvasWidth != stretchedCanvasWidth || lastStretchedCanvasHeight != stretchedCanvasHeight)
			{
				shutdownSceneFbo();
				initSceneFbo(stretchedCanvasWidth, stretchedCanvasHeight);

				lastStretchedCanvasWidth = stretchedCanvasWidth;
				lastStretchedCanvasHeight = stretchedCanvasHeight;
			}

			// useFbo gets set to false by initFbo if it's not needed
			if (useFbo)
			{
				gl.glEnable(GL_MULTISAMPLE);
				gl.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboSceneHandle);
			}
		}

		if (!enableMultisampling)
		{
			gl.glDisable(GL_MULTISAMPLE);
		}

		// Clear scene
		int sky = client.getSkyboxColor();
		gl.glClearColor((sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
		gl.glClear(GL_COLOR_BUFFER_BIT);

		// Draw 3d scene
		final TextureProvider textureProvider = client.getTextureProvider();
		if (textureProvider != null)
		{
			if (textureArrayId == -1)
			{
				// lazy init textures as they may not be loaded at plugin start.
				// this will return -1 and retry if not all textures are loaded yet, too.
				textureArrayId = textureManager.initTextureArray(textureProvider, gl);
			}

			final Texture[] textures = textureProvider.getTextures();
			int renderHeightOff = client.getViewportYOffset();
			int renderWidthOff = client.getViewportXOffset();
			int renderCanvasHeight = canvasHeight;
			int renderViewportHeight = viewportHeight;
			int renderViewportWidth = viewportWidth;

			// Setup anisotropic filtering
			final int anisotropicFilteringLevel = config.anisotropicFilteringLevel();

			if (textureArrayId != -1 && lastAnisotropicFilteringLevel != anisotropicFilteringLevel)
			{
				textureManager.setAnisotropicFilteringLevel(textureArrayId, anisotropicFilteringLevel, gl);
				lastAnisotropicFilteringLevel = anisotropicFilteringLevel;
			}

			if (client.isStretchedEnabled())
			{
				Dimension dim = client.getStretchedDimensions();
				renderCanvasHeight = dim.height;

				double scaleFactorY = dim.getHeight() / canvasHeight;
				double scaleFactorX = dim.getWidth() / canvasWidth;

				// Pad the viewport a little because having ints for our viewport dimensions can introduce off-by-one errors.
				final int padding = 1;

				// Ceil the sizes because even if the size is 599.1 we want to treat it as size 600 (i.e. render to the x=599 pixel).
				renderViewportHeight = (int) Math.ceil(scaleFactorY * (renderViewportHeight)) + padding * 2;
				renderViewportWidth = (int) Math.ceil(scaleFactorX * (renderViewportWidth)) + padding * 2;

				// Floor the offsets because even if the offset is 4.9, we want to render to the x=4 pixel anyway.
				renderHeightOff = (int) Math.floor(scaleFactorY * (renderHeightOff)) - padding;
				renderWidthOff = (int) Math.floor(scaleFactorX * (renderWidthOff)) - padding;
			}

			int vertexBuffer, uvBuffer;
			if (computeMode != ComputeMode.NONE)
			{
				if (computeMode == ComputeMode.OPENGL)
				{
					// Before reading the SSBOs written to from postDrawScene() we must insert a barrier
					gl.glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
				}
				else
				{
					// Wait for the command queue to finish, so that we know the compute is done
					openCLManager.finish();
				}

				// Draw using the output buffer of the compute
				vertexBuffer = tmpOutBuffer.glBufferId;
				uvBuffer = tmpOutUvBuffer.glBufferId;
			}
			else
			{
				// Only use the temporary buffers, which will contain the full scene
				vertexBuffer = tmpVertexBuffer.glBufferId;
				uvBuffer = tmpUvBuffer.glBufferId;
			}

			double shadowYaw = 0;
			double shadowPitch = 0;
			TintMode activeTintMode = config.tintMode();
			int finalShadowColorMap = texShadowColorMap;

			Matrix4 lightSpaceProjection = new Matrix4();
			float[] lightSpaceProjectionMatrix = null;
			if (enableShadows && client.getGameState() == GameState.LOGGED_IN)
			{
				shadowYaw = Math.toRadians(config.sunAngleHorizontal());
				shadowPitch = Math.toRadians(config.sunAngleVertical());

				if (config.useTimeBasedAngles())
				{
					double latitude = 0, longitude = 0;

					try
					{
						latitude = Double.parseDouble(config.latitude());
						longitude = Double.parseDouble(config.longitude());
					}
					catch (NumberFormatException ex)
					{
						log.debug("Invalid value for latitude or longitude coordinate");
						ex.printStackTrace();
					}

					long millis = System.currentTimeMillis();
					if (config.speedUpTime())
					{
						millis *= 86400D / 60; // 1 day passes every minute
					}

					double[] sunPos = SunCalc.getPosition(millis, latitude, longitude);
					double azimuth = sunPos[0];
					double zenith = sunPos[1];

					shadowPitch = zenith;
					shadowYaw = -azimuth;

					// Normalize pitch and yaw
					shadowPitch = (shadowPitch + 2 * Math.PI) % (Math.PI * 2);
					shadowYaw = (shadowYaw + 2 * Math.PI) % (Math.PI * 2);

					if (shadowPitch < 0 || shadowPitch > Math.PI)
					{
						activeTintMode = TintMode.NIGHT;

						double[] moonPos = SunCalc.getMoonPosition(millis, latitude, longitude);
						double[] moonIllumination = SunCalc.getMoonIllumination(millis, sunPos, moonPos);

						azimuth = moonPos[0];
						zenith = moonPos[1];

						double illumination = moonIllumination[0],
							phase = moonIllumination[1],
							angle = moonIllumination[2];

						shadowPitch = zenith;
						shadowYaw = -azimuth;

						// Normalize pitch and yaw
						shadowPitch = (shadowPitch + 2 * Math.PI) % (Math.PI * 2);
						shadowYaw = (shadowYaw + 2 * Math.PI) % (Math.PI * 2);
					}
				}

				gl.glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
				gl.glBindFramebuffer(GL_FRAMEBUFFER, fboDepthMap);

				if (shadowPitch < 0 || shadowPitch > Math.PI)
				{
					// The sun/moon is below the surface, so everything should be in shadow since OSRS is flat
					activeTintMode = TintMode.NIGHT;

					gl.glClearDepth(1);
					gl.glClear(GL_DEPTH_BUFFER_BIT);

					if (enableShadowTranslucency)
					{
						gl.glBindFramebuffer(GL_FRAMEBUFFER, fboShadowColor);
						gl.glClear(GL_DEPTH_BUFFER_BIT);
					}
				}
				else
				{
					int offsetX = 0, offsetY = 0, offsetZ = 0;
					Player player = client.getLocalPlayer();
					if (player != null)
					{
						LocalPoint loc = player.getLocalLocation();
						if (loc != null)
						{
							offsetX -= loc.getX();
							offsetY -= loc.getY();
							offsetZ -= 0;

							int[][][] tileHeights = client.getTileHeights();
							int plane = client.getPlane();
							try
							{
								offsetZ -= tileHeights[plane][loc.getSceneX()][loc.getSceneY()];
							}
							catch (Exception ex)
							{
								// Shouldn't get here
								log.warn(String.format("Non-existent tile: [%d][%d][%d]", plane, loc.getSceneX(), loc.getSceneY()));
							}
						}
					}

					int[][] bounds = getSceneBounds();

					float left = bounds[0][0];
					float right = bounds[0][1];
					float bottom = bounds[1][0];
					float top = bounds[1][1];

					float zNear = -5000;
					float zFar = 10000;

					float dx = right - left;
					float dy = top - bottom;

					// Scale the scene projection to fit inside the screen bounds
					float yawScale = 1 / (float) (1 + Math.abs(Math.sin(shadowYaw * 2)) * (Math.sqrt(2) - 1));
					float pitchScale = 1 + (float) Math.abs(1 - Math.sin(shadowPitch));
					lightSpaceProjection.scale(yawScale, yawScale * pitchScale, 1);

					// Calculate orthographic projection matrix for shadow mapping
					lightSpaceProjection.makeOrtho(
						-dx / 2,
						dx / 2,
						-dy / 2,
						dy / 2,
						zNear * 2, zFar);

					lightSpaceProjection.rotate((float) (Math.PI - shadowPitch), -1, 0, 0);
					lightSpaceProjection.rotate((float) shadowYaw, 0, 1, 0);

					lightSpaceProjection.translate(-dx / 2 - left, offsetZ, -dy / 2 - bottom);

					lightSpaceProjectionMatrix = lightSpaceProjection.getMatrix();

					gl.glUseProgram(glShadowProgram);

					// Bind light space projection matrix
					gl.glUniformMatrix4fv(uniShadowLightSpaceProjectionMatrix, 1, false, lightSpaceProjection.getMatrix(), 0);

					// Bind all uniforms required to determine exact colors of all objects
					gl.glUniform1f(uniShadowBrightness, (float) textureProvider.getBrightness());
					gl.glUniform1f(uniShadowSmoothBanding, config.smoothBanding() ? 0f : 1f);
					gl.glUniform1i(uniShadowTextures, 1); // texture sampler array is bound to texture1
					gl.glUniform2fv(uniShadowTextureOffsets, 128, textureOffsets, 0);
					gl.glUniform1f(uniShadowTextureLightMode, config.brightTextures() ? 1f : 0f);

					// Bind vertex and UV buffers
					gl.glBindVertexArray(vaoHandle);
					gl.glEnableVertexAttribArray(0);
					gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
					gl.glVertexAttribIPointer(0, 4, GL_INT, 0, 0);
					gl.glEnableVertexAttribArray(1);
					gl.glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
					gl.glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);

					// Enable depth testing so we can write depth values
					gl.glEnable(GL_DEPTH_TEST);

					// We just allow the GL to do face culling. Note this requires the priority renderer
					// to have logic to disregard culled faces in the priority depth testing.
					gl.glEnable(GL_CULL_FACE);

					// Clear depth map
					gl.glClearDepth(1);
					gl.glClear(GL_DEPTH_BUFFER_BIT);

					if (enableShadowTranslucency)
					{
						// Instruct the shadow shader to only keep opaque fragments
						gl.glUniform1i(uniShadowRenderPass, 0);
					}
					else
					{
						// Instruct the shadow shader to keep all fragments
						gl.glUniform1i(uniShadowRenderPass, -1);
					}

					// Perform the first render pass
					gl.glDrawArrays(GL_TRIANGLES, 0, targetBufferOffset);

					// Set up for the second render pass if enabled
					if (enableShadowTranslucency)
					{
						gl.glBindFramebuffer(GL_FRAMEBUFFER, fboShadowColor);

						// Init to white, fully transparent. Represents the shadow's starting point
						gl.glClearDepth(1);
						gl.glClearColor(1, 1, 1, 1);
						gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

						// Instruct the shadow shader to only keep translucent fragments
						gl.glUniform1i(uniShadowRenderPass, 1);

						gl.glEnable(GL_BLEND);

						// The best blending I could work out that doesn't invert colors
//						gl.glBlendEquation(GL_FUNC_ADD);
//						gl.glBlendFuncSeparate(
//							GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA,
//							GL_DST_ALPHA, GL_ZERO);

						// Order independent blending hack, but we end up with inverted colors
//						gl.glBlendEquationSeparate(
//							GL_FUNC_REVERSE_SUBTRACT,
//							GL_FUNC_ADD);
//						gl.glBlendFuncSeparate(
//							GL_SRC_ALPHA, GL_ONE,
//							GL_DST_ALPHA, GL_ZERO);

						// Same blending, but with pre-multiplied alpha
//						gl.glBlendEquation(GL_FUNC_REVERSE_SUBTRACT);
//						gl.glBlendFunc(GL_ONE, GL_ONE);
						gl.glBlendEquation(GL_FUNC_REVERSE_SUBTRACT);
						gl.glBlendFunc(GL_ONE, GL_ONE);

						// This makes it so no fragments get culled because of depth
						gl.glDepthFunc(GL_ALWAYS);

						switch (config.colorPassFaceCulling())
						{
							case BACK:
								gl.glEnable(GL_CULL_FACE);
								break;
							case DISABLE:
								gl.glDisable(GL_CULL_FACE);
								break;
							case FRONT:
								gl.glCullFace(GL_FRONT);
								break;
						}

						// Perform the second render pass
						gl.glDrawArrays(GL_TRIANGLES, 0, targetBufferOffset);

						// Blur the color map if any sort of blurring of the shadow map is done
						if (config.shadowMappingTechnique().getKernelSize() > 1)
						{
							finalShadowColorMap = applyBlur(texShadowColorMap, getShadowBlurKernel(), 1);
						}
					}

					// Cleanup
					// TODO: remove unneeded when done
					gl.glDisable(GL_DEPTH_TEST);
					gl.glDepthFunc(GL_LESS);
					gl.glDisable(GL_CULL_FACE);
					gl.glCullFace(GL_BACK);
					gl.glDisable(GL_BLEND);
					gl.glBlendEquation(GL_FUNC_ADD);
					gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				}

				// Rebind default FBO
				gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
			}

			glDpiAwareViewport(renderWidthOff, renderCanvasHeight - renderViewportHeight - renderHeightOff, renderViewportWidth, renderViewportHeight);

			gl.glUseProgram(glProgram);

			final int drawDistance = getDrawDistance();
			final int fogDepth = config.fogDepth();
			gl.glUniform1i(uniUseFog, fogDepth > 0 ? 1 : 0);
			gl.glUniform4f(uniFogColor, (sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
			gl.glUniform1i(uniFogDepth, fogDepth);
			gl.glUniform1i(uniDrawDistance, drawDistance * Perspective.LOCAL_TILE_SIZE);

			// Brightness happens to also be stored in the texture provider, so we use that
			gl.glUniform1f(uniBrightness, (float) textureProvider.getBrightness());
			gl.glUniform1f(uniSmoothBanding, config.smoothBanding() ? 0f : 1f);
			gl.glUniform1i(uniColorBlindMode, config.colorBlindMode().ordinal());
			gl.glUniform1f(uniTextureLightMode, config.brightTextures() ? 1f : 0f);

			Matrix4 projection = new Matrix4();
			float[] projectionMatrix = null;

			if (config.projectionDebugMode() == ProjectionDebugMode.DISABLED)
			{
				// Calculate projection matrix
				projection.scale(client.getScale(), client.getScale(), 1);
				projection.multMatrix(makePerspectiveProjectionMatrix(viewportWidth, viewportHeight, 50));
				projection.rotate((float) (Math.PI - pitch * Perspective.UNIT), -1, 0, 0);
				projection.rotate((float) (yaw * Perspective.UNIT), 0, 1, 0);
				projection.translate(-client.getCameraX2(), -client.getCameraY2(), -client.getCameraZ2());
				projectionMatrix = projection.getMatrix();
			}
			else if (config.projectionDebugMode() == ProjectionDebugMode.SHADOW)
			{
				projection = lightSpaceProjection;
				projectionMatrix = projection.getMatrix();
			}

			// Bind projection matrices
			if (projectionMatrix != null)
			{
				gl.glUniformMatrix4fv(uniProjectionMatrix, 1, false, projectionMatrix, 0);
			}
			if (lightSpaceProjectionMatrix != null)
			{
				gl.glUniformMatrix4fv(uniLightSpaceProjectionMatrix, 1, false, lightSpaceProjectionMatrix, 0);
			}

			for (int id = 0; id < textures.length; ++id)
			{
				Texture texture = textures[id];
				if (texture == null)
				{
					continue;
				}

				textureProvider.load(id); // trips the texture load flag which lets textures animate

				textureOffsets[id * 2] = texture.getU();
				textureOffsets[id * 2 + 1] = texture.getV();
			}

			// Bind uniforms
			gl.glUniform1i(uniEnableDebug, config.enableDebugMode() ? 1 : 0);
			gl.glUniformBlockBinding(glProgram, uniBlockMain, 0);
			gl.glUniform1i(uniTextures, 1); // texture sampler array is bound to texture1
			gl.glUniform2fv(uniTextureOffsets, 128, textureOffsets, 0);
			gl.glUniform1i(uniTintMode, activeTintMode.getId());
			gl.glUniform1i(uniDistanceFadeMode, config.distanceFadeMode().getId());

			// Bind shadow-related uniforms
			gl.glUniform1i(uniEnableShadows, enableShadows ? 1 : 0);

			if (enableShadows)
			{
				gl.glUniform1i(uniEnableShadowTranslucency, enableShadowTranslucency ? 1 : 0);
				gl.glUniform1i(uniShadowMappingTechnique, config.shadowMappingTechnique().getId());
				gl.glUniform1i(uniShadowMappingKernelSize, config.shadowMappingTechnique().getKernelSize());
				gl.glUniform1f(uniShadowYaw, (float) shadowYaw);
				gl.glUniform1f(uniShadowPitch, (float) shadowPitch);
				gl.glUniform1f(uniShadowDistance, (float) config.maxShadowDistance() / 90.f);

				gl.glUniform1f(uniShadowOpacity, config.shadowOpacity() / 100.f);
				gl.glUniform1f(uniShadowColorIntensity, config.shadowColorIntensity() / 100.f);

				gl.glActiveTexture(GL_TEXTURE2);
				gl.glUniform1i(uniShadowDepthMap, 2);
				gl.glBindTexture(GL_TEXTURE_2D, texDepthMap);

				if (enableShadowTranslucency)
				{
					gl.glActiveTexture(GL_TEXTURE3);
					gl.glUniform1i(uniShadowColorMap, 3);
					gl.glBindTexture(GL_TEXTURE_2D, finalShadowColorMap);

					gl.glActiveTexture(GL_TEXTURE4);
					gl.glUniform1i(uniShadowColorDepthMap, 4);
					gl.glBindTexture(GL_TEXTURE_2D, texShadowColorDepthMap);
				}

				gl.glActiveTexture(GL_TEXTURE0);
			}

			// We just allow the GL to do face culling. Note this requires the priority renderer
			// to have logic to disregard culled faces in the priority depth testing.
			gl.glEnable(GL_CULL_FACE);

			// Enable blending for alpha
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

			// Draw buffers
			gl.glBindVertexArray(vaoHandle);

			gl.glEnableVertexAttribArray(0);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
			gl.glVertexAttribIPointer(0, 4, GL_INT, 0, 0);

			gl.glEnableVertexAttribArray(1);
			gl.glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
			gl.glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);

			gl.glDrawArrays(GL_TRIANGLES, 0, targetBufferOffset);

			gl.glDisable(GL_BLEND);
			gl.glDisable(GL_CULL_FACE);

			gl.glUseProgram(0);
		}

		// Could be used for either anti-aliasing, post-processing, or both
		if (useFbo)
		{
			// If anti-aliasing is enabled, blit color attachment(s) to where they are needed
			if (enableMultisampling)
			{
				// With post-processing enabled, blit to fboPostProcessingHandle, otherwise use the default FBO
				int dstFboHandle = enablePostProcessing ? fboPostProcessingHandle : 0;
				gl.glBindFramebuffer(GL_READ_FRAMEBUFFER, fboSceneHandle);
				gl.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, dstFboHandle);
				gl.glBlitFramebuffer(
					0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
					0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
					GL_COLOR_BUFFER_BIT, GL_NEAREST);
			}

			// Bind default buffer
			gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);

			if (enablePostProcessing)
			{
				int sceneIdx = fboSceneColorAttachmentList.indexOf(GL_COLOR_ATTACHMENT0);

				// Take what's been rendered to texPostProcessingHandles and apply post-processing effects
				if (enableBloom)
				{
					// Source: https://learnopengl.com/Advanced-Lighting/Bloom

					int bloomIdx = fboSceneColorAttachmentList.indexOf(GL_COLOR_ATTACHMENT1);


//					int texResult = applyGaussianBlur(texPostProcessingHandles[bloomIdx]);

//					// Apply two-pass gaussian blur to the bloom texture
//					gl.glUseProgram(glGaussianBlurProgram);
//
//					int direction = 0;
//					int amount = 10;
//					int tex = texPostProcessingHandles[bloomIdx];
//					for (int i = 0; i < amount; i++)
//					{
//						gl.glBindFramebuffer(GL_FRAMEBUFFER, fboPingPong[direction]);
//						gl.glUniform1i(uniGaussianBlurDirection, direction);
//						gl.glBindTexture(GL_TEXTURE_2D, tex);
//						renderQuad();
//						// Swap texture and direction
//						direction = 1 - direction;
//						tex = texPingPong[direction];
//					}
//					gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);

					// Debug the results by blitting to screen
//					gl.glBindFramebuffer(GL_READ_FRAMEBUFFER, fboPingPong[1 - direction]);
//					gl.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
//					gl.glBlitFramebuffer(
//						0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
//						0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
//						GL_COLOR_BUFFER_BIT, GL_NEAREST);

					// Bind the blurred texture for the final post-processing step
//					gl.glActiveTexture(GL_TEXTURE1);
//					gl.glUniform1i(uniPostProcessingBloomTexture, 1);
//					gl.glBindTexture(GL_TEXTURE_2D, texPostProcessingBloom);
				}

				gl.glUseProgram(glPostProcessingProgram);

				gl.glActiveTexture(GL_TEXTURE0);
				gl.glUniform1i(uniPostProcessingColorTexture, 0);
				gl.glBindTexture(GL_TEXTURE_2D, texPostProcessingColor);

				renderQuad(false);

				gl.glUseProgram(0);
			}
		}

		vertexBuffer.clear();
		uvBuffer.clear();
		modelBuffer.clear();
		modelBufferSmall.clear();
		modelBufferUnordered.clear();

		targetBufferOffset = 0;
		smallModels = largeModels = unorderedModels = 0;
		tempOffset = 0;
		tempUvOffset = 0;

		// Texture on UI
		drawUi(overlayColor, canvasHeight, canvasWidth);

		glDrawable.swapBuffers();

		drawManager.processDrawComplete(this::screenshot);
	}

	private float[] makePerspectiveProjectionMatrix(float w, float h, float n)
	{
		return new float[]
			{
				2 / w, 0, 0, 0,
				0, 2 / h, 0, 0,
				0, 0, -1, -1,
				0, 0, -2 * n, 0
			};
	}

	private void drawUi(final int overlayColor, final int canvasHeight, final int canvasWidth)
	{
		final BufferProvider bufferProvider = client.getBufferProvider();
		final int[] pixels = bufferProvider.getPixels();
		final int width = bufferProvider.getWidth();
		final int height = bufferProvider.getHeight();

		gl.glEnable(GL_BLEND);

		vertexBuffer.clear(); // reuse vertex buffer for interface
		vertexBuffer.ensureCapacity(pixels.length);

		IntBuffer interfaceBuffer = vertexBuffer.getBuffer();
		interfaceBuffer.put(pixels);
		vertexBuffer.flip();

		gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		gl.glBindTexture(GL_TEXTURE_2D, interfaceTexture);

		gl.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, interfaceBuffer);

		// Use the texture bound in the first pass
		final UIScalingMode uiScalingMode = config.uiScalingMode();
		gl.glUseProgram(glUiProgram);
		gl.glUniform1i(uniTex, 0);
		gl.glUniform1i(uniTexSamplingMode, uiScalingMode.getMode());
		gl.glUniform2i(uniTexSourceDimensions, canvasWidth, canvasHeight);
		gl.glUniform1i(uniUiColorBlindMode, config.colorBlindMode().ordinal());
		gl.glUniform4f(uniUiAlphaOverlay,
			(overlayColor >> 16 & 0xFF) / 255f,
			(overlayColor >> 8 & 0xFF) / 255f,
			(overlayColor & 0xFF) / 255f,
			(overlayColor >>> 24) / 255f
		);

		if (client.isStretchedEnabled())
		{
			Dimension dim = client.getStretchedDimensions();
			glDpiAwareViewport(0, 0, dim.width, dim.height);
			gl.glUniform2i(uniTexTargetDimensions, dim.width, dim.height);
		}
		else
		{
			glDpiAwareViewport(0, 0, canvasWidth, canvasHeight);
			gl.glUniform2i(uniTexTargetDimensions, canvasWidth, canvasHeight);
		}

		// Set the sampling function used when stretching the UI.
		// This is probably better done with sampler objects instead of texture parameters, but this is easier and likely more portable.
		// See https://www.khronos.org/opengl/wiki/Sampler_Object for details.
		if (client.isStretchedEnabled())
		{
			// GL_NEAREST makes sampling for bicubic/xBR simpler, so it should be used whenever linear isn't
			final int function = uiScalingMode == UIScalingMode.LINEAR ? GL_LINEAR : GL_NEAREST;
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, function);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, function);
		}

		// Texture on UI
//		gl.glBindVertexArray(vaoUiHandle);
//		gl.glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
		renderQuad(true);

		// Reset
		gl.glBindTexture(GL_TEXTURE_2D, 0);
		gl.glBindVertexArray(0);
		gl.glUseProgram(0);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL_BLEND);

		vertexBuffer.clear();
	}

	/**
	 * Convert the front framebuffer to an Image
	 *
	 * @return
	 */
	private Image screenshot()
	{
		int width = client.getCanvasWidth();
		int height = client.getCanvasHeight();

		if (client.isStretchedEnabled())
		{
			Dimension dim = client.getStretchedDimensions();
			width = dim.width;
			height = dim.height;
		}

		if (OSType.getOSType() != OSType.MacOS)
		{
			final Graphics2D graphics = (Graphics2D) canvas.getGraphics();
			final AffineTransform t = graphics.getTransform();
			width = getScaledValue(t.getScaleX(), width);
			height = getScaledValue(t.getScaleY(), height);
			graphics.dispose();
		}

		ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4)
			.order(ByteOrder.nativeOrder());

		gl.glReadBuffer(GL_FRONT);
		gl.glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		for (int y = 0; y < height; ++y)
		{
			for (int x = 0; x < width; ++x)
			{
				int r = buffer.get() & 0xff;
				int g = buffer.get() & 0xff;
				int b = buffer.get() & 0xff;
				buffer.get(); // alpha

				pixels[(height - y - 1) * width + x] = (r << 16) | (g << 8) | b;
			}
		}

		return image;
	}

	@Override
	public void animate(Texture texture, int diff)
	{
		textureManager.animate(texture, diff);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (computeMode == ComputeMode.NONE || gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		invokeOnMainThread(this::uploadScene);
	}

	private void uploadScene()
	{
		vertexBuffer.clear();
		uvBuffer.clear();

		sceneUploader.upload(client.getScene(), vertexBuffer, uvBuffer);

		vertexBuffer.flip();
		uvBuffer.flip();

		IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
		FloatBuffer uvBuffer = this.uvBuffer.getBuffer();

		updateBuffer(sceneVertexBuffer, GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, GL_STATIC_COPY, CL_MEM_READ_ONLY);
		updateBuffer(sceneUvBuffer, GL_ARRAY_BUFFER, uvBuffer.limit() * Float.BYTES, uvBuffer, GL_STATIC_COPY, CL_MEM_READ_ONLY);

		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

		vertexBuffer.clear();
		uvBuffer.clear();
	}

	/**
	 * Check is a model is visible and should be drawn.
	 */
	private boolean isVisible(Model model, int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int _x, int _y, int _z, long hash)
	{
		if (!config.enableIsVisibleCheck())
		{
			return true;
		}

		final int XYZMag = model.getXYZMag();
		final int zoom = client.get3dZoom();
		final int modelHeight = model.getModelHeight();

		int clipMaxX = client.getRasterizer3D_clipMidX2();
		int clipMinX = client.getRasterizer3D_clipNegativeMidX();
		int clipCeilY = client.getRasterizer3D_clipNegativeMidY();
		int clipFloorY = client.getRasterizer3D_clipMidY2();

		// _x, _y and _z are relative to the camera

		// Z = depth, negative going away from the screen
		int yawRotatedZ = yawCos * _z - yawSin * _x >> 16;
		int pitchAndYawRotatedZ = pitchSin * _y + pitchCos * yawRotatedZ >> 16;
		int rotatedMag = pitchCos * XYZMag >> 16;
		// Add visual height to depth, effectively moving it towards the screen
		// Probably to not hide models that have their base too close to the camera for the base
		// to be visible, while they top of the model should still be in view
		int depth = pitchAndYawRotatedZ + rotatedMag;

		if (enableShadows)
		{
			// Move clip bounds by the length of the shadow the model is casting in either direction

			float sunPitch = (float) Math.toRadians(config.sunAngleVertical());
			float sunPitchTan = (float) Math.tan(sunPitch);
			if (Math.abs(sunPitchTan) > 1e-6) // Skip when sun rays are parallel with the ground plane
			{
				float sunYaw = (float) Math.toRadians(config.sunAngleHorizontal());

				int completeHeight = modelHeight + XYZMag;
				int shadowRadius = (int) (completeHeight / sunPitchTan);

				// Turn X and Z lengths into the camera's view coordinates
				float cameraYaw = (float) (client.getCameraYaw() * Perspective.UNIT);
				float relativeSunYaw = sunYaw - cameraYaw;

				// Invert yaw depending on the sun's vertical angle
				if (sunPitch > HALF_PI && sunPitch < TWO_PI - HALF_PI)
				{
					relativeSunYaw += PI;
				}
				// Normalize to between between -pi and pi
				relativeSunYaw -= TWO_PI * Math.floor((relativeSunYaw + Math.PI) / TWO_PI);

				int shadowLengthHorizontal = (int) Math.abs(shadowRadius * Math.sin(relativeSunYaw));
				int shadowLengthNorthSouth = (int) Math.abs(shadowRadius * Math.cos(relativeSunYaw));
				int shadowLengthVertical = shadowLengthNorthSouth * pitchSin >> 16;
				int shadowLengthDepth = shadowLengthNorthSouth * pitchCos >> 16;

				if (depth == 0)
				{
					return true;
				}
				depth += shadowLengthDepth;

//				System.out.println("shadow direction relative to camera: " + (relativeSunYaw > 0 ? "left" : "right") + " " + (Math.abs(relativeSunYaw) < HALF_PI ? "up" : "down"));

				if (relativeSunYaw > 0)
				{
					// shadow pointing left
					clipMaxX += shadowLengthHorizontal;
				}
				else
				{
					// shadow pointing right
					clipMinX -= shadowLengthHorizontal;
				}

				if (Math.abs(relativeSunYaw) < HALF_PI)
				{
					// shadow pointing upwards
					clipFloorY += shadowLengthVertical;
				}
				else
				{
					// shadow pointing downwards
					clipCeilY -= shadowLengthVertical;
				}
			}
		}

		// The depth check apparently also removes objects that should be casting shadows into the scene sometimes
		if (depth <= 50)
		{
			return false;
		}

		// X in rotated world coords, 0 in the middle of the screen, positive to the right
		int projX = _z * yawSin + yawCos * _x >> 16;
		// Calculate minimum X value given the object's magnitude
		int minProjX = (projX - XYZMag) * zoom;
		if (minProjX / depth >= clipMaxX)
		{
			return false;
		}

		int maxProjX = (projX + XYZMag) * zoom;
		if (maxProjX / depth <= clipMinX)
		{
			return false;
		}

		// _y is world height relative to camera height
		// Y in rotated world coords, 0 in the middle with, positive going upwards
		int projY = pitchCos * _y - yawRotatedZ * pitchSin >> 16;
		int yMag = pitchSin * XYZMag >> 16;
		int maxProjY = (projY + yMag) * zoom;
		if (maxProjY / depth <= clipCeilY)
		{
			return false;
		}

		int rotatedHeight = (pitchCos * modelHeight >> 16) + yMag;
		int minProjY = (projY - rotatedHeight) * zoom;
		return minProjY / depth < clipFloorY;
	}

	/**
	 * Draw a renderable in the scene
	 *
	 * @param renderable
	 * @param orientation
	 * @param pitchSin
	 * @param pitchCos
	 * @param yawSin
	 * @param yawCos
	 * @param x
	 * @param y
	 * @param z
	 * @param hash
	 */
	@Override
	public void draw(Renderable renderable, int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash)
	{
		if (computeMode == ComputeMode.NONE)
		{
			Model model = renderable instanceof Model ? (Model) renderable : renderable.getModel();
			if (model != null)
			{
				// Apply height to renderable from the model
				if (model != renderable)
				{
					renderable.setModelHeight(model.getModelHeight());
				}

				model.calculateBoundsCylinder();

				if (!isVisible(model, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash))
				{
					return;
				}

				model.calculateExtreme(orientation);
				client.checkClickbox(model, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash);

				modelX = x + client.getCameraX2();
				modelY = y + client.getCameraY2();
				modelZ = z + client.getCameraZ2();
				modelOrientation = orientation;
				int triangleCount = model.getTrianglesCount();
				vertexBuffer.ensureCapacity(12 * triangleCount);
				uvBuffer.ensureCapacity(12 * triangleCount);

				drawingModel = true;

				renderable.draw(orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash);

				drawingModel = false;
			}
		}
		// Model may be in the scene buffer
		else if (renderable instanceof Model && ((Model) renderable).getSceneId() == sceneUploader.sceneId)
		{
			Model model = (Model) renderable;

			model.calculateBoundsCylinder();

			if (!isVisible(model, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash))
			{
				return;
			}

			model.calculateExtreme(orientation);
			client.checkClickbox(model, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash);

			int tc = Math.min(MAX_TRIANGLE, model.getTrianglesCount());
			int uvOffset = model.getUvBufferOffset();

			GpuIntBuffer b = bufferForTriangles(tc);

			b.ensureCapacity(8);
			IntBuffer buffer = b.getBuffer();
			buffer.put(model.getBufferOffset());
			buffer.put(uvOffset);
			buffer.put(tc);
			buffer.put(targetBufferOffset);
			buffer.put(FLAG_SCENE_BUFFER | (model.getRadius() << 12) | orientation);
			buffer.put(x + client.getCameraX2()).put(y + client.getCameraY2()).put(z + client.getCameraZ2());

			targetBufferOffset += tc * 3;
		}
		else
		{
			// Temporary model (animated or otherwise not a static Model on the scene)
			Model model = renderable instanceof Model ? (Model) renderable : renderable.getModel();
			if (model != null)
			{
				// Apply height to renderable from the model
				if (model != renderable)
				{
					renderable.setModelHeight(model.getModelHeight());
				}

				model.calculateBoundsCylinder();

				if (!isVisible(model, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash))
				{
					return;
				}

				model.calculateExtreme(orientation);
				client.checkClickbox(model, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash);

				boolean hasUv = model.getFaceTextures() != null;

				int faces = Math.min(MAX_TRIANGLE, model.getTrianglesCount());
				vertexBuffer.ensureCapacity(12 * faces);
				uvBuffer.ensureCapacity(12 * faces);
				int len = 0;
				for (int i = 0; i < faces; ++i)
				{
					len += sceneUploader.pushFace(model, i, false, vertexBuffer, uvBuffer, 0, 0, 0, 0);
				}

				GpuIntBuffer b = bufferForTriangles(faces);

				b.ensureCapacity(8);
				IntBuffer buffer = b.getBuffer();
				buffer.put(tempOffset);
				buffer.put(hasUv ? tempUvOffset : -1);
				buffer.put(len / 3);
				buffer.put(targetBufferOffset);
				buffer.put((model.getRadius() << 12) | orientation);
				buffer.put(x + client.getCameraX2()).put(y + client.getCameraY2()).put(z + client.getCameraZ2());

				tempOffset += len;
				if (hasUv)
				{
					tempUvOffset += len;
				}

				targetBufferOffset += len;
			}
		}
	}

	@Override
	public boolean drawFace(Model model, int face)
	{
		if (!drawingModel)
		{
			return false;
		}

		targetBufferOffset += sceneUploader.pushFace(model, face, true, vertexBuffer, uvBuffer, modelX, modelY, modelZ, modelOrientation);
		return true;
	}

	/**
	 * returns the correct buffer based on triangle count and updates model count
	 *
	 * @param triangles
	 * @return
	 */
	private GpuIntBuffer bufferForTriangles(int triangles)
	{
		if (triangles <= SMALL_TRIANGLE_COUNT)
		{
			++smallModels;
			return modelBufferSmall;
		}
		else
		{
			++largeModels;
			return modelBuffer;
		}
	}

	private int getScaledValue(final double scale, final int value)
	{
		return SurfaceScaleUtils.scale(value, (float) scale);
	}

	private void glDpiAwareViewport(final int x, final int y, final int width, final int height)
	{
		if (OSType.getOSType() == OSType.MacOS)
		{
			// JOGL seems to handle DPI scaling for us already
			gl.glViewport(x, y, width, height);
		}
		else
		{
			final Graphics2D graphics = (Graphics2D) canvas.getGraphics();
			final AffineTransform t = graphics.getTransform();
			gl.glViewport(
				getScaledValue(t.getScaleX(), x),
				getScaledValue(t.getScaleY(), y),
				getScaledValue(t.getScaleX(), width),
				getScaledValue(t.getScaleY(), height));
			graphics.dispose();
		}
	}

	private int getDrawDistance()
	{
		final int limit = computeMode != ComputeMode.NONE ? MAX_DISTANCE : DEFAULT_DISTANCE;
		return Ints.constrainToRange(config.drawDistance(), 0, limit);
	}

	private int getShadowDistance()
	{
		return Math.min(getDrawDistance(), config.maxShadowDistance());
	}

	private BlurKernel getShadowBlurKernel()
	{
		int kernelSize = config.shadowMappingTechnique().getKernelSize();
		if (shadowBlurKernel == null || shadowBlurKernel.size != kernelSize)
		{
			shadowBlurKernel = calculateGaussianBlurKernel(kernelSize);
		}
		return shadowBlurKernel;
	}

	private static void invokeOnMainThread(Runnable runnable)
	{
		if (OSType.getOSType() == OSType.MacOS)
		{
			OSXUtil.RunOnMainThread(true, false, runnable);
		}
		else
		{
			runnable.run();
		}
	}

	private void updateBuffer(GLBuffer glBuffer, int target, int size, Buffer data, int usage, long clFlags)
	{
		gl.glBindBuffer(target, glBuffer.glBufferId);
		if (size > glBuffer.size)
		{
			log.trace("Buffer resize: {} {} -> {}", glBuffer, glBuffer.size, size);

			glBuffer.size = size;
			gl.glBufferData(target, size, data, usage);

			if (computeMode == ComputeMode.OPENCL)
			{
				if (glBuffer.cl_mem != null)
				{
					CL.clReleaseMemObject(glBuffer.cl_mem);
				}
				if (size == 0)
				{
					glBuffer.cl_mem = null;
				}
				else
				{
					glBuffer.cl_mem = clCreateFromGLBuffer(openCLManager.context, clFlags, glBuffer.glBufferId, null);
				}
			}
		}
		else if (data != null)
		{
			gl.glBufferSubData(target, 0, size, data);
		}
	}

	private int[][] getSceneBounds()
	{
		Player p = client.getLocalPlayer();
		if (p == null)
		{
			return new int[][]{{0, 0}, {0, 0}};
		}

		LocalPoint l = p.getLocalLocation();

		int drawDistance = getShadowDistance();
		int x = l.getSceneX();
		int y = l.getSceneY();
		int minX = l.getX() - Math.min(drawDistance, x - 1) * Perspective.LOCAL_TILE_SIZE;
		int minY = l.getY() - Math.min(drawDistance, y - 1) * Perspective.LOCAL_TILE_SIZE;
		int maxX = l.getX() + Math.min(drawDistance, Perspective.SCENE_SIZE - 1 - x) * Perspective.LOCAL_TILE_SIZE;
		int maxY = l.getY() + Math.min(drawDistance, Perspective.SCENE_SIZE - 1 - y) * Perspective.LOCAL_TILE_SIZE;

		return new int[][]{{minX, maxX}, {minY, maxY}};
	}

	private void renderQuad(boolean useUiCoords)
	{
		int uvOffset = 12;
		if (useUiCoords)
		{
			uvOffset += 8;
		}

		if (vaoQuad == 0)
		{
			FloatBuffer quadVertices = GpuFloatBuffer.allocateDirect(28);
			quadVertices.put(new float[]
				{
					// Vertex positions
					-1.0f,  1.0f, 0.0f, // top left
					-1.0f, -1.0f, 0.0f, // bottom left
					 1.0f,  1.0f, 0.0f, // top right
					 1.0f, -1.0f, 0.0f, // bottom right

					// UV coordinates in OpenGL screen space
					0.f, 1.f, // top left
					0.f, 0.f, // bottom left
					1.f, 1.f, // top right
					1.f, 0.f, // bottom right

					// UV coordinates for UI
					0.f, 0.f, // top left
					0.f, 1.f, // bottom left
					1.f, 0.f, // top right
					1.f, 1.f  // bottom right
				});
			quadVertices.rewind();
			// setup plane VAO
			vaoQuad = glGenVertexArrays(gl);
			vboQuad = glGenBuffers(gl);
			gl.glBindVertexArray(vaoQuad);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vboQuad);
			gl.glBufferData(GL_ARRAY_BUFFER, quadVertices.capacity() * Float.BYTES, quadVertices, GL_STATIC_DRAW);
			gl.glEnableVertexAttribArray(0);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
		}

		gl.glBindVertexArray(vaoQuad);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboQuad);
		gl.glEnableVertexAttribArray(1);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 2 * Float.BYTES, uvOffset * Float.BYTES);

		gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		gl.glBindVertexArray(0);
	}

	@AllArgsConstructor
	private class BlurKernel
	{
		public float[] halfKernel;
		public int size;
	}

	/**
	 * Applies a 1D blur kernel to the texture vertically and horizontally once per iteration
	 * @param textureHandle An OpenGL texture name
	 * @param kernel A half blur kernel
	 * @param iterations The number of times to apply the blur in each direction
	 * @return
	 */
	private int applyBlur(int textureHandle, BlurKernel kernel, int iterations)
	{
		// Apply two-pass gaussian blur to the bloom texture
		gl.glUseProgram(glGaussianBlurProgram);

		gl.glUniform1fv(uniGaussianBlurKernel, kernel.halfKernel.length, kernel.halfKernel, 0);
		gl.glUniform1i(uniGaussianBlurKernelSize, kernel.size);

		int direction = 0;
		int tex = textureHandle;
		for (int i = 0; i < iterations * 2; i++)
		{
			gl.glBindFramebuffer(GL_FRAMEBUFFER, fboScenePingPong[direction]);
			gl.glUniform1i(uniGaussianBlurDirection, direction);
			gl.glBindTexture(GL_TEXTURE_2D, tex);
			renderQuad(false);
			// Swap texture and direction
			direction = 1 - direction;
			tex = texScenePingPong[direction];
		}

		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glUseProgram(0);

		return tex;
	}

	/**
	 * Generates a 1D gaussian blur kernel excluding, only including values from the midpoint on
	 * @param kernelSize A kernel size of 5 will return 3 values, a kernel size of 2 will return 1
	 * @return
	 */
	private BlurKernel calculateGaussianBlurKernel(int kernelSize)
	{
		float sigma = 1;
		float fMidpoint = kernelSize / 2.f;
		int iMidpoint = (int) fMidpoint;

		float[] kernel = new float[(int) Math.ceil(fMidpoint)];
		kernel[0] = calculateGaussianWeight(fMidpoint, sigma);
		for (int i = 1; i <= iMidpoint; i++)
		{
			float w = calculateGaussianWeight(fMidpoint + i, sigma);
			kernel[i] = w;
		}

		return new BlurKernel(kernel, kernelSize);
	}

	private float calculateGaussianWeight(float x, float sigma)
	{
		float sigma2 = pow(sigma, 2);
		return 1.f / sqrt(TWO_PI * sigma2) * pow(E, -pow(x, 2) / (2 * sigma2));
	}

	@FunctionalInterface
	private interface ShaderInitFunction
	{
		void apply() throws ShaderException;
	}

	private void tryShaderInit(ShaderInitFunction initFunction, Runnable errorCallback)
	{
		try
		{
			initFunction.apply();
		}
		catch (ShaderException ex)
		{
			log.error("ShaderException: ", ex);
			if (errorCallback != null)
			{
				errorCallback.run();
			}
		}
	}
}
