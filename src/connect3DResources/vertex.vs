#version  330
//VERTEX SHADER
layout (location=0) in vec3 position; //world space position of the vertex. From position 0 in the input buffer, interpret buffer data as a vec3.
layout (location=1) in vec2 textureCoordinate;

out vec2 exTextureCoordinate;

uniform mat4 projectionMatrix;
uniform mat4 worldMatrix;

void main()
{
	gl_Position = projectionMatrix * worldMatrix * vec4(position, 1.0);
	exTextureCoordinate = textureCoordinate;
}