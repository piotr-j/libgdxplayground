varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 u_texSize;

vec4 texture2DAA(sampler2D tex, vec2 uv) {
    vec2 uv_texspace = uv*u_texSize;
    vec2 seam = floor(uv_texspace+.5);
    uv_texspace = (uv_texspace-seam)/fwidth(uv_texspace)+seam;
    uv_texspace = clamp(uv_texspace, seam-.5, seam+.5);
    return texture2D(tex, uv_texspace/u_texSize);
}

void main() {

  gl_FragColor = v_color * texture2DAA(u_texture, v_texCoords);
}
