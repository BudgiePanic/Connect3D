//Rendering strategy
//On initialize, create a swing window with a drawing pane
//swing events get accumulated in a list and are consumed in the poll events method
//redraw collects draw requests, processes them and then draws the circles only

package connect3DRender;

import java.awt.*;
import java.awt.Graphics;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import connect3DCore.Piece;
import connect3DUtil.MathUtil.Coord3D;
import static connect3DUtil.MathUtil.*;

/**
 * Uses java swing to display the game to the players and get player input.
 * @author Benjamin
 *
 */
public final class SwingRenderer implements Renderer {
	
	/**
	 * The dimension of the board we are drawing.
	 * This field is used to convert mouse clicks into user input.
	 */
	private final int dimension;
	
	/**
	 * The current color.
	 */
	private Piece color;
	
	/**
	 * Store list of objects that are interested in receiving updates from the renderer.
	 */
	private final List<Observer> observers = new ArrayList<>(1);
	
	/**
	 * List of components this renderer will attempt to draw.
	 */
	private final List<Component> drawables = new ArrayList<>();
	
	/**
	 * Root GUI element.
	 */
	private JFrame window;
	
	/**
	 * The width of the window in pixels.
	 */
	private static final int WIDTH = 1280;
	/**
	 * The height of the window in pixels.
	 */
	private static final int HEIGHT = 720;
	
	//message passing variables. These variables are passed to observers on notifyObservers()
	private volatile int x,y,z;
	private volatile String message;
	
	/**
	 * Store of draw requests made by components registered to this renderer.
	 */
	private volatile List<Draw> drawRequests = new ArrayList<Draw>();

	/**
	 * A matrix that converts from world space coordinates to normalized device coordinates.
	 */
	private volatile Matrix4 projection;
	
	/**
	 * A matrix that rotates coordinates horizontally along the X axis using the Y axis rotation matrix.
	 */
	private volatile Matrix4 rotateH;
	
	/**
	 * A matrix that rotates coordinates vertically about the Z axis using the X axis rotation matrix.
	 */
	private volatile Matrix4 rotateV;
	
	/**
	 * A matrix that 'rolls' coordinates using the Z axis.
	 */
	private volatile Matrix4 rotateR;
	
	/**
	 * The 'up/down' angle the user has specified
	 */
	private volatile double pitch;
	
	/**
	 * The 'left/right' angle the user has specified.
	 */
	private volatile double yaw;
	
	/**
	 * The maximum pitch value
	 */
	private final double pitch_max;
	
	//physics based rotation fields
	//private volatile double v_pitch, v_yaw, a_pitch, a_yaw;
	//private volatile long elapsed;
	
	/**
	 * Package private constructor so only the Render Factory can instantiate it.
	 * @param dimension
	 *  The d * d * d size of the board.
	 *  Used for converting mouse inputs into board space locations. 
	 */
	SwingRenderer(int dimension){
		assert dimension >= 4;
		this.dimension = dimension;
		this.pitch_max = toRadians(45.0);
	}

	@Override
	public void drawCylinderAt(int x, int y, int z, float radius, float height) {}

	@Override
	public void drawCubeAt(int x, int y, int z, float width, float height) {}

	@Override
	public void drawSphereAt(int x, int y, int z, float radius) {
		if(this.color == Piece.EMPTY) return; //don't draw transparent stuff.
		drawRequests.add(new Draw(new Coord3D(x,y,z), color, true) {
			@Override
			void draw(Graphics g) {
				Coord3D screen = toScreenSpace(getProjected(), WIDTH, HEIGHT);
				int a = (int)screen.x;
				int b = HEIGHT - (int)screen.y;
				int size = 30 - (5 * (int)getRotated().z); //TODO could use dynamic ball sizing
				Color old = g.getColor();
				g.setColor(Color.BLACK);
				g.fillOval(a-1,b-1,size+2,size+2);
				g.setColor(this.color.color());
				g.fillOval(a,b,size,size);
				g.setColor(old);
			}
		});
	}

	@Override
	public void drawMessage(String msg) {
		assert msg != null;
		this.drawRequests.add(new Draw(null, null, false) {
			@Override
			void draw(Graphics g) { g.drawString(msg, 5,25); }
		});
	}

	@Override
	public void setActiveColor(Piece p) { this.color = p; }

	@Override
	public void addObserver(Observer o) { observers.add(o); }

	@Override
	public void removeObserver(Observer o) { observers.remove(o); }

	@Override
	public void notifyObservers() {
		assert message != null;
		for(Observer o : observers) {
			o.update(x,y,z,message);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		double aspect = (double) HEIGHT / (double) WIDTH;
		this.projection = createProjectionM(0.1, 100.0, 70.0, aspect);
		this.pitch = 0.0;
		this.yaw = 0.0;
		this.rotateH = makeRotationMatrixY(pitch);
		this.rotateV = makeRotationMatrixX(yaw);
		this.rotateR = makeRotationMatrixZ(0.0); // don't 'roll' the points.
		/*
		this.v_pitch = 0.0;
		this.v_pitch = 0.0;
		this.a_pitch = 0.0;
		this.a_yaw = 0.0;
		*/
		try {
			SwingUtilities.invokeAndWait(() -> {
				window = new JFrame("Connect3D");
				window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				window.setResizable(false);
				window.setPreferredSize(new Dimension(WIDTH,HEIGHT));
				window.add(new DrawPanel());
				window.validate();
				window.pack();
				window.setVisible(true);
				/*
				this.elapsed = System.currentTimeMillis();
				*/
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new InitializationException();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new InitializationException();
		}
	}

	@Override
	public void destroy() {
		if(window != null) {
			window.dispose();
			window = null;
			System.gc();
		}
	}

	@Override
	public void pollEvents() throws IllegalStateException {}

	@Override
	public void redraw() throws IllegalStateException {
		try {
			SwingUtilities.invokeAndWait(() -> {
				/*//physics based scene rotation
				double timeSince = (double)(System.currentTimeMillis() - elapsed)/1000.0;
				elapsed = System.currentTimeMillis();
				pitch += v_pitch * timeSince;
				v_pitch += a_pitch * timeSince;
				a_pitch = -(0.5 * v_pitch);
				
				yaw += v_yaw * timeSince;
				v_yaw += a_yaw * timeSince;
				a_yaw = -(0.5 * v_yaw);
				
				if(v_pitch > -0.01 && v_pitch < 0.01) v_pitch = 0.0;
				if(v_yaw > -0.01 && v_yaw < 0.01) v_yaw = 0.0;
				*/
				if(pitch > pitch_max) pitch = pitch_max;
				if(pitch < -pitch_max) pitch = -pitch_max;
				this.rotateH = makeRotationMatrixY(yaw);
				this.rotateV = makeRotationMatrixX(pitch);
				this.drawRequests.clear();
				for(Component c : drawables) {
					c.draw(this); //collect draw requests from the scene
				}
				Collections.sort(this.drawRequests);
				window.repaint(); //window itself will traverse the list and draw the objects
			}
			);
		} catch (InvocationTargetException | InterruptedException b) {
			throw new RuntimeException(b);
		}
	}

	@Override
	public void addComponent(Component c) { this.drawables.add(c); }

	@Override
	public boolean removeComponent(Component c) { return this.drawables.remove(c); }

	/**
	 * Panel that performs IO for the swing renderer.
	 * Input via a mouse listener.
	 * @author Benjamin
	 *
	 */
	private class DrawPanel extends JPanel {
		private static final long serialVersionUID = -6549532872100015668L;
		/**
		 * Amount of pixels needed to achieve evenly spaced lines horizontally.
		 */
		private final int spaceX;
		/**
		 * Amount of pixels needed to achieve evenly spaced lines vertically.
		 */
		private final int spaceY;
		/**
		 * The column that the mouse is being hovered in
		 */
		private volatile int cursorX;
		/**
		 * The row that the mouse is being hovered in
		 */
		private volatile int cursorY;
		
		/**
		 * The last horizontal pixel the mouse was during a drag..
		 */
		private volatile int lastX = 0;
		
		/**
		 * The last vertical pixel the mouse was on during a drag;
		 */
		private volatile int lastY = 0;
		/**
		 * Field to keep track of mouse drag events. Always skip the first drag even in a drag sequence.
		 */
		private volatile boolean skipPress;
		/**
		 * Create a new panel.
		 * Comes initialized with a mouse listener that reports user events.
		 */
		DrawPanel(){
			init();
			int paddingColumns = 2;
			spaceX = (SwingRenderer.WIDTH / (dimension + paddingColumns))+1;
			spaceY = (SwingRenderer.HEIGHT / (dimension + paddingColumns));
			assert spaceX != 0;
			assert spaceY != 0;
			skipPress = false;
		}
		
		/**
		 * Get the draw panel ready.
		 */
		private void init() {
			setSize(WIDTH,HEIGHT);
			setPreferredSize(new Dimension(WIDTH,HEIGHT));
			setOpaque(true);
			setBackground(Color.WHITE);
			addMouseListener(new MouseListener()
				{
					@Override
					public void mouseClicked(MouseEvent e) {
						SwingRenderer.this.x = toBoardSpaceX(e.getX()) - 1;
						SwingRenderer.this.z = toBoardSpaceY(e.getY()) - 1;
						SwingRenderer.this.message = "place";
						SwingRenderer.this.notifyObservers();
					}
					@Override
					public void mousePressed(MouseEvent e) {}
					@Override
					public void mouseReleased(MouseEvent e) { skipPress = true; }
					@Override
					public void mouseEntered(MouseEvent e) {
						cursorX = toBoardSpaceX(e.getX());
						cursorY = toBoardSpaceY(e.getY());
					}
					@Override
					public void mouseExited(MouseEvent e) {}
				}
			);
			addMouseMotionListener(new MouseMotionListener() 
				{
					@Override
					public void mouseDragged(MouseEvent e) {
						double sensitvity = 0.5;
						if(!skipPress) {
							SwingRenderer.this.yaw += toRadians(e.getX() - lastX) * sensitvity; //direct yaw adjustment
							SwingRenderer.this.pitch += toRadians(e.getY() - lastY) * sensitvity; //direct pitch adjustment
//							SwingRenderer.this.v_yaw   = sensitvity * (double)(lastX - e.getX()); //physics based adjustment
//							SwingRenderer.this.v_pitch = sensitvity * (double)(lastY - e.getY()); //physics based adjustment
							
						}
						lastX = e.getX();
						lastY = e.getY();
						skipPress = false;
					}

					@Override
					public void mouseMoved(MouseEvent e) {
						int x,y;
						x = toBoardSpaceX(e.getX());
						y = toBoardSpaceY(e.getY());
						if(x != cursorX || y != cursorY) {
							cursorX = x; cursorY = y;
							//System.out.println("MouseMoved->"+x+" "+y);
							SwingRenderer.this.x = x - 1;
							SwingRenderer.this.z = y - 1;
							SwingRenderer.this.y = -1;
							SwingRenderer.this.message = "hover";
							SwingRenderer.this.notifyObservers();
						}
					}
				}
			);
			addMouseWheelListener((e)->{});
			//if(e.getWheelRotation > 0) then user scrolled down
		}
		
		/**
		 * Converts a screen x coordinate to a selection space x coordinate
		 * @param x
		 *  A screen space X location
		 * @return
		 *  The board space equivalent of this location
		 */
		private int toBoardSpaceX(int x) {
			float screenX = x;
			float columnWidth = this.spaceX;
			return (int) (screenX / columnWidth);
		}
		
		/**
		 * Converts a screen y coordinate to a selection space y coordinate
		 * @param y
		 *  A screen space Y location
		 * @return
		 *  The board space equivalent of this location
		 */
		private int toBoardSpaceY(int y) {
			float screenY = y;
			float rowWidth = this.spaceY;
			return (int) (screenY / rowWidth);
		}
		
		//PAINT CODE BELOW THIS POINT

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			paintInputLayerH(g, dimension+1);
			paintInputLayerV(g, dimension+1);
			//the draw requests should already be sorted
			for(Draw d : SwingRenderer.this.drawRequests) {
				d.draw(g);
			}
		}
		
		/**
		 * Draw the vertical lines that are used to show the grid for valid inputs.
		 * @param g
		 *  The graphics context
		 * @param numbLines 
		 *  The number of lines to draw
		 */
		private void paintInputLayerV(Graphics g, int numbLines) {
			if(numbLines <= 0) {return;}
			int depth = numbLines * this.spaceY;
			int x1,y1,x2,y2;
			y1 = depth;
			y2 = y1;
			x1 = this.spaceX;
			x2 = SwingRenderer.WIDTH - this.spaceX;
			g.drawLine(x1,y1,x2,y2);
			paintInputLayerV(g, numbLines-1);
		}

		/**
		 * Draw the horizontal lines that are used to show the grid for valid inputs.
		 * @param g
		 *  Graphics context
		 * @param numbLines 
		 *  The number of lines to draw
		 */
		private void paintInputLayerH(Graphics g, int numbLines) {
			if(numbLines <= 0) {return;}
			int offset = numbLines * this.spaceX;
			int x1,y1,x2,y2;
			x1 = offset;
			x2 = x1;
			y1 = this.spaceY;
			y2 = SwingRenderer.HEIGHT - spaceY;
			g.drawLine(x1,y1,x2,y2);
			paintInputLayerH(g, numbLines-1);
		}
		
	}
	
	/**
	 * Helper class to encapsulate draw calls from drawable objects.
	 * @author Benjamin
	 *
	 */
	private abstract class Draw implements Comparable<Draw> {
		/**
		 * Boolean to distinguish draw calls that have a depth value and those that don't.
		 */
		public final boolean inWorld;
		/**
		 * The location of this draw call in Normalized Device Coordinate space.
		 */
		private Coord3D projected = null;
		/**
		 * The location of this draw call after it has been translated and rotated to be in front of the virtual camera.
		 */
		private Coord3D rotated = null;
		/**
		 * The location of this draw call in world space.
		 */
		public final Coord3D location;
		/**
		 * The color of this draw call.
		 */
		protected final Piece color;
		
		/**
		 * Create a new draw request.
		 * @param location
		 * The world space location of the draw.
		 * @param color
		 * The color of the draw.
		 * @param inWorld
		 * True if this draw call is for an in world object that can be compared to others. 
		 */
		Draw(Coord3D location, Piece color, boolean inWorld){
			this.location = location;
			this.color = color;
			this.inWorld = inWorld;
		}
		
		@Override
		public int compareTo(Draw o) {
			if(!inWorld || !o.inWorld) return 0;
			Coord3D me = getRotated();
			Coord3D other = o.getRotated();
			if(me.z > other.z) return -1;
			if(other.z > me.z) return 1;
			return 0;
		}
		
		/**
		 * Get this draw call's Screenspace transformation.
		 * @return
		 * The location of the draw call in screen space.
		 */
		Coord3D getProjected() {
			assert inWorld == true;
			if(this.projected == null) {
				Matrix4 proj = SwingRenderer.this.projection;
				//rotate the point then translate it away from 0,0,0 origin
				Coord3D ndc = multiply(add(getRotated(),new Coord3D(0,0, 8)), proj);
				projected = ndc; 
			}
			return projected;
		}
		
		/**
		 * Get this draw call's coordinate after the rotation operation has been applied to it.
		 * @return
		 * The location of the draw call after it has been rotated.
		 */
		Coord3D getRotated() {
			if(this.rotated == null) {
				Matrix4 rotX = SwingRenderer.this.rotateH;
				Matrix4 rotY = SwingRenderer.this.rotateV;
				Matrix4 roll = SwingRenderer.this.rotateR;
				// translate point so board rotates around its centre rather than 0,0,0
				double offset = -(((double)(dimension)-0.5)/2.0);
				Coord3D shift = new Coord3D(offset, 0 ,offset);
				this.rotated = multiply(multiply(multiply(add(location, shift), rotX), rotY), roll);
			}
			return this.rotated;
		}
		
		/**
		 * Execute this draw request.
		 * @param g
		 * The SwingRenderer's graphics context
		 */
		abstract void draw(Graphics g);
	}
}
