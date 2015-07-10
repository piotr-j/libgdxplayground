#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying float vTexCoord0;
varying vec4 vScaleOffset;

uniform sampler2D u_texture;
uniform float invResolution;



//alpha threshold for our occlusion map
const float THRESHOLD = 0.15;
void main(void) 
{
  float distance = 1.0;
  
  //rectangular to polar filter
  vec2 t = vec2(sin(vTexCoord0), cos(vTexCoord0))*vScaleOffset.xy;
   
  for (float y=0.0; y <= 1.0; y+=invResolution) {		
		//coord which we will sample from occlude map
		vec2 coord = t * y + vScaleOffset.zw;
				
		//if we've hit an opaque fragment (occluder), then get new distance and early out
		if (texture2D(u_texture, coord).g > THRESHOLD)
		{
			distance = y;
			break;
  		}
  } 
  gl_FragColor.r = 1.0 - distance;
}