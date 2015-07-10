#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif

varying vec2 vTexCoord0;

uniform sampler2D u_texture;

 
void main(void)
{
	gl_FragColor.g = texture2D(u_texture, vTexCoord0).a;
}