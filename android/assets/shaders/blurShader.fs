varying vec4 vColor;
varying vec2 vTexCoord;

uniform sampler2D u_texture;
uniform float res;
uniform float radius;
uniform vec2 dir;

void main(){

    vec4 sum(0.0);

    for(int i = -radius;i<radius;i++){
        for(int j = -radius;j<radius;j++){
            sum += texture2D(u_texture, vec2(vTexCoord.x+i/res, vTexCoord.y+j/res));
        }
    }

    gl_FragColor = vColor * vec4(sum.rgb, 1.0);
}