package connect3DRender;

import connect3DCore.Piece;

/**
 * A graphics object needs to be able to perform these fundamental operations.
 * This could be extended in the future with:
 *  Draw mesh
 *  Draw model
 *  etc.
 * @author Benjamin
 *
 */
public interface Graphics{

	/**
	 * Draws a cylinder at x,y,z with radius and height.
	 * @param x
	 *  lateral
	 * @param y
	 *  height
	 * @param z
	 *  depth
	 * @param radius
	 *  'thickness' of cylinder
	 * @param height
	 *  tallness of cylinder.
	 */
	void drawCylinderAt(int x, int y, int z, float radius, float height);

	/**
	 * Draws a width * width * height cube at position x,y,z.
	 * (width * base * height).
	 * Uses the active color to select the color of the cube.
	 * @param x
	 *  The lateral component of the position.
	 * @param y
	 *  The vertical component of the position.
	 * @param z
	 *  The depth component of the position.
	 * @param width
	 *  The width and base lengths of the cube.
	 * @param height
	 *  The height of the cube.
	 */
	void drawCubeAt(int x, int y, int z, float width, float height);

	/**
	 * Draw a sphere at x,y,z with a given radius.
	 * @param x
	 *  lateral
	 * @param y
	 *  height
	 * @param z
	 *  depth
	 * @param radius
	 *  the 'size' of the sphere.
	 */
	void drawSphereAt(int x, int y, int z, float radius);

	/**
	 * Change the color that an object will be drawn in.
	 * @param p
	 *  Valid colors are based of the pieces. The empty piece will result in transparency.
	 */
	void setActiveColor(Piece p);

}