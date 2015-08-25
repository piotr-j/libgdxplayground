varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_noise;
uniform float u_time;

void main() {
//  float2 displacedTexCoord = i.texcoord + float2(tex2D(_NoiseTex, i.texcoord).xy)/20;
// float2 displacedTexCoord = i.texcoord + float2(tex2D(_NoiseTex, i.texcoord + float2(_SinTime.w, _CosTime.w)/10).xy)/25;
//  vec4 noise = texture2D(u_noise, v_texCoords + vec2(sin(u_time * 50.)/500., cos(u_time * 50.)/500.));
//  vec2 displacedTexCoord = v_texCoords + vec2(sin(u_time * 50.)/500., cos(u_time * 50.)/500.);
/*
float2 displacedTexCoord = i.texcoord + float2(
    tex2D(_NoiseTex, i.vertex.xy/300 + float2((_Time.w%50)/50, 0)).z - .5,
    tex2D(_NoiseTex, i.vertex.xy/300 + float2(0, (_Time.w%50)/50)).z - .5
)/20;
*/
//  vec2 displacedTexCoord = v_texCoords + vec2(
//    texture2D(u_noise, gl_FragCoord.xy / 3000. + vec2(mod(u_time, 50.) / 50., 0)).z - .5,
//    texture2D(u_noise, gl_FragCoord.xy / 3000. + vec2(0, mod(u_time, 50.) / 50.)).z - .5
//  );
  vec2 frag = vec2(gl_FragCoord.x/1280., gl_FragCoord.y/720.);
  vec2 displacedTexCoord = v_texCoords + vec2(
//  vec2 displacedTexCoord = v_texCoords + vec2(
    texture2D(u_noise, frag + vec2(sin(u_time * 25.)/20., 0)).z - .5,
    texture2D(u_noise, frag + vec2(0, cos(u_time * 25.)/20.)).z - .5
  )/250.;
  gl_FragColor = v_color * texture2D(u_texture, displacedTexCoord);
//  gl_FragColor = v_color * texture2D(u_noise, frag);
//  gl_FragColor = vec4(gl_FragCoord.x/1280., gl_FragCoord.y/720., 0., 1.);
}
