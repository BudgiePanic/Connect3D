package connect3DRender;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import connect3DCore.Piece;

/**
 * The text renderer uses the command line to perform IO.
 * Simple, boring, good for testing?
 * @author Benjamin
 *
 */
public final class TextRenderer implements Renderer {
	
	/**
	 * Don't ask for user input the first time the renderer is polled for input.
	 * This is to ensure the board is drawn once, so the user can see something.
	 */
	private boolean firstTime;
	
	/**
	 * The face of the board the user has specified the board to be drawn from.
	 */
	private Face face = Face.FRONT;
	
	/**
	 * Observers who are interested in user events.
	 */
	private List<Observer> observers = new ArrayList<>(1);
	
	/**
	 * Components that this renderer is supposed to draw each redraw call.
	 */
	private List<Component> drawables = new ArrayList<>();

	/**
	 * Remember if the initialize method has been called.
	 */
	private boolean initialized;
	
	private int x,y,z;
	private String inputType;
	/**
	 * Matches valid row column inputs
	 */
	private static Pattern inputPattern = Pattern.compile("\\d+ \\d+");
	
	/**
	 * Package-private constructor so only the renderer factory can generate them.
	 */
	TextRenderer(){
		this.firstTime = true;
		this.initialized = false;
	}
	
	// Can't draw 3D objects to command line
	@Override
	public void drawCylinderAt(int x, int y, int z, float radius, float height) {}

	//
	@Override
	public void drawCubeAt(int x, int y, int z, float width, float height) {
		//TODO
	}

	// Can't draw 3D objects to command line
	@Override
	public void drawSphereAt(int x, int y, int z, float radius) {}

	@Override
	public void setActiveColor(Piece p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addObserver(Observer o) {
		if(o == null) throw new IllegalArgumentException("Observer cannot be null!");
		this.observers.add(o);
	}

	@Override
	public void removeObserver(Observer o) { this.observers.remove(o); }

	@Override
	public void notifyObservers() {
		for(Observer o : observers) {
			o.update(x, y, z, inputType);
		}
	}

	@Override
	public void initialize() throws InitializationException { 
		this.initialized = true;
		this.x = -1; //put some default values into the user input fields
		this.y = -1;
		this.z = -1;
		this.inputType = "";
	}

	@Override
	public void pollEvents() throws IllegalStateException {
		if(!initialized) throw new IllegalStateException("Renderer not initialized");
		if(!firstTime) {
			firstTime = false;
			
			//TODO
			//Ask the user for an input
			//Parse the input
			System.out.print("Your move :>");
			
			
			//Notify observers of the input, if the input was worthy of notification.
			//Respond to the input
			
		}
	}

	@Override
	public void redraw() throws IllegalStateException {
		
	}

	@Override
	public void addComponent(Component c) { this.drawables.add(c); }

	@Override
	public boolean removeComponent(Component c) { return this.drawables.remove(c); }
	
	/**
	 * The faces of the board that can be drawn.
	 * Draw the board differently based on the face.
	 * @author Benjamin
	 *
	 */
	private static enum Face{
		FRONT {
			@Override
			void paint() {
				// TODO Auto-generated method stub
				
			}
		},
		BACK {
			@Override
			void paint() {
				// TODO Auto-generated method stub
				
			}
		},
		LEFT {
			@Override
			void paint() {
				// TODO Auto-generated method stub
				
			}
		},
		RIGHT {
			@Override
			void paint() {
				// TODO Auto-generated method stub
				
			}
		},
		TOP {
			@Override
			void paint() {
				// TODO Auto-generated method stub
				
			}
		};
		
		/**
		 * Repaint the components based on the selected face.
		 */
		abstract void paint();
	}

}
