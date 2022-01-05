package connect3DUtil;

import connect3DUtil.MathUtil.Coord3D;

public final class Camera {
	
	/**
	 * The position of the camera in virtual space.
	 */
	private Coord3D position;
	
	/**
	 * The direction the camera is pointing.
	 */
	private Coord3D direction;
	
	/**
	 * The up direction in the world.
	 */
	private Coord3D up;
	
	/**
	 * up / down angle.
	 */
	private double pitch;
	/**
	 * left / right angle.
	 */
	private double yaw;
	/**
	 * The field of view.
	 */
	private double FOV;
	
	/**
	 * creates a camera at position, pointing towards -1;
	 */
	public Camera(Coord3D position) {
		assert position != null;
		this.position = position;
		this.direction = new Coord3D(0.0, 0.0, -1.0);
		this.up = new Coord3D(0, 1, 0);
		this.pitch = 0.0;
		this.yaw = -90.0;
		this.FOV = 45.0;
	}
}
