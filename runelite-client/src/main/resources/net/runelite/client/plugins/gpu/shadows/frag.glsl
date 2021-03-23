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
#version 330

uniform sampler2DArray textures;
uniform vec2 textureOffsets[64];
uniform int renderPass;

uniform float brightness;
uniform float smoothBanding;
uniform float textureLightMode;
uniform float minimumOpacity = .1;

in vec4 Color;
noperspective centroid in float fHsl;
flat in int textureId;
in vec2 fUv;
in float fogAmount;

out vec4 FragColor;

#include ../utils/jagex_hsl_to_rgb.glsl

void main() {
    vec4 c = Color;

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

    // Discarding essentially invisible fragments because transparent clickboxes are used occasionally
    // Olm would cast a box shadow if not for this with colored shading disabled
    if (c.a < .01)
        discard;

//    float alphaThreshold = .99; // olm has barely opaque hitboxes
    float alphaThreshold = 1;

    gl_FragDepth = gl_FragCoord.z;
    if (renderPass == 0) {
        if (c.a < alphaThreshold)
            discard;
    } else if (renderPass == 1) {
        if (c.a >= alphaThreshold) {
            discard;
        } else {
            // Makes shadows from things like glass appear more like you would expect
            if (c.a < minimumOpacity)
                c.a = minimumOpacity;
            // Move translucent objects sligthly towards the camera since they often intersect
            FragColor = c * c.a; // Pre-multiply alpha
        }
    }




    //    if (c.a > .9f) {
//        // Set shadow to black, fully opaque
//        FragColor = vec4(0, 0, 0, 1);
//        // Write depth normally
//        gl_FragDepth = gl_FragCoord.z;
//    } else {
//        // Set shadow color without modification
//        FragColor = c;
//        // Write depth as if the translucent object is as far away as possible,
//        // effectively making it have no normal shadow
//        gl_FragDepth = 1;
//    }

    //    if (c.a != 1) {
//        // Store translucent object depth values in the alpha channel
//        // Store color values scaled towards white by the object's alpha value
//        FragColor = vec4(c.rgb * (1.f - c.a), gl_FragCoord.z);
//        //        gl_FragDepth = gl_FragCoord.z - 0.001f;
////        gl_FragDepth = 1;
//    } else {
////        FragColor = c;
//        gl_FragDepth = gl_FragCoord.z;
//    }

//    if (maskTransparent) {
//        if (c.a != 1) // Discard if not fully opaque, shadow pass
//            discard;
//        //gl_FragDepth = gl_FragCoord.z; // I believe this happens by default, but leaving it in anyway
//    } else {
//        if (c.a == 1)
//            discard;
//        float strength = shadowStrength / 100.f;
//        FragColor = vec4(c.rgb, c.a * strength);
//        gl_FragDepth = gl_FragCoord.z; // I believe this happens by default, but leaving it in anyway
//    }

    //    vec4 c;

//    float smoothBanding = 0;

//    vec3 rgb = jagexHslToRgb(int(fHsl)) * smoothBanding + Color.rgb * (1.f - smoothBanding);
//    c = vec4(rgb, Color.a);

//    c.rg = gl_FragCoord.xy;
//    c.rgb = vec3(gl_FragCoord.z);


//    gl_FragColor = c;

//    float a = alpha;
//
//    if (textureId > 0) {
//        int textureIdx = textureId - 1;
//
//        vec2 animatedUv = fUv + textureOffsets[textureIdx];
//
//        vec4 textureColor = texture(textures, vec3(animatedUv, float(textureIdx)));
//        a = textureColor.a;
//    }
//
//    if (maskTransparent) {
//        if (a < 1)
//            discard;
//    } else {
//        if (a >= 1)
//            discard;
//    }

//    gl_FragColor = vec4(vec3(gl_FragCoord.z), 1);
//    gl_FragColor.r = 0.5;
//    gl_FragDepth = fragCoord.z;
//    gl_FragDepth = gl_FragCoord.z;
//    gl_FragColor = vec4(1, 1, 1, 1);
}
