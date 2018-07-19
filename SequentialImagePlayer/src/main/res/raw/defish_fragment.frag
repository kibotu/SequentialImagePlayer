#ifdef GL_ES
precision highp float;
#endif

varying vec2 texcoordVarying;  // position received from vertex shader
uniform sampler2D texture;

uniform float time;
uniform vec2 textureSize;
uniform float rippleOffset;
uniform float rippleCenterUvX;
uniform float rippleCenterUvY;
uniform float alpha;
uniform float strength;
uniform float zoom;

void main(void) {

    // correction radius
    float cr = sqrt(textureSize.x * textureSize.x + textureSize.y * textureSize.y) / strength;

    // half width
    float hW = textureSize.x / 2.0;

    // half height
    float hH = textureSize.y / 2.0;

    // translate uv by half resolution
    vec2 translatedUv = vec2(
        texcoordVarying.x * textureSize.x - hW,
        texcoordVarying.y * textureSize.y -  hH
    );

    // distance
    float dis = sqrt((translatedUv.x) *
                     (translatedUv.x)
		            +(translatedUv.y) *
                     (translatedUv.y));

    // distance ratio to radius
    float r = dis / cr;

    // avoid null pointer
    float theta = r == 0.0
        ? 1.0
        // arc tangens by corrected distance
        : atan(r) / r;

    // get source coordinate by distance correction and apply zooming
    float sX = hW + theta * translatedUv.x * zoom;
    float sY = hH + theta * translatedUv.y * zoom;

    // use new uv coordinates
    vec2 newUv = vec2(sX /textureSize.x , sY / textureSize.y);
    vec4 color = texture2D(texture, newUv);

    gl_FragColor = vec4(color.r, color.g, color.b, alpha);
}