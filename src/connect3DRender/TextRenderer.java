package connect3DRender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	 * The current screen space a.
	 * if this value changes by more than one between draws, extra spaces should be drawn.
	 */
	private int a;
	
	/**
	 * The number of rows, columns that the board has.
	 * The text renderer specifically needs to know this information to draw the board
	 * from different faces.
	 */
	private final int dimension;
	
	/**
	 * Package-private constructor so only the renderer factory can generate them.
	 * @param dimension
	 *  The dimension of the board that this renderer will be drawing.
	 */
	TextRenderer(int dimension){
		this.firstTime = true;
		this.initialized = false;
		assert dimension >= 4;
		this.dimension = dimension;
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
			addToDrawTable(w);
		}
		List<Draw> sortedReqs = drawTable.values().stream().
				collect(Collectors.toList()); //TODO needs testing lol TODO
		Collections.sort(sortedReqs);
		Collections.reverse(sortedReqs);
		if(sortedReqs.isEmpty()) return; 
		//execute the draw requests TODO
			//initialize the b value based on the first request
			//initialize the a value
			//Go through the sorted requests.
			//convert draw request location to screen space, based on FACE.
			//if a 'b' value changes, add a new line to the string builder and reset a
			//if the 'a' value changes by more than one, add padding spaces.
		StringBuilder output = new StringBuilder();
		this.a = 0;
		this.b = sortedReqs.get(0).toRenderSpace().b;
		for(Draw d : sortedReqs) {
			Coord screenSpace = d.toRenderSpace();
			if(this.b != screenSpace.b) {
				this.a = 0;
				this.b++;
				output.append('\n');
			}
			while(a < screenSpace.a) {
				a++; output.append(' ');
			}
			output.append(d.color.charRep());
			a++;
		}
		
		System.out.println(output.toString());
	}
	
	/**
	 * Helper method to populate the draw table map.
	 * Only adds eligible draw requests to the draw table.
	 * @param d
	 *  The draw request that is being tested for eligibility to be drawn.
	 */
	private void addToDrawTable(Draw d) {
		//figure out this draw's location in render space
		//get the draw that is already being drawn to this location
		//if there is no draw, then add this one
		//if there is a draw then add this one if the other one is transparent
		//or if this one passes the cull check
		Coord here = d.toRenderSpace();
		Draw prev = this.drawTable.get(here);
		if(prev == null) {
			drawTable.put(here, d);
		} else if (	prev.color == Piece.EMPTY ||
					d.cull(prev) > 1 ) {
			drawTable.put(here, d);
		}
	}

	@Override
	public void addComponent(Component c) { this.drawables.add(c); }

	@Override
	public boolean removeComponent(Component c) { return this.drawables.remove(c); }
	
	/**
	 * Helper method for addToDrawTable in FACE;
	 * @param c
	 *  the location we are checking to see if there is a draw request at.
	 * @return
	 *  null if the draw table has no draw request for location c.
	 */
	private Draw getFromDrawTable(Coord c) {
		return this.drawTable.get(c);
	}
	
	/**
	 * Helper method for addToDrawTable in FACE
	 * @param c
	 *  The location that is being overwritten / added to 
	 * @param d
	 *  The draw request for that location
	 */
	private void addToDrawTable(Coord c, Draw d) {
		this.drawTable.put(c, d);
	}
	
	/**
	 * The faces of the board that can be drawn from.
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
			Coord toScreenSpace(int x, int y, int z, int dimension) {
				return new Coord(x, dimension-y);
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
					if(d1.z > d2.z) return 1;
					return -1;
				}
				return 0;
			}

			@Override
			Coord toScreenSpace(int x, int y, int z, int dimension) {
				int a = dimension - x;
				int b = dimension - y;
				return new Coord(a, b);
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
				if(d1.y > d2.y) return 1;
				if(d1.y < d2.y) return -1;
				if(d1.z > d2.z) return 1;
				if(d1.z < d2.z) return -1;
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
			Coord toScreenSpace(int x, int y, int z, int dimension) {
				int a = dimension - z;
				int b = dimension - y;
				return new Coord(a, b);
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
				if(d1.y > d2.y) return 1;
				if(d1.y < d2.y) return -1;
				if(d1.z < d2.z) return 1;
				if(d1.z > d2.z) return -1;
				throw new RuntimeException("Exhausted all comparison options while sorting\n"
						+ "RIGHT: "+d1+"\n"+d2);
			}

			@Override
			int cull(Draw d1, Draw d2) {
				// if the two draws height's are the same
				// and their z values are the same
				// keep draws that have a bigger x
				if(d1.y == d2.y && d1.z == d2.z) {
					if(d1.x > d2.x) return 1;
					return -1;
				}
				return 0;
			}

			@Override
			Coord toScreenSpace(int x, int y, int z, int dimension) {
				int a = z;
				int b = dimension - y;
				return new Coord(a,b);
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
				if(d1.z > d2.z) return 1;
				if(d1.z < d2.z) return -1;
				if(d1.x < d2.x) return 1;
				if(d1.x > d2.x) return -1; 
				throw new RuntimeException("Exhausted all comparison options while sorting\n"
						+ "TOP: "+d1+"\n"+d2);
			}

			@Override
			int cull(Draw d1, Draw d2) {
				assert d1.color != Piece.EMPTY;
				assert d2.color != Piece.EMPTY;
				//if the draw's x and z values are the same
				//then keep draws that have a bigger y value
				if(d1.x == d2.x && d1.z == d2.z) {
					if(d1.y > d2.y) return 1;
					return -1;
				}
				return 0;
			}

			@Override
			Coord toScreenSpace(int x, int y, int z, int dimension) {
				int a = x;
				int b = dimension - z;
				return new Coord(a,b);
			}
			
		};
		
		/**
		 * When drawing, the rasterizer(System.out lol) goes from top left to bottom right.
		 * Sort the draw requests to ensure the draws happen in the correct order.
		 * Uses the strategy pattern to choose how to sort based on the current FACE.
		 * {Compares the draw requests locations in screen space?}
		 * currently compares world space locations.
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
		 * Convert an x,y,z location to a,b screen space
		 * @param x
		 *  the horizontal
		 * @param y
		 *  the vertical
		 * @param z
		 *  the depth
		 * @param dimension 
		 *  The dimension of the board, used in world space to screen space calculations.
		 * @return
		 *  this location in a,b screen space
		 */
		abstract Coord toScreenSpace(int x, int y, int z, int dimension);
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
		 * Encapsulate a drawing request.
		 * @param x
		 *  The world space lateral displacement from the origin.
		 * @param y
		 *  The world space vertical displacement from the origin.
		 * @param z
		 *  The world space depth displacement from the origin.
		 * @param t
		 *  The type of geometry primitive being drawn.
		 * @param color
		 *  The color of the geometry.
		 *  
		 */
		Draw(int x, int y, int z, Type t, Piece color){
			assert t != null; assert color != null;
			this.x = x; this.y = y; this.z = z;
			this.type = t; this.color = color;
		}
		
		/**
		 * Converts this draw requests coordinates to screen space.
		 * @return
		 *  A location with the screen space coords of this draw request.
		 */
		public Coord toRenderSpace() {
			return TextRenderer.this.face.toScreenSpace(x,y,z,
					TextRenderer.this.dimension-1 //dimension = 4 ->> 0 - 3 index range
			);
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
			return this.type.name()+" x:"+x+" y:"+y+" z:"+z;
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
		public final int a;
		/**
		 * The from the origin vertically (the y in (x,y))
		 */
		public final int b;
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
		
		@Override
		public String toString() {
			return "Coord: a->"+a+" b->"+b;
		}

		@Override
		public int hashCode() {
			return Objects.hash(a, b);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Coord))
				return false;
			Coord other = (Coord) obj;
			return a == other.a && b == other.b;
		}
		
	}
}
