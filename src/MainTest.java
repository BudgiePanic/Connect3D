import java.util.List;

import connect3DCore.Piece;
import connect3DGame.Game;
import connect3DRender.RenderFactory;

/**
 * Class to test the software renderer.
 * @author Benjamin
 *
 */
public final class MainTest {
	/**
	 * Try to run the game using a swing renderer.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello connect3D!");
		System.out.println("Testing Swing rendering.");
		System.out.println("Starting the game...");
		int board_size = 4;
		//run the game
		new Game(RenderFactory.Renderer("software", board_size), List.of(Piece.RED, Piece.BLUE), board_size).run();
		System.out.println("Game complete. Good Bye.");
	}
}
