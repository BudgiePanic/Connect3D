package connect3DUtil;

import java.math.*;
/**
 * Utility math functions for the Swing renderer.
 * @author Benjamin
 *
 */
public class MathUtil {
	/**
	 * Location in 3D space
	 * @author Benjamin
	 *
	 */
	public static class Coord3D {
		public final double x,y,z;
		public Coord3D(float x, float y, float z) {
			this.x = x; this.y = y; this.z = z;
		}
		public Coord3D(int x, int y, int z) {
			this.x = (double)x; this.y = (double)y; this.z = (double)z;
		}
		public Coord3D(double x, double y, double z) {
			this.x = x; this.y = y; this.z = z;
		}
		/**
		 * Returns the magnitude of this Coordinate.
		 * @return
		 * The magnitude of this coordinate.
		 */
		public double magnitude() {
			return Math.sqrt((x*x) + (y*y) + (z*z));
		}
	}
	
	/**
	 * Add two coordinates together.
	 * @param one
	 * The first coordinate to be summed
	 * @param two
	 * The second coordinate to be summed
	 * @return
	 * Produces a new Coord that is the result of adding the two coords together.
	 */
	public static Coord3D add(Coord3D one, Coord3D two) {
		return new Coord3D((one.x+two.x),(one.y+two.y),(one.z + two.z));
	}
	
	/**
	 * Subtract two coordinates.
	 * @param one
	 * The first coordinate.
	 * @param two
	 * The second coordinate.
	 * @return
	 * (one - two).
	 */
	public static Coord3D subtract(Coord3D one, Coord3D two) {
		return new Coord3D((one.x-two.x),(one.y-two.y),(one.z-two.z));
	}
	
	/**
	 * Make a new coord that is the result of one / two.
	 * @param c 
	 * the coordinate being divided
	 * @param factor 
	 * the scale factor
	 * @return
	 * A new coordinate that is divided by the factor.
	 */
	public static Coord3D divide(Coord3D c, double factor) {
		return new Coord3D(c.x / factor, 
							c.y / factor, 
							 c.z / factor);
	}
	
	/**
	 * Make a new coord that is the result of one / two.
	 * @param c 
	 * the coordinate being divided
	 * @param factor 
	 * the scale factor
	 * @return
	 * A new coordinate that is divided by the factor.
	 */
	public static Coord3D scale(Coord3D c, double factor) {
		return new Coord3D(c.x * factor, 
							c.y * factor, 
							 c.z * factor);
	}
	
	/**
	 * Calculate the dot product of two coords. 
	 * a . b = mag(a) * mab(b) * cos(angle)
	 * @param a
	 * the first coord
	 * @param b
	 * the second coord
	 * @return
	 * the dot product
	 */
	public static double dot(Coord3D a, Coord3D b) {
		return (a.x * b.x) + (a.y * b.y) + (a.z * b.z);
	}
	
	/**
	 * Get the cross product of a and b. 
	 * a and b must have (0,0,0) as their origin for this method to work correctly.
	 * a x b = mag(a) * mag(b) * sin(angle) * 
	 * @param a
	 * the first coordinate
	 * @param b
	 * the second coordinate
	 * @return
	 * the cross product of a and b (a x b)
	 */
	public static Coord3D cross(Coord3D a, Coord3D b) {
		return new Coord3D(
				(a.y * b.z) - (a.z * b.y),
				(a.z * b.x) - (a.x * b.z),
				(a.x * b.y) - (a.y * b.x)
				);
	}
	
	/**
	 * Change a coordinate's magnitude to one.
	 * c.norm = c / c.magnitude
	 * @param c
	 * The coordinate being normalized
	 * @return
	 * A new Coordinate that is the same as the input, but normalized.
	 */
	public static Coord3D normalize(Coord3D c) {
		return new Coord3D(1,2,3);
	}
}
