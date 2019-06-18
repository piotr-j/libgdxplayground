// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_pattern;
uniform vec2 u_scale_offset;
uniform vec2 u_pos_offset;
uniform float u_angle;
uniform float u_aspect;
uniform vec2 u_resolution;

vec2 rotateUV(vec2 uv, float rotation, vec2 mid){
  float sf = sin(rotation);
  float cf = cos(rotation);
  return vec2(
    cf * (uv.x - mid.x) + sf * (uv.y - mid.y) + mid.x,
    cf * (uv.y - mid.y) - sf * (uv.x - mid.x) + mid.y
  );
}


void main() {

//  vec4 sc = v_color * texture2D(u_texture, v_texCoords);

  float sin_factor = sin(u_angle);
  float cos_factor = cos(u_angle);

  vec2 ptc = vec2(v_texCoords.x * u_scale_offset.x, v_texCoords.y * u_scale_offset.y);

//  ptc = vec2((ptc.x - u_pos_offset.x) * u_aspect, ptc.y - u_pos_offset.y) * mat2(cos_factor, sin_factor, -sin_factor, cos_factor);
  ptc = rotateUV(ptc, u_angle, vec2(1./u_resolution.x, 1./u_resolution.y));
  ptc.x += u_pos_offset.x;
  ptc.y += u_pos_offset.y;
//  ptc = vec2(ptc.x, ptc.y) * mat2(cos_factor, sin_factor, -sin_factor, cos_factor);
//  ptc += .5;

  vec4 sc = v_color * texture2D(u_texture, v_texCoords);

  vec4 pc = texture2D(u_pattern, ptc);

  gl_FragColor.a = sc.a;
  // we want to show border if we have it
  float b = (sc.r + sc.g + sc.b)/3.;
  b = smoothstep(.0, .2, b);
  //  gl_FragColor.rgb = vec3(b);
  gl_FragColor.rgb = mix(sc.rgb, pc.rgb, pc.a * b);
//  gl_FragColor.rgb = mix(sc.rgb, pc.rgb, pc.a);
//  gl_FragColor.rgb = vec3(b);
}
