package connect3DUtil;

import org.joml.Vector4f;

/**
 * POJO for material struct in fragment shader.
 * @author Benjamin
 *
 */
public final class Material {
	private Vector4f ambient;
	private Vector4f diffuse;
	private Vector4f specular;
	private int hasTexture;
	private float reflectance;
	public Vector4f getAmbient() {
		return ambient;
	}
	public void setAmbient(Vector4f ambient) {
		this.ambient = ambient;
	}
	public Vector4f getDiffuse() {
		return diffuse;
	}
	public void setDiffuse(Vector4f diffuse) {
		this.diffuse = diffuse;
	}
	public Vector4f getSpecular() {
		return specular;
	}
	public void setSpecular(Vector4f specular) {
		this.specular = specular;
	}
	public int getHasTexture() {
		return hasTexture;
	}
	public void setHasTexture(int hasTexture) {
		this.hasTexture = hasTexture;
	}
	public float getReflectance() {
		return reflectance;
	}
	public void setReflectance(float reflectance) {
		this.reflectance = reflectance;
	}
}
