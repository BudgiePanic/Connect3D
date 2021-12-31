import java.util.List;

import connect3DCore.Piece;
import connect3DGame.Game;
import connect3DRender.RenderFactory;

public final class MainTest {
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
