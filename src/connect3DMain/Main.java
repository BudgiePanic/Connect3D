package connect3DMain;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import connect3DCore.Piece;
import connect3DGame.Game;
import connect3DRender.RenderFactory;
import connect3DUtil.ConfigDialog;

/**
 * Start the program.
 * @author Benjamin
 *
 */
public final class Main {
	
	/**
	 * The number of players the game will use.
	 */
	public int numberPlayers;
	/**
	 * The size of the board.
	 */
	public int boardSize;
	/**
	 * The rendering type that the game will use.
	 */
	public String renderType;
	/**
	 * Should the game start.
	 */
	public boolean shouldStart;
	
	/**
	 * Use the connect 3D core lib and render lib to start a game of connect3D.
	 * Gets player info from a ConfigDialog.
	 * @param args ignored, currently.
	 */
	public static void main(String[] args) {
		
		Main main = new Main();
		
		try {
			SwingUtilities.invokeAndWait(()->{
				new ConfigDialog(main);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		if(main.shouldStart) {
			new Game(RenderFactory.Renderer(main.renderType, main.boardSize), toPieceList(main.numberPlayers), main.boardSize).run();
		} 
		System.out.println("Goodbye.");
	}
	
	/**
	 * Convert the number of players into a list of unique Pieces.
	 * @param numbPlayers
	 *  The number of players.
	 * @return
	 *  A list of pieces to represent the players.
	 */
	private static List<Piece> toPieceList(int numbPlayers){
		if(numbPlayers < 2 || numbPlayers > 4) throw new IllegalArgumentException("Invalid number of players:"+numbPlayers);
		List<Piece> answer = new ArrayList<>();
		for(int i = 0; i != numbPlayers; i++) {
			answer.add(Piece.values()[i]);
		}
		return answer;
	}
}

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