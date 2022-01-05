package connect3DGame;

import java.util.Collections;
import java.util.List;

import connect3DCore.Board;
import connect3DCore.BoardFactory;
import connect3DCore.Piece;
import connect3DRender.InitializationException;
import connect3DRender.Observer;
import connect3DRender.Renderer;
import connect3DUtil.Coord;

/**
 * Main game thread. 
 * Registers as an observer of renderer events.
 * Continuously polls the renderer to check for events and then redraw.
 * Maintains the state of game.
 * @author Benjamin
 *
 */
public final class Game implements Runnable, Observer {

	private Renderer renderer;
	private List<Piece> players;
	private Board board;
	private final int BOARD_SIZE;
	private int currentPlayer;
	private Coord currentSelect;
	
	/**
	 * Games need a renderer to perform IO through.
	 * @param r
	 *  The renderer that the game will use to perform IO
	 * @param players 
	 *  This list of piece data will be used as the game configuration.
	 *  The game will cycle turns around these pieces until someone wins or the board fills
	 *  capacity.
	 *  Should contain 2 of more piece types and NOT contain EMPTY.
	 * @param board_size
	 *  The size of the board that this game will use.
	 * @throws IllegalArgumentException 
	 *  Thrown if the players list is empty, does not contain two or more pieces, or contains empty
	 *  which is an invalid player type.
	 */
	public Game(Renderer r, List<Piece> players, int board_size) throws IllegalArgumentException {
		if(r == null || players == null) throw new IllegalArgumentException("Params cannot be null!");
		if(players.isEmpty() || players.contains(Piece.EMPTY)) throw new IllegalArgumentException("Must provide valid players");
		this.BOARD_SIZE = board_size;
		this.players = Collections.unmodifiableList(players);
		this.currentPlayer = 0;
		this.renderer = r;
		renderer.addObserver(this);
		try {
			renderer.initialize();
		} catch (InitializationException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		board = BoardFactory.board(BOARD_SIZE);
		//These components will be redrawn each frame.
		renderer.addComponent(board);
		renderer.addComponent((g) -> {
			//Anonymous component to display the current player.
			g.drawMessage(players.get(currentPlayer).name()+"'s Turn");
		});
		renderer.addComponent((g) -> {
			if(currentSelect == null) return;
			int x = (int)this.currentSelect.x;
			int z = (int)this.currentSelect.z;
			if(board.isXZvalid(x, z)) {
				int y = board.getNextFree(x, z);
				if(y >= 0) {
					g.setActiveColor(Piece.WHITE);
					g.drawSphereAt(x, y, z, 1);
				}
			}
		});
		
	}
	
	@Override
	public void run() {
		while((!board.hasSomeoneWon()) && (!board.isBoardFull())) {
			renderer.pollEvents();
			renderer.redraw();
		}
		
		if(board.isBoardFull()) {
			System.out.println("No one wins...");
		} else {
			System.out.println(board.getWinner() + " WINS!!!");
		}
		renderer.destroy();
	}

	@Override
	public void update(int x, int y, int z, String type) {
		switch(type.toLowerCase()) {
			case "exit":
				System.out.println("Good bye.");
				renderer.destroy();
				System.exit(0);
			case "place":
				placeAt(x,z);
				break;
			case "hover":
				this.currentSelect = new Coord(x,y,z,-1);
				break;
			default:
				System.out.println("Unrecognized input: "+type);
				break;
		}
	}

	/**
	 * Tell the board to place a piece at (x,z) for the current player.
	 * If placement succeeded then increment to the next player.
	 * 
	 * @param x
	 *  The lateral of the desired piece location.
	 * @param z
	 *  The depth of the desired piece location.
	 */
	
	private void placeAt(int x, int z) {
		try {
			if (board.isXZvalid(x, z)
					&& board.placePieceAt(x, z, players.get(currentPlayer))) {
				incrementPlayer();
			} 
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Helper method, increase the current player by one.
	 * Return current player to first player after the final player.
	 */
	private void incrementPlayer() {
		int rollOver = players.size();
		currentPlayer++;
		if(currentPlayer == rollOver) {
			currentPlayer = 0;
		}
	}
}
