package connect3DUtil;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * POJO for PointLight struct in fragment shader.
 * @author Benjamin
 *
 */
public class PointLight {
	private Vector3f color;
	private Vector3f viewPosition;
	private Vector3f worldPosition;
	private float intensity;
	private Attenuation attenuation;
	
	public PointLight(Vector3f color, Vector3f worldPosition, float intensity) {
		attenuation = new Attenuation(1,0,0);
		this.color = color;
		this.worldPosition = worldPosition;
		this.intensity = intensity;
		this.viewPosition = new Vector3f();
	}
	
	/**
	 * Combines the world position information with the view matrix to create the view position
	 * @param viewMatrix
	 *  The view matrix.
	 */
	public void updateViewPosition(Matrix4f viewMatrix) {
		Vector4f temp = new Vector4f(worldPosition.x, 
										worldPosition.y, 
											worldPosition.z, 1.0f);
		temp.mul(viewMatrix);
		viewPosition.x = temp.x;
		viewPosition.y = temp.y;
		viewPosition.z = temp.z;
	}
	
	public Vector3f getColor() {
		return color;
	}



	public void setColor(Vector3f color) {
		this.color = color;
	}



	public Vector3f getWorldPosition() {
		return worldPosition;
	}



	public void setWorldPosition(Vector3f worldPosition) {
		this.worldPosition = worldPosition;
	}



	public Vector3f getViewPosition() {
		return viewPosition;
	}



	public void setViewPosition(Vector3f viewPosition) {
		this.viewPosition = viewPosition;
	}



	public float getIntensity() {
		return intensity;
	}



	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}



	public Attenuation getAttenuation() {
		return attenuation;
	}



	public void setAttenuation(Attenuation attenuation) {
		this.attenuation = attenuation;
	}
	
	public static class Attenuation{
		private float constant;
		private float linear;
		private float exponant;
		public Attenuation(float constant, float linear, float exponant) {
			this.constant = constant;
			this.linear = linear;
			this.exponant = exponant;
		}
		public float getConstant() {
			return constant;
		}
		public void setConstant(float constant) {
			this.constant = constant;
		}
		public float getLinear() {
			return linear;
		}
		public void setLinear(float linear) {
			this.linear = linear;
		}
		public float getExponant() {
			return exponant;
		}
		public void setExponant(float exponant) {
			this.exponant = exponant;
		}
	}
}
