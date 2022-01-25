package connect3DUtil;

import org.joml.Vector3f;

import connect3DUtil.MathUtil.Coord3D;

/**
 * Camera class remembers the camera position and rotation values.
 * Works with JOML.
 * The objects storing the values are mutable.
 * @author Benjamin
 *
 */
public class Camera {
	
	/**
	 * The position of the camera in the world.
	 */
	public final Vector3f worldPosition;
	
	/**
	 * The Pitch, Yaw and Roll values of the camera.
	 */
	public final Vector3f cameraRotation;
	
	/**
	 * The location in the world that the camera is pointing at.
	 */
	public final Vector3f cameraPointingAt;
	
	/**
	 * A vector representing the up direction in the world.
	 */
	public final Vector3f worldUp;
	
	/**
	 * How far away the camera is from the origin.
	 */
	public float radius = 2.0f;
	
	/**
	 * The XZ displacement of the camera from 0 degrees in radians.
	 */
	public float theta = 0.0f;
	
	/**
	 * The ZY displacement of the camera from 0 degrees in radians.
	 */
	public float chi = 0.0f;
	
	/**
	 * Create a camera at {0,0,5} with rotation values {0,0,0}.
	 */
	public Camera() {
		this.worldPosition = new Vector3f(0,0,radius);
		this.cameraRotation = new Vector3f(0,0,0);
		this.cameraPointingAt = new Vector3f(0,0,0);
		this.worldUp = new Vector3f(0,1,0);
	}
	
	/**
	 * Set a new position for the camera.
	 * @param newPosition
	 *  The new position of the camera.
	 */
	public void setCameraPosition(Coord3D newPosition) {
		this.worldPosition.x = (float)newPosition.x;
		this.worldPosition.y = (float)newPosition.y;
		this.worldPosition.z = (float)newPosition.z;
	}
	
	/**
	 * Move the camera by some amount.
	 * @param dx
	 *  The amount to move in the X axis.
	 * @param dy
	 *  The amount to move in the Y axis.
	 * @param dz
	 *  The amount to move in the Z axis.
	 */
	protected void moveCamera(float dx, float dy, float dz) {
		
	}

	/**
	 * Updates the camera's position vector based on the radius, theta and chi values.
	 */
	public void updatePosition() {
		this.worldPosition.x = (float)Math.sin(theta) * radius;
		this.worldPosition.z = (float)Math.cos(theta) * radius; //TODO something isn't quite right here...
		this.worldPosition.y = (float)Math.cos(chi) * radius;
	}
	
	
}
