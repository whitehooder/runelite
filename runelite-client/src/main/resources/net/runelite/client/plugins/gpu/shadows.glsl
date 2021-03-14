/*
 * Copyright (c) 2021, Hooder <https://github.com/aHooder>
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
#define PI 3.1415926535897932384626433832795f

float sampleDepthMap(sampler2D tex, vec3 coords) {
    if (!enableShadowMultisampling)
    {
        return coords.z > texture(tex, coords.xy).r ? 1.0 : 0.0;
    }

    int n = 1;

    float shadow = 0;
    vec2 size = textureSize(tex, 0);
    vec2 texelSize = 1.0 / size;
    for (int x = -n; x <= n; ++x)
    {
        for (int y = -n; y <= n; ++y)
        {
            float pcfDepth = texture(tex, coords.xy + vec2(x, y) * texelSize).r;
            if (coords.x < size.x)
            shadow += coords.z > pcfDepth ? 1.0 : 0.0;
        }
    }
    return shadow / 9.0; // only for n = 1
    //    return shadow / pow(n * 2 + 1, 2);
}

vec4 sampleColorMap(sampler2D tex, vec2 coords) {
    if (!enableShadowMultisampling)
    {
        return texture(tex, coords);
    }

    vec4 color = vec4(0);
    vec2 size = textureSize(tex, 0);
    vec2 texelSize = 1.0 / size;
    for (int x = -1; x <= 1; ++x)
    {
        for (int y = -1; y <= 1; ++y)
        {
            vec4 samp = texture(tex, coords + vec2(x, y) * texelSize);
            color += samp;
        }
    }
    return color / 9.0;
}

vec4 applyShadows(vec4 c) {
    if (!enableShadows)
        return c;

    float nightTransitionThreshold = PI / 5.f;

    vec3 coords = fragPosLightSpace.xyz / fragPosLightSpace.w * .5 + .5;
    // Seems like a lot of checks, maybe worth thinking through
    if (coords.z <= 1 && coords.x >= 0 && coords.x <= 1 && coords.y >= 0 && coords.y <= 1) {
        // Apply bias to prevent flat surfaces casting shadows on themselves
        float bias = 0.0001;
        //bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);
        // TODO: geometry shader to generate some okay enough surface normals?
        coords.z -= bias;

        float distanceFadeOpacity = 1;
        if (distanceFadeMode > 0) {
            vec2 fadeCoords = abs(coords.xy) * 2 - 1;
            // a bit of duplicate code for readability
            if (distanceFadeMode == 1) {
                fadeCoords = pow(fadeCoords, vec2(2));
                distanceFadeOpacity = max(0, 1 - sqrt(pow(fadeCoords.x, 2) + pow(fadeCoords.y, 2)));
            } else if (distanceFadeMode == 2) {
                fadeCoords = pow(fadeCoords, vec2(2));
                distanceFadeOpacity = max(0, 1 - max(fadeCoords.x, fadeCoords.y));
            } else if (distanceFadeMode == 3) {
                distanceFadeOpacity = max(0, 1 - sqrt(pow(fadeCoords.x, 2) + pow(fadeCoords.y, 2)));
            }
        }

//        if (distanceFadeOpacity == 0)
//            return c;

        float shadow = sampleDepthMap(shadowDepthMap, coords);
        if (enableShadowMultisampling) {
            // multisampled shadow tends to be darker due to some self shadowing currently, so artificially increase contrast
            shadow = pow(shadow, 4);
        }

        //            if (shadow < 0.9 && enableShadowTranslucency && c.a > 0.01) {
        if (!enableShadowTranslucency || shadow > .95) {
            c.rgb *= 1 - shadow * shadowOpacity * distanceFadeOpacity;
        } else {
            vec2 colorUv = coords.xy; // closes tiny gaps between shadow and color

            if (c.a != 1) {
                // The Z value is only used by sampleDepthMap
                coords.z += translucencyOffset;
            }

            float translucentShadow = sampleDepthMap(shadowColorDepthMap, coords);
            vec4 translucentShadowColor = sampleColorMap(shadowColorMap, colorUv);

            float opacity = translucentShadow * distanceFadeOpacity;
            vec3 shadowColor = translucentShadowColor.rgb;

            vec3 hsl = rgb2hsl(shadowColor);

            hsl.r = mod(hsl.r + .5, 1); // Invert hue due to blend function inverting initially
            hsl.g = hsl.g * shadowColorIntensity; // Multiply saturation unbounded

            if (shadowSeparateOpacityAndColor) {
                // TODO: not implemented
                shadowColor = hsl2rgb(hsl);
                c.rgb *= mix(vec3(1), shadowColor, opacity * shadowOpacity);
            } else {
                // Analogous to real life where no light can be added since what you see is reflected light
                shadowColor = hsl2rgb(hsl);
                c.rgb *= mix(vec3(1), shadowColor, opacity * shadowOpacity);
            }
        }
    }

    if (enableDebug) {
        float tileSize = 600;
        float offsetLeft = 0;
        float offsetBottom = 0;

        float overlayAlpha = 1;
        vec2 preOffset = vec2(0.00, 0.00);
        vec2 postOffset = vec2(0.00, 0.00);
        float zoom = 1; // applied after offset

        //            // Window test
        //            vec2 preOffset = vec2(0.7, 0.00);
        //            vec2 postOffset = vec2(0, 0.1);
        //            float zoom = 2; // applied after offset

        vec2 uv = gl_FragCoord.xy - vec2(offsetLeft, offsetBottom);
        int tileX = int(floor(uv.x / tileSize));
        int tileY = int(floor(uv.y / tileSize));
        vec2 uvTileOffset = vec2(tileX, tileY);

        // scale uv to 0-1 and apply transformations
        uv = uv / vec2(tileSize) - uvTileOffset;
        uv += preOffset;
        uv -= .5; // Move 0 to center
        uv *= vec2(1 / zoom);
        uv += .5; // Move 0 back
        uv += postOffset;

        if (tileX == 0) {
            if (tileY == 0 && false) {
                return vec4(vec3(texture(shadowDepthMap, uv).r), overlayAlpha);
            } else if (tileY == 1 && false) {
                float translucentDepth = texture(shadowColorDepthMap, uv).r;
                return FragColor = vec4(vec3(translucentDepth), overlayAlpha);
            } else if (tileY == 0) {
                vec4 color = texture(shadowColorMap, uv);
                return FragColor = vec4(color.rgb, overlayAlpha);
            } else if (tileY == 1) {
                vec4 color = texture(shadowColorMap, uv);
                return FragColor = vec4(vec3(color.a), overlayAlpha);
            }
        }
    }

    return c;
}