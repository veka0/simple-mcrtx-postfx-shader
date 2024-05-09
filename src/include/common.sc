float luminance(vec3 clr) { return dot(clr, vec3(0.2126, 0.7152, 0.0722)); }

// https://en.wikipedia.org/wiki/SRGB#From_CIE_XYZ_to_sRGB
vec3 linearToSRGB(vec3 c) {
    // Full linear to sRGB function
    // return max(mix(12.92 * c, 1.055 * pow(c, 1.0 / 2.4) - 0.055, greaterThan(c, 0.0031308)), 0);
    
    // Approximation
    return max(pow(c, 1.0 / 2.2), 0);
}

// Bloom implementation is based on: https://learnopengl.com/Guest-Articles/2022/Phys.-Based-Bloom
float KarisAverage(vec3 col) {
    // Formula is 1 / (1 + luma)
    // luma = luminance(gamma_correct(hdr))
    float luma = luminance(linearToSRGB(col)) * 0.25;
    return 1.0 / (1.0 + luma);
}
vec3 upscaleBloomFiltered(vec2 texCoord, mediump sampler2D _sampler, vec2 windowRes) {
    // The filter kernel is applied with a radius, specified in pixels
    // of the final game window output.
    const float filterSize = 12.0;
    vec2 filterOffset = filterSize / windowRes;
    
    float x = filterOffset.x;
    float y = filterOffset.y;
    
    // Take 9 samples around current texel:
    // a - b - c
    // d - e - f
    // g - h - i
    // === ('e' is the current texel) ===
    vec3 a = texture2D(_sampler, vec2(texCoord.x - x, texCoord.y + y)).rgb;
    vec3 b = texture2D(_sampler, vec2(texCoord.x, texCoord.y + y)).rgb;
    vec3 c = texture2D(_sampler, vec2(texCoord.x + x, texCoord.y + y)).rgb;
    
    vec3 d = texture2D(_sampler, vec2(texCoord.x - x, texCoord.y)).rgb;
    vec3 e = texture2D(_sampler, vec2(texCoord.x, texCoord.y)).rgb;
    vec3 f = texture2D(_sampler, vec2(texCoord.x + x, texCoord.y)).rgb;
    
    vec3 g = texture2D(_sampler, vec2(texCoord.x - x, texCoord.y - y)).rgb;
    vec3 h = texture2D(_sampler, vec2(texCoord.x, texCoord.y - y)).rgb;
    vec3 i = texture2D(_sampler, vec2(texCoord.x + x, texCoord.y - y)).rgb;
    
    // Apply weighted distribution, by using a 3x3 tent filter:
    //  1   | 1 2 1 |
    // -- * | 2 4 2 |
    // 16   | 1 2 1 |
    vec3 upsample = e*4.0;
    upsample += (b + d+f + h) * 2.0;
    upsample += (a + c+g + i);
    upsample *= 1.0 / 16.0;
    return upsample;
}

// https://github.com/TheRealMJP/BakingLab/blob/master/BakingLab/ACES.hlsl
vec3 RRTAndODTFit(vec3 v) {
    vec3 a = v * (v + 0.0245786) - 0.000090537;
    vec3 b = v * (0.983729 * v + 0.4329510) + 0.238081;
    return a / b;
}
vec3 ACESFittedTonemap(vec3 rgb) {
    const mat3 ACESInputMat = mtxFromCols(
        vec3(0.59719, 0.35458, 0.04823),
        vec3(0.07600, 0.90834, 0.01566),
        vec3(0.02840, 0.13383, 0.83777)
    );
    const mat3 ACESOutputMat = mtxFromCols(
        vec3(1.60475, - 0.53108, - 0.07367),
        vec3(- 0.10208, 1.10813, - 0.00605),
        vec3(- 0.00327, - 0.07276, 1.07602)
    );
    rgb = mul(rgb, ACESInputMat);
    rgb = RRTAndODTFit(rgb);
    rgb = mul(rgb, ACESOutputMat);
    rgb = clamp(rgb, 0.0, 1.0);
    return rgb;
}