#ifdef GL_ES
precision highp float;
#endif

varying vec2 texcoordVarying;  // position received from vertex shader
uniform sampler2D texture;

uniform float time;
uniform vec2 resolution;
uniform float rippleOffset;
uniform float rippleCenterUvX;
uniform float rippleCenterUvY;
uniform float alpha;
uniform float strength;
uniform float zoom;

void main(void) {

    // strength
    float s = strength;

    // zoom
    float z = zoom;

    // correction radius
    float cr = sqrt(resolution.x * resolution.x + resolution.y * resolution.y) / s;

    // half width
    float hW = resolution.x / 2.0;

    // half height
    float hH = resolution.y / 2.0;

    // translate uv by half resolution
    vec2 new = vec2(texcoordVarying.x - hW, texcoordVarying.y -  hH);

    // distance
    float dis = sqrt((new.x) *
                     (new.x)
		            +(new.y) *
                     (new.y));

    // distance ratio to radius
    float r = dis / cr;

    // avoid null pointer
    float theta = r == 0.0
        ? 1.0
        // arc tangens by corrected distance
        : atan(r) / r;

    // get source coordinate by distance correction and apply zooming
    float sX = hW + theta * new.x * z;
    float sY = hH + theta * new.y * z;

    // check bounds
    vec2 uv = vec2(clamp(sX, 0.0, resolution.x), clamp(sY, 0.0, resolution.y));

    // use new uv coordinates
    vec4 color = texture2D(texture, uv);

    gl_FragColor = vec4(color.r, color.g, color.b, alpha);
}