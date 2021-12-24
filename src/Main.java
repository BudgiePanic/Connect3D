import connect3DRender.RenderFactory;

/**
 * Start the program.
 * @author Benjamin
 *
 */
public final class Main {
	/**
	 * Use the connect 3D core lib and render lib to start a game of connect3D.
	 * @param args ignored, currently.
	 */
	public static void main(String[] args) {
		System.out.println("Hello connect3D");
		RenderFactory.Renderer("TEXT");
		RenderFactory.Renderer("OPENGL");
		RenderFactory.Renderer("DIRECTX");
		//Game then asks for the player config info via the renderer.
		//Uses this info the initialize a board
		//Then enters the main game loop
		//Players take turns making an input which the game converts into a board input 
		//This continues until someone makes 4 in a row or the board fills up.
		//then a "You won" is displayed
		//followed by "Play again?" OR "Exit"
		//new Game(RenderFactory.Renderer(args[0])).start();
	}
}
