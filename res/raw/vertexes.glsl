attribute vec3 aPosition;
 uniform mat4 projectionMatrix;
 attribute vec2 aTextureCoord;
 varying vec2 vTextureCoord;
 void main() {
     gl_Position = projectionMatrix*vec4(aPosition, 1.0);
     vTextureCoord = aTextureCoord;
 }