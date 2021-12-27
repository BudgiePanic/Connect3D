package connect3DRender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	 * This is to ensure the board is drawn once, so the user can see something
	 * before entering their input.
	 */
	private boolean firstTime;
	
	/**
	 * The face of the board the user has specified the board to be drawn from.
	 */
	private Face face = Face.FRONT;
	
	/**
	 * Observers who are interested in user events.
	 */
	private final List<Observer> observers = new ArrayList<>(1);
	
	/**
	 * Components that this renderer is supposed to draw each redraw call.
	 */
	private final List<Component> drawables = new ArrayList<>();
	
	/**
	 * Store the draw requests that are made by the renderable components.
	 */
	private final List<Draw> drawRequests = new ArrayList<>();

	/**
	 * Remember if the initialize method has been called.
	 */
	private boolean initialized;
	
	/**
	 * The active color. Used for drawing?
	 */
	private Piece activePiece;
	
	/**
	 * Store the names of all the Face types at compile type.
	 * Not sure if there is any better way to do this. At least it works.
	 */
	private static final List<String> faces = Collections.unmodifiableList(
		List.of(Face.values()).stream()
		.map(Face::name).collect(Collectors.toList())
	);
	
	//not happy with this system at the moment.
	//Could be replaced with a proper message passing system between the renderer and observers.
	private int x,y,z;
	private String inputType;
	/**
	 * Matches valid row column inputs
	 */
	private static final Pattern inputPattern = Pattern.compile("\\d+ \\d+");
	/**
	 * Matches user program exit string
	 */
	private static final Pattern exitPattern = Pattern.compile(".*(exit)|(EXIT)|(Exit).*");
	/**
	 * Scanner to connect to system in and read user input.
	 */
	private Scanner in;
	
	
	/**
	 * Package-private constructor so only the renderer factory can generate them.
	 */
	TextRenderer(){
		this.firstTime = true;
		this.initialized = false;
	}
	
	// Cylinder drawing not implemented for the text renderer
	@Override
	public void drawCylinderAt(int x, int y, int z, float radius, float height) {}

	//Cube drawing not implemented for the text renderer.
	@Override
	public void drawCubeAt(int x, int y, int z, float width, float height) {}

	@Override
	public void drawSphereAt(int x, int y, int z, float radius) {
		//Currently ignoring radius. It's just a unit sphere.
		drawRequests.add(new Draw(x,y,z,Type.SPHERE, activePiece));
	}
	
	@Override
	public void drawMessage(String msg) {
		if(msg == null) throw new IllegalArgumentException();
		System.out.println(msg); //text based message drawing...
	}

	@Override
	public void setActiveColor(Piece p) {
		if(p == null) throw new IllegalArgumentException();
		this.activePiece = p;
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
		this.activePiece = Piece.EMPTY;
		this.x = -1; //put some default values into the user input fields
		this.y = -1;
		this.z = -1;
		this.inputType = "";
		in = new Scanner(System.in);
	}
	
	@Override
	public void destroy() {
		if(this.in != null) {
			this.in.close();
		}
		this.initialized = false;
	}

	@Override
	public void pollEvents() throws IllegalStateException {
		if(!initialized) throw new IllegalStateException("Renderer not initialized");
		if(!firstTime) {
			System.out.println("Your move:");
			String userTyped = in.nextLine();
			//Handle the user input
			if (inputPattern.matcher(userTyped).find()) {
				//Try to parse two numbers
				if(handlePlacement(userTyped)) {
					notifyObservers(); //notify if successfully parsed input into numbers.
				}
			} else if (exitPattern.matcher(userTyped).find()) {
				//Notify if the user typed exit.
				this.inputType = "EXIT";
				notifyObservers();
			} else {
				//Update the face if the user typed one.
				if (faces.contains(userTyped)) {
					face = Face.valueOf(userTyped);
					drawMessage("Face: "+this.face.name());
				}
			} 
		}
		firstTime = false;
	}

	/**
	 * Helper method for user input handling.
	 * Handle the case of user entering a location to place a piece on the board.
	 * @param next
	 *  The text input the user typed.
	 * @return 
	 *  True if the string could be parsed into two separate integers.
	 */
	private boolean handlePlacement(String next) {
		//extract the two numbers from the string
		//put them into the x,y fields
		String[] numbs = next.split(" ");
		assert numbs.length == 2;
		try {
			this.x = Integer.parseInt(numbs[0]);
			this.z = Integer.parseInt(numbs[1]);
		} catch (NumberFormatException e) {
			return false;
		}
		//update the message type field
		this.inputType = "PLACE";
		return true;
	}

	@Override
	public void redraw() throws IllegalStateException {
		//staged drawing.
		this.drawRequests.clear();
		//Component's draw calls are stored as requests
		for(Component c : drawables) {
			c.draw(this);
		}
		//process(sort) the draw requests
		//execute the draw requests
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
		/**
		 * viewing the game from the front.
		 * If the X and Y values are the same
		 * then sort based off the z depth.
		 */
		FRONT {
			@Override
			int compare(Draw d1, Draw d2) {
				if(d1.x == d2.x &&
						d1.y == d1.y) {
					
				}
				return 0;
			}
		},
		/**
		 * viewing the game from behind
		 */
		BACK {
			
			@Override
			int compare(Draw d1, Draw d2) {
				// TODO Auto-generated method stub
				return 0;
			}
			
		},
		/**
		 * viewing the game from the left
		 */
		LEFT {
			
			@Override
			int compare(Draw d1, Draw d2) {
				// TODO Auto-generated method stub
				return 0;
			}
			
		},
		/**
		 * viewing the game from the right.
		 */
		RIGHT {

			@Override
			int compare(Draw d1, Draw d2) {
				// TODO Auto-generated method stub
				return 0;
			}
			
		},
		/**
		 * Viewing the game from a 'birds eye view' perspective
		 */
		TOP {

			@Override
			int compare(Draw d1, Draw d2) {
				// TODO Auto-generated method stub
				return 0;
			}
			
		};
		
		/**
		 * Compare two draw requests to see which one goes in front.
		 * @param d1 
		 *  The first draw request
		 * @param d2 
		 *  The second draw request
		 * @return 
		 *  +ve means d1 goes in front and should be kept
		 *  -ve means d2 goes in back and should not be drawn
		 *  0 means it doesn't matter as their draws won't interfere with each other.
		 */
		abstract int compare(Draw d1, Draw d2);
	}
	
	/**
	 * The types of geometry primitives that the renderer needs to be able to draw.
	 * @author Benjamin
	 *
	 */
	private static enum Type {
		CYLINDER,
		CUBE,
		SPHERE
	}
	
	/**
	 * Encapsulate a drawing request into a comparable object.
	 * Allows sorting of scene objects. Uses the strategy pattern to dynamically 
	 * sort based on the FACE. Immutable type;
	 * Could help create a scene graph.
	 * @author Benjamin
	 *
	 */
	private class Draw implements Comparable<Draw> {
		/**
		 * Lateral location of the object being drawn
		 */
		final int x;
		/**
		 * Height location of the object being drawn
		 */
		final int y;
		/**
		 * depth location of the object being drawn
		 */
		final int z;
		/**
		 * The color this geometry primitive will be.
		 */
		final Piece color;
		/**
		 * The type of geometry that will be drawn.
		 */
		final Type type;
		
		/**
		 * 
		 * @param x
		 * @param y
		 * @param z
		 * @param t
		 * @param color
		 */
		Draw(int x, int y, int z, Type t, Piece color){
			assert t != null; assert color != null;
			this.x = x; this.y = y; this.z = z;
			this.type = t; this.color = color;
		}
		
		@Override
		public int compareTo(Draw o) {
			return TextRenderer.this.face.compare(this, o);
		}
		
	}
}