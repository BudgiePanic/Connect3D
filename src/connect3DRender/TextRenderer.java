package connect3DRender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import connect3DCore.Piece;

//Another rendering strategy I just thought of is:
//Do a linear scan through all the draw requests
//based on the FACE, collect all the unique (A,B) pairs that could be rendered.
//Build a map of (A,B) -> Draws
//Try to put all Draws into the map, but only insert ones that survive their CULL check
//with the draw that is already in the map. 
//========================================================================================
//Then to render, we just sort the map entries and execute the draws within.

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
	 * Store a mapping of render space coords to draw requests.
	 * Allows quick filtering of draw requests that are behind other draw requests.
	 */
	private final Map<Coord, Draw> drawTable = new HashMap<>();

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
	 * The current screen space b.
	 * If this value changes, then the rasterizer should start drawing on a new line.
	 */
	private int b;
	
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
		this.drawTable.clear();
		//Populate the draw requests list.
		for(Component c : drawables) {
			c.draw(this);
		}
		//process the draw requests
		for(Draw w : drawRequests) {
			face.addToScreenTable(w, this);
		}
		List<Draw> sortedReqs = drawTable.values().stream().
				sorted().collect(Collectors.toList());
		if(sortedReqs.isEmpty()) return;
		//execute the draw requests TODO
			//initialize the b value based on the first request
			//Go through the sorted requests.
			//if a 'b' value changes, add a new line to the string builder {done via draw comparable method...}
	}

	@Override
	public void addComponent(Component c) { this.drawables.add(c); }

	@Override
	public boolean removeComponent(Component c) { return this.drawables.remove(c); }
	
	/**
	 * Helper method for addToDrawTable;
	 * @param c
	 *  the location we are checking to see if there is a draw request at.
	 * @return
	 *  null if the draw table has no draw request for location c.
	 */
	private Draw getFromDrawTable(Coord c) {
		return this.drawTable.get(c);
	}
	
	/**
	 * Helper method for addToDrawTable
	 * @param d
	 */
	private void addToDrawTable(Coord c, Draw d) {
		this.drawTable.put(c, d);
	}
	
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
		 * Closer Z draws will be kept.
		 * Small Z values = closer.
		 */
		FRONT {
			@Override
			int compare(Draw d1, Draw d2) {
				//send higher y values to be rendered first
				//if height is equal, render smaller x values first
				if(d1.y > d2.y) return 1;
				if(d1.y < d2.y) return -1;
				if(d1.x < d2.x) return 1;
				if(d1.x > d2.x) return -1;
				throw new RuntimeException("Exhausted all comparison options while sorting\n"
						+ "FRONT: "+d1+"\n"+d2);
			}

			@Override
			int cull(Draw d1, Draw d2) {
				if(d1.x == d2.x && d1.y == d1.y) {
					if(d1.z < d2.z) return 1; //d1 is closer, keep it
					return -1; //d2 must be closer, keep d2
				}
				//else: draw requests aren't related.
				return 0;
			}

			@Override
			void addToScreenTable(Draw d, TextRenderer r) {
				Coord coord = new Coord(d.x, d.y); //translate the draw to render space.
				Draw prev = r.getFromDrawTable(coord); 
				if(prev != null &&
						d.cull(prev) > 0) {
					//overwrite the map value
					r.addToDrawTable(coord, d);
				} else {
					//Nothing in the map, add the draw
					r.addToDrawTable(coord, d);
				}
			}
		},
		/**
		 * viewing the game from behind.
		 */
		BACK {
			
			@Override
			int compare(Draw d1, Draw d2) {
				//higher y values should be drawn first
				//if height is the same, draw bigger x values first.
				if(d1.y > d2.y) return 1;
				if(d1.y < d2.y) return -1;
				if(d1.x > d2.x) return 1;
				if(d1.x < d2.x) return -1;
				throw new RuntimeException("Exhausted all comparison options while sorting\n"
						+ "BACK: "+d1+"\n"+d2);
			}

			@Override
			int cull(Draw d1, Draw d2) {
				//If the X and Y values are the same, keep requests that are further away.
				if(d1.x == d2.x && d1.y == d2.y) {
					//if d1's z value is bigger, keep it
					return d1.z - d2.z;
				}
				return 0;
			}

			@Override
			void addToScreenTable(Draw d, TextRenderer r) {
				Coord coord = new Coord(d.x, d.y); 
				Draw prev = r.getFromDrawTable(coord); 
				if(prev != null && //overwrite the map value if it is closer
						d.cull(prev) > 0) {
					r.addToDrawTable(coord, d);
				} else {
					//Nothing in the map, add the draw
					r.addToDrawTable(coord, d);
				}	
			}
			
		},
		/**
		 * viewing the game from the left.
		 */
		LEFT {
			
			@Override
			int compare(Draw d1, Draw d2) {
				//render higher draws first
				//if height is equal, render bigger z values first
				throw new RuntimeException("Exhausted all comparison options while sorting\n"
						+ "LEFT: "+d1+"\n"+d2);
			}

			@Override
			int cull(Draw d1, Draw d2) {
				//if the z and the y values are the same
				//keep draws with a smaller x.
				if(d1.z == d2.z && d1.y == d2.y) {
					if(d1.x < d2.x) return 1;
					return -1;
				}
				return 0;
			}

			@Override
			void addToScreenTable(Draw d, TextRenderer r) {
				Coord coord = new Coord(d.z, d.y); 
				Draw prev = r.getFromDrawTable(coord); 
				if(prev != null && //overwrite the map value if it is closer
						d.cull(prev) > 0) {
					r.addToDrawTable(coord, d);
				} else {
					//Nothing in the map, add the draw
					r.addToDrawTable(coord, d);
				}	
			}
			
		},
		/**
		 * viewing the game from the right.
		 */
		RIGHT {

			@Override
			int compare(Draw d1, Draw d2) {
				//render taller requests first
				//if the height is the same, render smaller Z's first
				throw new RuntimeException("Exhausted all comparison options while sorting\n"
						+ "RIGHT: "+d1+"\n"+d2);
			}

			@Override
			int cull(Draw d1, Draw d2) {
				// if the two draws height's are the same
				// and their z values are the same
				// keep draws that have a bigger x
				if(d1.y == d2.y && d1.z == d2.z) {
					return d1.x - d2.x;
				}
				return 0;
			}

			@Override
			void addToScreenTable(Draw d, TextRenderer r) {
				Coord coord = new Coord(d.z, d.y); 
				Draw prev = r.getFromDrawTable(coord); 
				if(prev != null && //overwrite the map value if it is closer
						d.cull(prev) > 0) {
					r.addToDrawTable(coord, d);
				} else {
					//Nothing in the map, add the draw
					r.addToDrawTable(coord, d);
				}	
			}
			
		},
		/**
		 * Viewing the game from a 'birds eye view' perspective
		 */
		TOP {

			@Override
			int compare(Draw d1, Draw d2) {
				//render bigger Z's first
				//if the Z is equal, render smaller x's first
				throw new RuntimeException("Exhausted all comparison options while sorting\n"
						+ "TOP: "+d1+"\n"+d2);
			}

			@Override
			int cull(Draw d1, Draw d2) {
				//if the draw's x and z values are the same
				//then keep draws that have a bigger y value
				if(d1.x == d2.x && d1.z == d2.z) {
					return d1.y - d2.y;
				}
				return 0;
			}

			@Override
			void addToScreenTable(Draw d, TextRenderer r) {
				Coord coord = new Coord(d.x, d.z); 
				Draw prev = r.getFromDrawTable(coord); 
				if(prev != null && //overwrite the map value if it is closer
						d.cull(prev) > 0) {
					r.addToDrawTable(coord, d);
				} else {
					//Nothing in the map, add the draw
					r.addToDrawTable(coord, d);
				}	
			}
			
		};
		
		/**
		 * When drawing, the rasterizer(System.out lol) goes from top left to bottom right.
		 * Sort the draw requests to ensure the draws happen in the correct order.
		 * Uses the strategy pattern to choose how to sort based on the current FACE.
		 * @param d1
		 *  The first draw request being sorted
		 * @param d2
		 *  The second draw request being sorted
		 * @return
		 *  +ve if d1 is drawn first
		 *  -ve if d2 is drawn first
		 *  should not return 0 (draws should not be on top of each other)
		 */
		abstract int compare(Draw d1, Draw d2);
		
		/**
		 * Compare two draw requests to see which one goes in front.
		 * @param d1 
		 *  The first draw request
		 * @param d2 
		 *  The second draw request
		 * @return 
		 *  +ve means d1 goes in front and should be kept.
		 *  -ve means d2 goes in back and should not be drawn.
		 *  0 means it doesn't matter as their draws won't interfere with each other.
		 */
		abstract int cull(Draw d1, Draw d2);
		
		/**
		 * Put's the draw request into the draw table if it is front of an existing draw request.
		 * @param d
		 *  The draw request being put into the draw table.
		 * @param r 
		 * Reference to the renderer. Because enums are static we can't look at the runtime instance
		 * of the renderer unless we give the reference manually.
		 */
		abstract void addToScreenTable(Draw d, TextRenderer r);
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
	class Draw implements Comparable<Draw> {
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
		
		/**
		 * Determine if this draw request is in front of another draw request.
		 * @param o
		 *  The other draw request.
		 * @return
		 * +ve 'this' request is in front of 'o' and should be kept
		 * -ve 'this' is behind 'o' and should be dropped
		 * 0 'this' and 'o' are not on the same axis and should both be kept.
		 */
		public int cull(Draw o) {
			return TextRenderer.this.face.cull(this, o);
		}
		
		@Override
		public String toString() {
			//TODO
			return "NOT IMPLEMENTED YET";
		}
	}
	/**
	 * Helper class to store (A,B) Render space coordinates.
	 * @author Benjamin
	 */
	private static class Coord{
		/**
		 * The distance from the origin laterally (the x in (x,y))
		 */
		final int a;
		/**
		 * The from the origin vertically (the y in (x,y))
		 */
		final int b;
		/**
		 * A new Coord.
		 * @param a
		 *  The 'x' in traditional screen space.
		 * @param b
		 *  The 'y' in traditional screen space.
		 */
		Coord(int a,int b){
			this.a = a; this.b = b;
		}
	}
}
