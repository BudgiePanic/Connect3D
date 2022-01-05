package connect3DUtil;

/**
 * Store a two dimensional point.
 * @author Benjamin
 *
 */
public final class Point {
	/**
	 * X equivalent.
	 */
	public final double a;
	/**
	 * Y equivalent.
	 */
	public final double b;
	
	/**
	 * Overload: accept ints.
	 * @param a
	 * @param b
	 */
	public Point(int a, int b) {
		this.a = a;
		this.b = b;
	}
	/**
	 * Create a new point.
	 * @param a
	 * @param b
	 */
	public Point(double a, double b) {
		this.a = a; this.b = b;
	}
}
