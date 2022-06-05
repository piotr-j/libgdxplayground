varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_texels_per_pixel;

vec2 uv_colececil( vec2 uv, vec2 texture_size ) {
    vec2 pixel = uv * texture_size;

    vec2 locationWithinTexel = fract(pixel);
    // Calculate the inverse of u_texels_per_pixel so we multiply here instead of dividing.
    vec2 interpolationAmount = clamp(locationWithinTexel * u_texels_per_pixel, 0, .5) +
    clamp((locationWithinTexel - 1) * u_texels_per_pixel + .5, 0, .5);

    return (floor(pixel) + interpolationAmount) / texture_size;
}

void main () {
    vec2 texture_size = textureSize(u_texture, 0);

    vec2 pixel = gl_FragCoord.xy;
    vec2 final_uv = uv_colececil( v_texCoords, texture_size );

    vec4 color = texture2D(u_texture, final_uv);

    gl_FragColor = v_color * color;
}
