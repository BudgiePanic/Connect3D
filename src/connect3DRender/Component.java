package connect3DRender;

/**
 * A component is an object that can be drawn to the screen by the renderer.
 * The drawing implementation is left to the implementer of this interface.
 * They are provided the renderer and must figure out how to draw themselves using the renderer's capabilities.
 * @author Benjamin
 *
 */
public interface Component {
	
	/**
	 * Redraw this component using the Graphics object.
	 * @param g
	 *  The graphics object provides a range of drawing methods.
	 */
	public void draw(Graphics g);
}
