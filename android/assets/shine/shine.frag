#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec2 u_scroll;
uniform vec4 u_bounds;

void main()
{

  vec2 scrollTexCoords = v_texCoords + u_scroll;
  // discard if outside of bounds, or we will see adjecent assets
  if (scrollTexCoords.x < u_bounds.x || scrollTexCoords.x > u_bounds.z ||
    scrollTexCoords.y < u_bounds.y || scrollTexCoords.y > u_bounds.w) {
      discard;
  }

  vec4 tc = texture2D(u_texture, v_texCoords);
  float ma = texture2D(u_texture, scrollTexCoords).a;
  // easier to see for debugging
//  if (tc.a <.2) tc.a = .2;
  tc.a *= ma;
  gl_FragColor = v_color * tc;
}