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
#version 330

uniform sampler2DArray textures;
uniform sampler2D shadowDepthMap;
uniform sampler2D shadowColorMap;
uniform sampler2D shadowColorDepthMap;

uniform vec2 textureOffsets[64];
uniform float brightness;
uniform float smoothBanding;
uniform vec4 fogColor;
uniform int colorBlindMode;
uniform float textureLightMode;
uniform int tintMode;
uniform int distanceFadeMode;

uniform bool enableDebug;
uniform bool enableShadows;
uniform bool enableShadowTranslucency;
uniform int shadowMappingTechnique;
uniform int shadowMappingKernelSize;
uniform bool shadowSeparateOpacityAndColor;

uniform float shadowOpacity;
uniform float shadowColorIntensity;
uniform float shadowPitch;
uniform float shadowYaw;
uniform float shadowDistance;

uniform float bloomThresholdBrightness;
uniform float bloomThresholdSaturation;

in vec4 Color;
noperspective centroid in float fHsl;
flat in int textureId;
in vec2 fUv;
in float fogAmount;
in vec4 fragPosLightSpace;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 BloomColor;

#include utils/jagex_hsl_to_rgb.glsl
#include utils/colorblind.glsl
#include utils/color_funcs.glsl
#include shadows/funcs.glsl

void main() {
    vec4 c;

    if (textureId > 0) {
        int textureIdx = textureId - 1;

        vec2 animatedUv = fUv + textureOffsets[textureIdx];

        vec4 textureColor = texture(textures, vec3(animatedUv, float(textureIdx)));
        vec4 textureColorBrightness = pow(textureColor, vec4(brightness, brightness, brightness, 1.0f));

        // textured triangles hsl is a 7 bit lightness 2-126
        float light = fHsl / 127.f;
        vec3 mul = (1.f - textureLightMode) * vec3(light) + textureLightMode * Color.rgb;
        c = textureColorBrightness * vec4(mul, 1.f);
    } else {
        // pick interpolated hsl or rgb depending on smooth banding setting
        vec3 rgb = jagexHslToRgb(int(fHsl)) * smoothBanding + Color.rgb * (1.f - smoothBanding);
        c = vec4(rgb, Color.a);
    }

    vec3 hsv = rgbToHsv(c.rgb);
//    if (hsv.y * hsv.z > .4) {
    if (hsv.y > bloomThresholdSaturation && hsv.z > bloomThresholdBrightness) {
        BloomColor = vec4(c.rgb, 1);
    } else {
        BloomColor = vec4(vec3(0), 1);
    }

    if (enableShadows) {
        c = applyShadows(c);
    }

    switch (tintMode)
    {
        case 1:// NIGHT
            c.rgb *= vec3(.3f, .3f, .5f);
            break;
        case 2:// SUNRISE
            c.rgb *= vec3(.734f, .262f, .142f);
            break;
        case 3:// SUNSET
            c.rgb *= vec3(.958f, .408f, .294f);
            break;
        case 4:// BRIGHT DAY
            c.rgb *= pow(vec3(1.1f), vec3(3));
            break;
        case 5:// BLOOD_RED
            c.rgb *= vec3(136 / 255.f, 8 / 255.f, 8 / 255.f);
            break;
    }

    vec3 mixedColor = mix(c.rgb, fogColor.rgb, fogAmount);

    if (colorBlindMode > 0) {
        mixedColor.rgb = colorblind(colorBlindMode, c.rgb);
    }

    FragColor = vec4(mixedColor, c.a);
}