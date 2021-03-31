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
import net.runelite.client.plugins.gpu.config.DaylightCycle;
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
		name = "General",
		description = "General GPU settings.",
		position = 0
	)
	String generalSection = "generalSection";

	@ConfigSection(
		name = "Shadows",
		description = "Options that configure shadows",
		position = 1
	)
	String shadowSection = "shadowSection";

	@ConfigSection(
		name = "Effects",
		description = "Options that configure various effects",
		position = 2
	)
	String effectsSection = "effectsSection";

	@ConfigSection(
		name = "Debug",
		description = "Anything debug-related goes here",
		position = 3
	)
	String debugSection = "debugSection";

	@Range(
		max = MAX_DISTANCE,
		slider = true
	)
	@ConfigItem(
		keyName = "drawDistance",
		name = "Draw Distance",
		description = "Draw distance",
		section = generalSection,
		position = 0
	)
	default int drawDistance()
	{
		return 25;
	}

	@ConfigItem(
		keyName = "smoothBanding",
		name = "Remove Color Banding",
		description = "Smooths out the color banding that is present in the CPU renderer",
		section = generalSection,
		position = 1
	)
	default boolean smoothBanding()
	{
		return false;
	}

	@ConfigItem(
		keyName = "antiAliasingMode",
		name = "Anti Aliasing",
		description = "Configures the anti-aliasing mode",
		section = generalSection,
		position = 2
	)
	default AntiAliasingMode antiAliasingMode()
	{
		return AntiAliasingMode.DISABLED;
	}

	@ConfigItem(
		keyName = "uiScalingMode",
		name = "UI scaling mode",
		description = "Sampling function to use for the UI in stretched mode",
		section = generalSection,
		position = 3
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
		section = generalSection,
		position = 4
	)
	default int fogDepth()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "useComputeShaders",
		name = "Compute Shaders",
		description = "Offloads face sorting to GPU, enabling extended draw distance.",
		section = generalSection,
		position = 5
	)
	default boolean enableComputeShaders()
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
		section = generalSection,
		position = 6
	)
	default int anisotropicFilteringLevel()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "colorBlindMode",
		name = "Colorblindness Correction",
		description = "Adjusts colors to account for colorblindness",
		section = generalSection,
		position = 7
	)
	default ColorBlindMode colorBlindMode()
	{
		return ColorBlindMode.NONE;
	}

	@ConfigItem(
		keyName = "brightTextures",
		name = "Bright Textures",
		description = "Use old texture lighting method which results in brighter game textures",
		section = generalSection,
		position = 8
	)
	default boolean brightTextures()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableShadows",
		name = "Enable shadows",
		description = "Enable drawing shadows to the scene.",
		section = shadowSection,
		position = -9
	)
	default boolean enableShadows()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableShadowTranslucency",
		name = "Enable translucency",
		description = "Enable proper shadows for translucent objects.",
		section = shadowSection,
		position = -8
	)
	default boolean enableShadowTranslucency()
	{
		return true;
	}

	@ConfigItem(
		keyName = "shadowResolution",
		name = "Shadow resolution",
		description = "Higher = more crisp. If the resolution isn't supported, the max supported resolution will be used instead.",
		section = shadowSection,
		position = -7
	)
	default TextureResolution shadowResolution()
	{
		return TextureResolution.RES_4096x4096;
	}

	@ConfigItem(
		keyName = "shadowMappingTechnique",
		name = "Technique",
		description = "Configure which technique is used for shadow mapping. These can have a large impact on performance and quality. Note even numbers shift the shadow by half a texture coordinate.",
		section = shadowSection,
		position = -6
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
		name = "Shadow distance",
		description = "The longer the distance, the lower the shadow quality becomes. The detail will be improved considerably going forward. Baby steps :)",
		section = shadowSection,
		position = -5
	)
	default int maxShadowDistance()
	{
		return 40;
	}

	@ConfigItem(
		keyName = "distanceFadeMode",
		name = "Distance fade mode",
		description = "Configures whether and how the shadow will fade out over distance.",
		section = shadowSection,
		position = -4
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
		section = shadowSection,
		position = -3
	)
	default int shadowOpacity()
	{
		return 50;
	}

	@Units(Units.PERCENT)
	@Range(
		max = 200,
		slider = true
	)
	@ConfigItem(
		keyName = "shadowColorIntensity",
		name = "Shadow color intensity",
		description = "Only has an effect when colored shading is enabled.",
		section = shadowSection,
		position = -2
	)
	default int shadowColorIntensity()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "daylightCycle",
		name = "Daylight cycle",
		description = "Configures what determines the sun's position.",
		section = shadowSection
	)
	default DaylightCycle daylightCycle()
	{
		return DaylightCycle.EARTH_BASED;
	}

	@Units(Units.DEGREES)
	@Range(
		max = 360,
		slider = true,
		wrapAround = true
	)
	@ConfigItem(
		keyName = "sunAngleHorizontal",
		name = "Sun angle horizontal (custom cycle)",
		description = "Configures the angle of the sun in the horizontal direction. Hold Ctrl to restrict movement and Shift to slow down.",
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
		name = "Sun angle vertical (custom cycle)",
		description = "Configures the angle of the sun in the vertical direction. Hold Ctrl to restrict movement and Shift to slow down.",
		section = shadowSection
	)
	default int sunAngleVertical()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "tintMode",
		name = "Color tint mode",
		description = "Configures color tint/time of day.",
		section = shadowSection
	)
	default TintMode tintMode()
	{
		return TintMode.NORMAL;
	}

	@ConfigItem(
		keyName = "latitude",
		name = "Latitude",
		description = "Configure latitude coordinates to use for calculating the sun position.",
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
		section = shadowSection
	)
	default String longitude()
	{
		return "0";
	}

	@ConfigItem(
		keyName = "enableBloom",
		name = "Enable bloom",
		description = "Adds bloom to bright objects.",
		section = effectsSection,
		position = -2
	)
	default boolean enableBloom()
	{
		return false;
	}

	// Arbitrary bloom settings and way too many options, for testing purposes

	@Range(
		max = 10,
		slider = true
	)
	@ConfigItem(
		keyName = "bloomBlurIterations",
		name = "Bloom blur iterations",
		description = "Configures the number of iterations for the blur step of the bloom effect.",
		section = effectsSection
	)
	default int bloomBlurIterations()
	{
		return 1;
	}

	@Range(
		min = 1,
		max = 20,
		slider = true
	)
	@ConfigItem(
		keyName = "bloomBlurSize",
		name = "Bloom blur size",
		description = "Configures the kernel size of the blur effect.",
		section = effectsSection
	)
	default int bloomBlurSize()
	{
		return 10;
	}

	@Units(Units.PERCENT)
	@Range(
		max = 500,
		slider = true
	)
	@ConfigItem(
		keyName = "bloomIntensity",
		name = "Bloom intensity",
		description = "Configures the amount of light added by the bloom effect.",
		section = effectsSection
	)
	default int bloomIntensity()
	{
		return 100;
	}

	@Units(Units.PERCENT)
	@Range(
		max = 100,
		slider = true
	)
	@ConfigItem(
		keyName = "bloomThresholdBrightness",
		name = "Bloom threshold brightness",
		description = "Controls which objects have bloom.",
		section = effectsSection
	)
	default int bloomThresholdBrightness()
	{
		return 90;
	}

	@Units(Units.PERCENT)
	@Range(
		max = 100,
		slider = true
	)
	@ConfigItem(
		keyName = "bloomThresholdSaturation",
		name = "Bloom threshold saturation",
		description = "Controls which objects have bloom.",
		section = effectsSection
	)
	default int bloomThresholdSaturation()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "colorPassFaceCulling",
		name = "Shadow color face culling",
		description = "Configures which faces are culled when rendering the translucency maps. No culling would perhaps look best if it wasn't for the client automatically culling some faces anyway.",
		section = debugSection
	)
	default FaceCullingMode colorPassFaceCulling()
	{
		return FaceCullingMode.DISABLE;
	}

	@ConfigItem(
		keyName = "speedUpTime",
		name = "Speed up time",
		description = "One day takes one minute.",
		section = debugSection
	)
	default boolean speedUpTime()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableDebugMode",
		name = "Debug shadow maps",
		description = "Displays the different textures used for shadow mapping.",
		section = debugSection
	)
	default boolean enableDebugMode()
	{
		return false;
	}

	@ConfigItem(
		keyName = "projectionDebugMode",
		name = "Projection debug mode",
		description = "Select debug projection to use for the viewport.",
		section = debugSection
	)
	default ProjectionDebugMode projectionDebugMode()
	{
		return ProjectionDebugMode.DISABLED;
	}

	@ConfigItem(
		keyName = "isVisibleCheck",
		name = "Enable isVisible check",
		description = "For debugging",
		section = debugSection
	)
	default boolean enableIsVisibleCheck()
	{
		return true;
	}
}