package connect3DRender;

/**
 * Renderer factory is the supplier of renderer objects to the rest of the program.
 * @author Benjamin
 *
 */
public final class RenderFactory {
	
	/**
	 * Create a new type of renderer. Renderer will be uninitialized. 
	 * @param type
	 *  The type of renderer that is desired.
	 * @param boardDimension
	 *  The length, width, height of the board. Kind of naughty
	 *  but I don't know how else to get this information into the renderer.
	 * @return
	 *  The renderer object.
	 * @throws IllegalArgumentException
	 *  Thrown if the renderer type does not exist.
	 */
	public static Renderer Renderer(String type, int boardDimension) throws IllegalArgumentException {
		//currently only supports the text renderer
		if(type.toLowerCase().equals("text")) return new TextRenderer(boardDimension);
		if(type.toLowerCase().equals("software")) return new SwingRenderer(boardDimension);
		if(type.toLowerCase().equals("hardware")) return new HardwareRenderer(boardDimension);
		throw new IllegalArgumentException("Unknown renderer type! ->"+type);
	}
}
