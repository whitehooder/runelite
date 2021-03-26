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

uniform sampler2D texColor;
uniform sampler2D texBloom;

uniform float bloomIntensity;
//uniform float exposure = 1;

in vec2 uv;
out vec4 FragColor;

void main()
{
    vec3 c = texture(texColor, uv).rgb;
    c += texture(texBloom, uv).rgb * bloomIntensity;
    FragColor = vec4(c, 1);
//    FragColor = vec4(texture(texBloom, uv).rgb, 1);
    return;
//    const float gamma = 2.2;
//    vec3 hdrColor = texture(texColor, uv).rgb;
//    vec3 bloomColor = texture(texBloom, uv).rgb;
//    hdrColor += bloomColor; // additive blending
//    // tone mapping
//    vec3 result = vec3(1.0) - exp(-hdrColor * exposure);
//    // also gamma correct while we're at it
//    result = pow(result, vec3(1.0 / gamma));
//    FragColor = vec4(result, 1.0);
}