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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import static net.runelite.client.plugins.gpu.GpuPlugin.MAX_DISTANCE;
import static net.runelite.client.plugins.gpu.GpuPlugin.MAX_FOG_DEPTH;
import net.runelite.client.plugins.gpu.config.AntiAliasingMode;
import net.runelite.client.plugins.gpu.config.ColorBlindMode;
import net.runelite.client.plugins.gpu.config.FaceCullingMode;
import net.runelite.client.plugins.gpu.config.DistanceFadeMode;
import net.runelite.client.plugins.gpu.config.ProjectionDebugMode;
import net.runelite.client.plugins.gpu.config.ShadowMappingTechnique;
import net.runelite.client.plugins.gpu.config.TextureResolution;
import net.runelite.client.plugins.gpu.config.TintMode;
import net.runelite.client.plugins.gpu.config.UIScalingMode;

@ConfigGroup("gpu")
public interface GpuPluginConfig extends Config
{
	@ConfigSection(
		name = "Shadows",
		description = "Options that configure shadows",
		position = 12
	)
	String shadowSection = "shadowSection";

	@Range(
		max = MAX_DISTANCE,
		slider = true
	)
	@ConfigItem(
		keyName = "drawDistance",
		name = "Draw Distance",
		description = "Draw distance",
		position = 1
	)
	default int drawDistance()
	{
		return 25;
	}

	@ConfigItem(
		keyName = "smoothBanding",
		name = "Remove Color Banding",
		description = "Smooths out the color banding that is present in the CPU renderer",
		position = 2
	)
	default boolean smoothBanding()
	{
		return false;
	}

	@ConfigItem(
		keyName = "antiAliasingMode",
		name = "Anti Aliasing",
		description = "Configures the anti-aliasing mode",
		position = 3
	)
	default AntiAliasingMode antiAliasingMode()
	{
		return AntiAliasingMode.DISABLED;
	}

	@ConfigItem(
		keyName = "uiScalingMode",
		name = "UI scaling mode",
		description = "Sampling function to use for the UI in stretched mode",
		position = 4
	)
	default UIScalingMode uiScalingMode()
	{
		return UIScalingMode.LINEAR;
	}

	@Range(
		max = MAX_FOG_DEPTH
	)
	@ConfigItem(
		keyName = "fogDepth",
		name = "Fog depth",
		description = "Distance from the scene edge the fog starts",
		position = 5
	)
	default int fogDepth()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "useComputeShaders",
		name = "Compute Shaders",
		description = "Offloads face sorting to GPU, enabling extended draw distance. Requires plugin restart.",
		warning = "This feature requires OpenGL 4.3 to use. Please check that your GPU supports this.\nRestart the plugin for changes to take effect.",
		position = 6
	)
	default boolean useComputeShaders()
	{
		return true;
	}

	@Range(
		max = 16
	)
	@ConfigItem(
		keyName = "anisotropicFilteringLevel",
		name = "Anisotropic Filtering",
		description = "Configures the anisotropic filtering level.",
		position = 7
	)
	default int anisotropicFilteringLevel()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "colorBlindMode",
		name = "Colorblindness Correction",
		description = "Adjusts colors to account for colorblindness",
		position = 8
	)
	default ColorBlindMode colorBlindMode()
	{
		return ColorBlindMode.NONE;
	}

	@ConfigItem(
		keyName = "brightTextures",
		name = "Bright Textures",
		description = "Use old texture lighting method which results in brighter game textures",
		position = 9
	)
	default boolean brightTextures()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableShadows",
		name = "Enable shadows",
		description = "Enable directional shadows.",
		position = 10,
		section = shadowSection
	)
	default boolean enableShadows()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableShadowTranslucency",
		name = "Enable translucency",
		description = "Enable proper shadows for translucent objects.",
		position = 11,
		section = shadowSection
	)
	default boolean enableShadowTranslucency()
	{
		return true;
	}

	@ConfigItem(
		keyName = "shadowResolution",
		name = "Shadow resolution",
		description = "Higher = more crisp. If the resolution isn't supported, the max supported resolution will be used instead.",
		position = 12,
		section = shadowSection
	)
	default TextureResolution shadowResolution()
	{
		return TextureResolution.RES_4096x4096;
	}

	@ConfigItem(
		keyName = "shadowMappingTechnique",
		name = "Technique",
		description = "Configure which technique is used for shadow mapping. These can have a large impact on performance and quality. Note even numbers shift the shadow by half a texture coordinate.",
		position = 13,
		section = shadowSection
	)
	default ShadowMappingTechnique shadowMappingTechnique()
	{
		return ShadowMappingTechnique.PERCENTAGE_CLOSER_FILTERING_3X3;
	}

	@Range(
		max = MAX_DISTANCE,
		slider = true
	)
	@ConfigItem(
		keyName = "shadowDistance",
		name = "Shadow distance (WIP)",
		description = "The longer the distance, the lower the shadow quality becomes. The detail will be improved considerably going forward. Baby steps :)",
		position = 14,
		section = shadowSection
	)
	default int maxShadowDistance()
	{
		return 30;
	}

	@ConfigItem(
		keyName = "distanceFadeMode",
		name = "Distance fade mode",
		description = "Configures whether and how the shadow will fade out over distance.",
		position = 15,
		section = shadowSection
	)
	default DistanceFadeMode distanceFadeMode()
	{
		return DistanceFadeMode.ROUNDED;
	}

	@Units(Units.PERCENT)
	@Range(
		max = 100,
		slider = true
	)
	@ConfigItem(
		keyName = "shadowOpacity",
		name = "Shadow opacity",
		description = "Lower = softer shadows, higher = harder shadows.",
		position = 16,
		section = shadowSection
	)
	default int shadowOpacity()
	{
		return 50;
	}

	@Units(Units.PERCENT)
	@Range(
		max = 500,
		slider = true
	)
	@ConfigItem(
		keyName = "shadowColorIntensity",
		name = "Shadow color intensity",
		description = "Only has an effect when colored shading is enabled.",
		position = 17,
		section = shadowSection
	)
	default int shadowColorIntensity()
	{
		return 100;
	}

	@Units(Units.DEGREES)
	@Range(
		max = 360,
		slider = true,
		wrapAround = true
	)
	@ConfigItem(
		keyName = "sunAngleHorizontal",
		name = "Sun angle horizontal",
		description = "Configures the angle of the sun in the horizontal direction. Hold Ctrl to restrict movement and Shift to slow down.",
		position = 19,
		section = shadowSection
	)
	default int sunAngleHorizontal()
	{
		return 320;
	}

	@Units(Units.DEGREES)
	@Range(
		max = 360,
		slider = true,
		wrapAround = true
	)
	@ConfigItem(
		keyName = "sunAngleVertical",
		name = "Sun angle vertical",
		description = "Configures the angle of the sun in the vertical direction. Hold Ctrl to restrict movement and Shift to slow down.",
		position = 20,
		section = shadowSection
	)
	default int sunAngleVertical()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "useTimeBasedAngles",
		name = "Time-synchronized sun (WIP)",
		description = "Synchronizes shadows with global UTC time.",
		position = 21,
		section = shadowSection
	)
	default boolean useTimeBasedAngles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tintMode",
		name = "Color tint mode",
		description = "Configures color tint/time of day.",
		position = 22,
		section = shadowSection
	)
	default TintMode tintMode()
	{
		return TintMode.NORMAL;
	}

	@ConfigItem(
		keyName = "colorPassFaceCulling",
		name = "Color face culling",
		description = "Configures which faces are culled when rendering the translucency maps. No culling would look perhaps look best if it wasn't for the client automatically culling some faces anyway.",
		position = 23,
		section = shadowSection
	)
	default FaceCullingMode colorPassFaceCulling()
	{
		return FaceCullingMode.DISABLED;
	}

	@ConfigItem(
		keyName = "speedUpTime",
		name = "Speed up time",
		description = "One day takes one minute.",
		position = 24,
		section = shadowSection
	)
	default boolean speedUpTime()
	{
		return false;
	}

	@ConfigItem(
		keyName = "latitude",
		name = "Latitude",
		description = "Configure latitude coordinates to use for calculating the sun position.",
		position = 25,
		section = shadowSection
	)
	default String latitude()
	{
		return "0";
	}

	@ConfigItem(
		keyName = "longitude",
		name = "Longitude",
		description = "Configure longitude coordinates to use for calculating the sun position.",
		position = 26,
		section = shadowSection
	)
	default String longitude()
	{
		return "0";
	}

	@ConfigItem(
		keyName = "enableDebugMode",
		name = "Enable debug overlay",
		description = "Displays the different textures used for shadow mapping.",
		position = 28
	)
	default boolean enableDebugMode()
	{
		return false;
	}

	@Units(Units.PERCENT)
	@Range(
		max = 200,
		slider = true
	)
	@ConfigItem(
		keyName = "debugSplitView",
		name = "Debug split view",
		description = "Displays different projections in split view.",
		position = 29,
		section = shadowSection
	)
	default int debugSplitView()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "projectionDebugMode",
		name = "Debug projection",
		description = "Select which projection to debug in split view.",
		position = 30
	)
	default ProjectionDebugMode projectionDebugMode()
	{
		return ProjectionDebugMode.DISABLED;
	}
}