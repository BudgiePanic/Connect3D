package connect3DMain;
import java.util.List;

import connect3DCore.Piece;
import connect3DGame.Game;
import connect3DRender.RenderFactory;

/**
 * Start the program.
 * @author Benjamin
 *
 */
public final class Main {
	
	/**
	 * The number of players the game will use.
	 */
	int numberPlayers;
	/**
	 * The rendering type that the game will use.
	 */
	String renderType;
	/**
	 * Should the game start.
	 */
	boolean shouldStart;
	
	/**
	 * Use the connect 3D core lib and render lib to start a game of connect3D.
	 * @param args ignored, currently.
	 */
	public static void main(String[] args) {
		
		
		
		//Ask user for game configurement information
		//Pass the config information to the Game constructor.
		//Game uses this information to configure the board
		//Game then starts
		//Players take turns making an input which the game converts into a board input 
		//This continues until someone makes 4 in a row or the board fills up.
		//The game thread then exits upon completion.
		//"This" will then show
		//a "You won" message
		//followed by "Play again?" OR "Exit"
		//new Game(RenderFactory.Renderer(args[0])).start();
		
		int board_size = 4;
		new Game(RenderFactory.Renderer("text", board_size), List.of(Piece.RED, Piece.BLUE), board_size).run();
		
		System.out.println("Game complete. Good Bye.");
		
	}
}
