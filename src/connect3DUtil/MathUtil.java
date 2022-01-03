package connect3DUtil;
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
		
		@Override
		public String toString() { return x+":"+y+":"+z; }
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
		if(factor >= 0 && factor <= 0.000001) throw new IllegalArgumentException("Cannot divide by zero ->"+factor);
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
		return divide(c, c.magnitude());
	}
	
	/**
	 * Convert a Eular angle in degrees to an angle in Radians.
	 * @param angle
	 * The Eular angle.
	 * @return
	 * The same angle but in Radians.
	 */
	public static double toRadians(double angle) {
		return angle * (Math.PI / 180.0);
	}
	
	/**
	 * Convert an angle in Radians to an angle in degrees.
	 * @param angle
	 * The angle in Radians.
	 * @return
	 * The same angle in degrees.
	 */
	public static double toDegrees(double angle) {
		return angle * (180.0 / Math.PI);
	}
	
	/**
	 * 
	 * @author Benjamin
	 *
	 */
	public static class Matrix4 {
		/**
		 * Internal matrix representation.
		 * Row -> column
		 */
		public final double[][] m;
		
		/**
		 * Create a new Matrix;
		 * @param m
		 * Manually specified matrix values.
		 */
		public Matrix4(final double[][] m) {
			this.m = m;
		}
		
	}
	
	/**
	 * Create a projection matrix.
	 * @param near
	 * @param far
	 * @param fov
	 * @param aspectRatio
	 * @return
	 */
	public static Matrix4 createProjectionM(double near, double far, double fov, double aspectRatio) {
		// 1 / tan (fov / 2)
		double a = aspectRatio;
		double f = 1.0 / Math.tan(toRadians(fov) / 2.0);
		double q = far / (far - near);
		
		double[][] m = new double[4][4];
		for(int x = 0; x < 4; x++) {
			for(int y = 0; y < 4; y++) {
				 m[x][y] = 0.0;
			}
		}
		
		m[0][0] = a * f;
		m[1][1] = f;
		m[2][2] = q;
		m[3][2] = -q * near;
		m[2][3] = 1.0;
		
		return new Matrix4(m);
	}
	
	/**
	 * multiply a coord3D by the matrix m. Implicitly creates a 4D coord with w = 1 to perform the calculation.
	 * @param v
	 * the coord.
	 * @param m
	 * the matrix.
	 * @return
	 * the result of multiplying v by m.
	 */
	public static Coord3D multiply(Coord3D v, Matrix4 m) {
		assert v != null; assert m!= null; assert m.m != null;
		
		double vw = 1.0;
		double x = (v.x * m.m[0][0]) + (v.y * m.m[1][0]) + (v.z * m.m[2][0]) + (vw * m.m[3][0]);
		double y = (v.x * m.m[0][1]) + (v.y * m.m[1][1]) + (v.z * m.m[2][1]) + (vw * m.m[3][1]);
		double z = (v.x * m.m[0][2]) + (v.y * m.m[1][2]) + (v.z * m.m[2][2]) + (vw * m.m[3][2]);
		double w = (v.x * m.m[0][3]) + (v.y * m.m[1][3]) + (v.z * m.m[2][3]) + (vw * m.m[3][3]);
		
		if(w != 0.0) return new Coord3D(x/w, y/w, z/w); //return a normalized vector
		
		return new Coord3D(x,y,z);
	}
	
	/**
	 * Takes a coordinate in normalized device space and converts it to an equivalent point on an (A,B) screen.
	 * The Z depth value is unchanged.
	 * @param c
	 * The coordinate being transformed to pixel space from ND space.
	 * @param screenWidth
	 * The width of the screen.
	 * @param screenHeight
	 * The height of the screen.
	 * @return
	 * The same coordinate but in pixel space.
	 */
	public static Coord3D toScreenSpace(Coord3D c, int screenWidth, int screenHeight) {
		double x = c.x + 1.0;
		x *= 0.5 * screenWidth;
		double y = c.y + 1.0;
		y *= 0.5 * screenHeight;
		double z = c.z;
		return new Coord3D(x,y,z);
	}
}
