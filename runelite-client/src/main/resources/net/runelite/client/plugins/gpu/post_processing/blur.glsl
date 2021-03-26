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
#version 330 core

#define MAX_KERNEL_SIZE 9

uniform sampler2D image;
uniform int direction;
uniform int kernelSize;
uniform float halfKernel[MAX_KERNEL_SIZE];

in vec2 uv;
out vec4 FragColor;

// TODO: implement fast box blur without requiring a kernel

void main() {
    // Calculate the size of a texel in UV coordinates
    vec2 uvOffset = 1.f / textureSize(image, 0);

    // Start with applying the kernel middle (index zero)
    vec3 result = texture(image, uv).rgb * halfKernel[0];

    // Calculate index to end on, rounding up to support both even and odd numbered kernels
    int end = int(ceil(kernelSize / 2.f));
    // If kernel size is even, start on index 0, otherwise start on index 1
    int kernelIdx = kernelSize % 2;
    // Apply the kernel in the negative and positive direction, increasing the offset with each iteration
    if (direction == 0) {
        float offset = uvOffset.x;
        do {
            result += texture(image, vec2(uv.x + offset, uv.y)).rgb * halfKernel[kernelIdx];
            result += texture(image, vec2(uv.x - offset, uv.y)).rgb * halfKernel[kernelIdx];
            offset += uvOffset.x;
        } while (++kernelIdx < end);
    } else {
        float offset = uvOffset.y;
        do {
            result += texture(image, vec2(uv.x, uv.y + offset)).rgb * halfKernel[kernelIdx];
            result += texture(image, vec2(uv.x, uv.y - offset)).rgb * halfKernel[kernelIdx];
            offset += uvOffset.y;
        } while (++kernelIdx < end);
    }

    FragColor = vec4(result, 1.0);
}