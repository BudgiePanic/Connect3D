package connect3DUtil;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * This class handles the world and projection matrices of the Hardware renderer using the joml math library.
 * @author Benjamin
 *
 */
public final class TransformManager {
	/**
	 * Converts coordinates into screen space coordinates.
	 */
	public final Matrix4f projectionMatrix;
	/**
	 * Converts model relative coordinates to world space coordinates.
	 */
	private final Matrix4f worldMatrix;
	/**
	 * Converts world space coordinates into camera space coordinates
	 */
	public final Matrix4f viewMatrix;
	/**
	 * Combination of the world and view matrix.
	 * Converts model coordinates into camera space coordinates.
	 */
	public final Matrix4f worldAndViewMatrix;
	
	/**
	 * Creates a projection matrix and a world transform matrix.
	 */
	public TransformManager() {
		projectionMatrix = new Matrix4f();
		worldMatrix = new Matrix4f();
		viewMatrix = new Matrix4f();
		worldAndViewMatrix = new Matrix4f();
	}
	
	/**
	 * Mutates the projection matrix object, updating it with the provided parameters.
	 * @param fov
	 *  The field of view desired IN RADIANS. 
	 * @param width 
	 *  The width of the screen in pixels.
	 * @param height 
	 *  The height of the screen in pixels.
	 * @param near 
	 *  The Z near clipping distance.
	 * @param far 
	 *  The Z far clipping distance.
	 */
	public void updateProjectionMatrix(float fov, float width, float height, float near, float far) {
		float aspect = width / height;
		projectionMatrix.identity();
		projectionMatrix.perspective(fov, aspect, near, far);
	}
	
	/**
	 * Mutates the world matrix object, updating it with the provided parameters.
	 * @param translation 
	 * @param rotations 
	 * @param scale 
	 */
	public void updateWorldAndViewMatrix(Vector3f translation, Vector3f rotations, float scale) {
		float yaw,pitch,roll;
		pitch = (float)Math.toRadians(rotations.x);
		yaw = (float)Math.toRadians(rotations.y);
		roll = (float)Math.toRadians(rotations.z);
		worldMatrix.identity().translate(translation).
		rotateX(pitch).rotateY(yaw).rotateZ(roll).
		scale(scale);
		
		viewMatrix.mul(worldMatrix, worldAndViewMatrix);
	}
	
	/**
	 * Update the view matrix based on a camera.
	 * @param camera
	 *  The camera being used to update the view matrix.
	 */
	public void updateViewMatrix(Camera camera) {
		viewMatrix.identity().lookAt(camera.worldPosition, camera.cameraPointingAt, camera.worldUp);
	}
}
