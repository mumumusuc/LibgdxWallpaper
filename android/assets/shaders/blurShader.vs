uniform mat4 u_projTrans;

attribute vec2 a_position;
attribute vec2 a_textCoord0;
attribute vec4 a_color;

varying vec4 vColor;
varying vec2 vTexCoord;

void main(){
    vColor = a_color;
    vTexCoord = a_texCoord0;
    gl_Position = u_projTrans * vec4(a_position, 0.0, 1.0);
}