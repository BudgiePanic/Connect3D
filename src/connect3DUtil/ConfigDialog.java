package connect3DUtil;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextArea;

import connect3DMain.Main;

/**
 * A configuration dialog to collect information from the player before the game begins.
 * Collects a player count and the desired rendering type.
 * Writes player input information into the main object.
 * If the user closes the modal dialog, the Main object's 'shouldStart' field will be set to false.
 * @author Benjamin
 *
 */
public final class ConfigDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9674151954765283L;
	
	private static final int WIDTH = 640, HEIGHT = 480; 
	
	/**
	 * A reference to an object residing on the main thread, so we can pass user input back to the main thread from the event queue thread.
	 */
	private Main main;
	
	/**
	 * A combo box that allows the player to select between 2 and 4 players.
	 */
	private JComboBox<Integer> playerCount = new JComboBox<Integer>(new Integer[] {2,3,4}) {{
		addItemListener(e->{
			main.numberPlayers = (Integer)e.getItem();
		});
	}};
	
	/**
	 * A combo box that allows the player to select between the size of the board.
	 */
	private JComboBox<Integer> boardSize = new JComboBox<Integer>(new Integer[] {4, 5, 6, 7, 8}) {{
		addItemListener(e->{
			main.boardSize = (Integer)e.getItem();
		});
	}};
	
	/**
	 * A combo box that allows the player to select the rendering type.
	 */
	JComboBox<String> renderType = new JComboBox<>(new String[] {"software","text","hardware"}) {{
		addItemListener(e->{
			main.renderType = (String)e.getItem();
		});
	}};
	
	/**
	 * Create a new modal dialog that blocks the event queue until the user clicks start or exits.
	 * @param main
	 *  A hook back to the main thread, so user input values can be passed back to the main thread.
	 */
	public ConfigDialog(Main main) {
		if(main == null) throw new IllegalArgumentException("main should not be null");
		this.main = main;
		//set default initial values on main
		this.main.numberPlayers = playerCount.getItemAt(0);
		this.main.renderType = renderType.getItemAt(0);
		this.main.boardSize = boardSize.getItemAt(0);
		this.main.shouldStart = false;
		this.setModal(true);
		init();
		this.setVisible(true);
	}

	/**
	 * Adds and configures components for this dialog window.
	 */
	private void init() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(WIDTH,HEIGHT);
		this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		this.setLayout(new GridLayout(0,3));
		this.setTitle("Connect3D: Configure Game Settings.");
		this.setResizable(false);
		getContentPane().add(new JTextArea("Number of players:") {{setEditable(false);}});
		getContentPane().add(new JTextArea("Rendering type:") {{setEditable(false);}});
		getContentPane().add(new JTextArea("Board dimension:") {{setEditable(false);}});
		getContentPane().add(playerCount);
		getContentPane().add(renderType);
		getContentPane().add(boardSize);
		getContentPane().add(new StartButton());
		this.pack();
		this.validate();
	}
	
	/**
	 * The start button disposes the ConfigDialog when pressed, releasing the SwingEvent Thread, which in turn starts the game on the main thread.
	 * @author Benjamin
	 *
	 */
	private class StartButton extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 54874565L;

		/**
		 * Start button has an action listener that destroys the dialog when pressed.
		 */
		StartButton(){
			setText("Start the Game!");
			addActionListener(e->{
				ConfigDialog.this.main.shouldStart = true;
				ConfigDialog.this.dispose();
			});
		}
	}
}
