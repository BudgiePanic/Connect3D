#version  330
//VERTEX SHADER
layout (location=0) in vec3 position; //world space position of the vertex. From position 0 in the input buffer, interpret buffer data as a vec3.
layout (location=1) in vec2 textureCoordinate;
layout (location=2) in vec3 normal; //a normalized vector pointing perpendicular to the surface

out vec2 exTextureCoordinate;
out vec3 worldViewVertexPosition;
out vec3 worldViewNormal;

uniform mat4 projectionMatrix;
uniform mat4 worldAndViewMatrix;

void main()
{
	
	vec4 modelViewPosition = worldAndViewMatrix * vec4(position, 1.0);
	
	gl_Position = projectionMatrix * worldAndViewMatrix * vec4(position, 1.0);
	exTextureCoordinate = textureCoordinate;
	
	worldViewNormal = normalize(worldAndViewMatrix * vec4(normal, 0.0)).xyz;
	worldViewVertexPosition = modelViewPosition.xyz;
}