#version 330
//VERTEX-SHADER
//Uses orthographic projection matrix to map HUD elements

layout (location=0) in vec3 vertex;
layout (location=1) in vec2 textCoord;
layout (location=2) in vec3 normal;

out vec2 outTextCoord;

uniform mat4 projectionMatrix; // A combination of the model and orthoghraphic projection matrices.

void main()
{
	gl_Position = projectionMatrix * vec4(vertex, 1.0);
	outTextCoord = textCoord;
}