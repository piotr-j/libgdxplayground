// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
//  vec4 c = v_color * texture2D(u_texture, v_texCoords);
  vec4 c = vec4(.5, .5, .5, texture2D(u_texture, v_texCoords).a);
  if (c.a == 0.0) {
    discard;
  }
  gl_FragColor = c;
}
