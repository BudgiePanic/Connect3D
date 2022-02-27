#version 330
//VERTEX SHADER

layout (location=0) in vec3 vertex;
layout (location=1) in vec2 textureCoord;
layout (location=2) in vec3 normal;

out vec2 outTextureCoord;

uniform mat4 worldViewMatrix;
uniform mat4 projectionMatrix;

void main()
{
	gl_Position = projectionMatrix * worldViewMatrix * vec4(vertex, 1.0);
	outTextureCoord = textureCoord;
}