// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_color_grade;
uniform float u_lut_size;

// This function allows 3D cube-shaped texture that is arranged in a 2D texture as a row to be sampled as if it were a
// 3D texture. OpenGL ES 2.0 and WebGL don't support 3D textures. It's easier to handle the look-up textures as single
// files anyway. This function would be more complicated if we weren't using equal dimensions for R, G, and B in the
// look-up table.
// This is based on the function explained in this video, but I fixed a bug that made its results slightly off:
// https://youtu.be/rfQ8rKGTVlg?t=26m42s
// The example in the video forgot to avoid sampling the first and last half-values for green and blue, although he
// explained how to do it correctly for red. Without these corrections, all colors would be slightly off when the
// color grading is disabled by using the source color grade table.
// NOTE this is still slightly broken
vec4 sampleAs3DTexture(sampler2D tex, vec3 texCoord, float size) {
    // x, y dimension of a slice
    float sliceSize = 1.0 / size; // 1.0/16.0 = 0.0625
    float slicePixelSize = sliceSize / size; // 0.0625/16.0 = 0.00390625
    // x, y dimension of a slice with .5 pixel margin
    float sliceInnerSize = slicePixelSize * (size - 1.0); // 0.05859375

    float zSlice0 = min(floor(texCoord.z * (size - 1.0)), size - 1.0);
    float zSlice1 = min(zSlice0 + 1.0, size - 1.0);
    float xOffset = slicePixelSize * 0.5 + texCoord.x * sliceInnerSize;

    float s0 = xOffset + zSlice0 * sliceSize;
    float s1 = xOffset + zSlice1 * sliceSize;

    float y = sliceSize * 0.5 + texCoord.y * sliceSize * (size - 1.0);

    vec4 slice0Color = texture2D(tex, vec2(s0, y));
    vec4 slice1Color = texture2D(tex, vec2(s1, y));

    float zOffset = mod(texCoord.z * (size - 1.0), 1.0);

    return mix(slice0Color, slice1Color, zOffset);
}

void main()
{
	vec3 inputColor = texture2D(u_texture, v_texCoords).rgb;
	gl_FragColor = sampleAs3DTexture(u_color_grade, inputColor, u_lut_size);
}
