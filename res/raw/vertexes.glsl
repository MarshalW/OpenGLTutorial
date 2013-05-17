attribute vec3 aPosition;
uniform mat4 projectionMatrix;
void main() {
    gl_Position = projectionMatrix*vec4(aPosition, 1.0);
}