package connect3DRender;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import connect3DCore.Piece;
import connect3DResources.FileLoader;
import connect3DResources.MeshLoader;
import connect3DUtil.Camera;
import connect3DUtil.ColorVector;
import connect3DUtil.DirectionalLight;
import connect3DUtil.Material;
import connect3DUtil.TransformManager;
import connect3DUtil.Mesh;
import connect3DUtil.PointLight;
import connect3DUtil.PointLight.Attenuation;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Hardware renderer uses the capabilities of LWJGL to display the game via OpenGL.
 * If you do not call the poll events method of the renderer, it will block and eventually crash.
 * Presumably this is because the event queue buffer fills up with unconsumed events.
 * @see "https://www.lwjgl.org/guide"
 * @see "https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/"
 * @author Benjamin
 *
 */
public final class HardwareRenderer implements Renderer {
	
	/**
	 * List of components this renderer is currently drawing.
	 */
	private final List<Component> drawables = new ArrayList<Component>();
	
	/**
	 * List of observers interested in receiving updates from this renderer.
	 */
	private final List<Observer> observers = new ArrayList<Observer>();
	
	private volatile int x = 0,y = 0,z = 0;
	private volatile String message;
//========================================
	/**
	 * The handle of the glfw window.
	 */
	private long a_window; 
	
	/**
	 * And object that encapsulates away the management of the world and projection martices.
	 */
	private final TransformManager transformManager = new TransformManager();
	
	/**
	 * Object that encapsulates input handling code.
	 */
	private InputHandler iHandler = new InputHandler();
	
	/**
	 * The width of the GLFW window in pixels
	 */
	private int WIDTH = 1280;
	
	/**
	 * The height of the GLFW window in pixels.
	 */
	private int HEIGHT = 720;
	
	/**
	 * the field of view being used by the hardware renderer.
	 */
	private float fov = 90.0f;
	
	/**
	 * remember if the renderer has been initialized.
	 */
	private boolean initialized = false;
	
	/**
	 * The shader program this renderer is using.
	 */
	private ShaderProgram shaderProgram;
	
	/**
	 * TODO TEMP
	 * A mesh that the renderer will draw each redraw.
	 */
	private Mesh mesh;
	
	/**
	 * TODO TEMP
	 * list of models that the renderer will paint each redraw.
	 */
	private final Set<Model> models = new HashSet<>();
	
	/**
	 * Scene camera.
	 */
	private final Camera camera;
	
	/**
	 * The light that is illuminating the scene.
	 */
	private final PointLight sceneLight;
	
	/**
	 * A light to illuminate the entire scene.
	 */
	private final DirectionalLight sunLight;
	
	/**
	 * The color of the ambient light of the scene.
	 */
	private final Vector3f ambientLight;
	
	/**
	 * The color that the drawable component has selected.
	 */
	private Piece activeColor = Piece.EMPTY;
	
	/**
	 * The N*N*N board dimension, so we can make the camera look at the center of the board.
	 */
	private final int boardDimension;
	
	/**
	 * Create a new Hardware renderer to render a board with boardDimension
	 * @param boardDimension
	 *  The dimensions of the board that this HW will be drawing.
	 */
	HardwareRenderer(int boardDimension){ 
		this.boardDimension = boardDimension;
		this.camera = new Camera();
		float dimension = (float) boardDimension;
		this.camera.radius = dimension * 2.0f;
		this.camera.cameraPointingAt.x = 0.0f;
		this.camera.cameraPointingAt.y = 0.0f;
		this.camera.cameraPointingAt.z = 0.0f;
		this.camera.updatePosition();
		//dim ambient light.
		this.ambientLight = new Vector3f(0.4f, 0.4f, 0.4f);
		Vector3f white = new Vector3f(1.0f, 1.0f, 1.0f);
		Vector3f lightPosition = new Vector3f();
		float lightIntensity = 20.0f;
		this.sceneLight = new PointLight(white, lightPosition, lightIntensity);
		this.sceneLight.setAttenuation(new Attenuation(0.0f, 0.0f, 1.0f));
		
		double angle = Math.toRadians(75.0);
		float xAngle = (float) Math.sin(angle);
		float yAngle = (float) Math.cos(angle);
		float zAngle = 0.0f;
		this.sunLight = new DirectionalLight(new Vector3f(xAngle, yAngle, zAngle), ColorVector.WHITE, 1.0f);
	}
	
	
	@Override
	public void addComponent(Component c) { this.drawables.add(c); }

	@Override
	public boolean removeComponent(Component c) { return this.drawables.remove(c); }
	
	@Override
	public void addObserver(Observer o) { this.observers.add(o); }

	@Override
	public void removeObserver(Observer o) { this.observers.remove(o); }

	@Override
	public void setActiveColor(Piece p) { this.activeColor = p; } 

	@Override
	public void notifyObservers() { this.observers.forEach((o)->{o.update(x, y, z, message);}); }

	@Override
	public void initialize() throws InitializationException {
		System.out.println("Hello LWJGL " + Version.getVersion() + "! inside hardware renderer");
		GLFWErrorCallback.createPrint(System.err).set();
		if(!glfwInit()) throw new InitializationException("GLFW library failed to initialize!");
		//set some stuff up.
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		//try to make the window.
		a_window = glfwCreateWindow(WIDTH, HEIGHT, "Connect3D", NULL, NULL);
		if(a_window == NULL) throw new InitializationException("GLFW window creation failed!");
		glfwMakeContextCurrent(a_window);
		glfwSwapInterval(1); //enable V-Sync
		glfwShowWindow(a_window);
		
		GL.createCapabilities();
		
		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); //TODO TEMP
		
		glEnable(GL_DEPTH_TEST);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		glfwSetFramebufferSizeCallback(a_window, (window, width, height)->{
			this.WIDTH = width; this.HEIGHT = height;
			glViewport(0,0,width,height);
		});
		
		glfwSetKeyCallback(a_window, (window, key, scancode, action, modifiers)->{ 
			iHandler.keyboard(key, action);
		});
		
		glfwSetScrollCallback(a_window, (window, xScroll, yScroll)->{
			iHandler.mouseScrolled(yScroll);
		});
		
		glfwSetCursorPosCallback(a_window, (window, xPos, yPos)->{ 
			iHandler.mouseMoved(xPos, yPos);
		});
		
		glfwSetMouseButtonCallback(a_window, (window, button, action, modifier)->{
			iHandler.mouseButton(button, action);
		});
		
		glfwSetCursorEnterCallback(a_window, (window, entered)->{
			iHandler.windowEvent(entered);
		});
		
		//Create shader program!
		String vertexSource;
		String fragmentSource;
		try {
			vertexSource = FileLoader.read("/connect3DResources/vertex.vs");
			fragmentSource = FileLoader.read("/connect3DResources/fragment.fs");
		} catch (Exception e) {
			throw new InitializationException(e.getMessage());
		}

		try {
			this.shaderProgram = new ShaderProgram();
			this.shaderProgram.createVertexShader(vertexSource);
			this.shaderProgram.createFragmentShader(fragmentSource);
			this.shaderProgram.link();
			this.shaderProgram.createUniform("projectionMatrix");
			this.shaderProgram.createUniform("worldAndViewMatrix");
			this.shaderProgram.createUniform("texture_sampler");
			//lighting uniforms
			this.shaderProgram.createMaterialUniform("material");
			this.shaderProgram.createUniform("ambientLight");
			this.shaderProgram.createUniform("specularPower");
			this.shaderProgram.createPointLightUniform("pointLight");
		} catch (Exception e) {
			e.printStackTrace();
			throw new InitializationException(e.getMessage());
		}
		
		try { 
			Material material = new Material();
//			Material material = new Material(FileLoader.loadAndCreateTexture("src/connect3DResources/textures/grassblock.png"));
			this.mesh = MeshLoader.loadMesh(FileLoader.readAllLines("/connect3DResources/models/sphere.obj"), material);
//			this.mesh = MeshLoader.loadMesh(FileLoader.readAllLines("/connect3DResources/models/bunny.obj"), material);
			
		} catch (Exception e) {
			throw new InitializationException(e.getMessage());
		}
		this.initialized = true;
		System.out.println("init HW renderer complete");
	}

	@Override
	public void destroy() {
		glfwFreeCallbacks(a_window);
		glfwDestroyWindow(a_window);
		if(shaderProgram != null) shaderProgram.delete();
		if(mesh != null) mesh.delete();
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		System.out.println("destroyed HW renderer");
	}
	
	@Override
	public boolean isActive() {
		if(!initialized) throw new IllegalStateException("uninitialized renderer cannot be active");
		return !glfwWindowShouldClose(a_window);
	}

	@Override
	public void pollEvents() throws IllegalStateException {
		//System.out.println("events polled");
		glfwPollEvents(); //this method activates any call back methods we registered in the initialize method...
	}
	@Override
	public void redraw() throws IllegalStateException {
		camera.updatePosition();
		for(Component c : drawables) {
			c.draw(this); //collect draw requests... this may add models to the models field.
		}
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		//activate shader program and update projection matrix and send projection matrix to GPU
		shaderProgram.bind();
		transformManager.updateProjectionMatrix(this.fov, WIDTH, HEIGHT, 0.1f, 100.0f);
		shaderProgram.uploadMat4f("projectionMatrix", transformManager.projectionMatrix);
		shaderProgram.uploadInteger("texture_sampler", 0); //sample from texture unit 0.
		transformManager.updateViewMatrix(camera);
		
		//make the light be the camera position
		sceneLight.getWorldPosition().x = camera.worldPosition.x;
		sceneLight.getWorldPosition().y = camera.worldPosition.y;
		sceneLight.getWorldPosition().z = camera.worldPosition.z;
		//update the light uniforms
		shaderProgram.uploadVec3f("ambientLight", ambientLight);
		shaderProgram.uploadFloat("specularPower", 10.0f);
		sceneLight.updateViewPosition(transformManager.viewMatrix);
		shaderProgram.uploadPointLight("pointLight", sceneLight);
		//update the direction light uniforms
		sunLight.updateViewDirection(transformManager.viewMatrix);
		shaderProgram.uploadDirectionalLight("directionalLight", sunLight);
		
		//draw each model
		for(Model m : models) {
			transformManager.updateWorldAndViewMatrix(m.getPosition(), m.getRotation(), m.getScale());
			shaderProgram.uploadMat4f("worldAndViewMatrix", transformManager.worldAndViewMatrix);
			m.ready();
			shaderProgram.uploadMaterial("material", m.getMesh().getMaterial());
			m.getMesh().draw();
		} 
		shaderProgram.unbind();
		
		glfwSwapBuffers(a_window);
		//System.out.println("redrawn");
	}
	
	@Override
	public void drawCylinderAt(int x, int y, int z, float radius, float height) {} //TODO

	@Override
	public void drawCubeAt(int x, int y, int z, float width, float height) {} //TODO

	@Override
	public void drawSphereAt(int x, int y, int z, float radius) {
		if(this.activeColor == Piece.EMPTY) return;
		Model m = new Model(mesh, activeColor.colorVector());
		//translate the model to be centred for the camera
		float shift = (float)this.boardDimension * 0.5f;
		shift -= radius * 0.5f;
		m.updatePosition(x - shift, y, z - shift);
		//m.updateScale(radius);
		m.updateScale(0.6f);
		models.add(m);
	} //TODO

	@Override
	public void drawMessage(String msg) {} //TODO

	/**
	 * Handles user input.
	 * @author Benjamin
	 *
	 */
	private class InputHandler {
		
		private boolean mouseOverWindow = false;
		private boolean isDragging = false;
		private boolean isRMBdragging = false;
		private boolean isPressing = false;
		private int xPos = -1, yPos = -1;
		private int lastX = -1, lastY = -1;
		private static final int press = GLFW_PRESS;
		private static final int release = GLFW_RELEASE;
		private static final int LMB = GLFW_MOUSE_BUTTON_1;
		private static final int RMB = GLFW_MOUSE_BUTTON_2;
		

		/**
		 * Handle key pressed.
		 * @param key
		 * @param action
		 */
		public void keyboard(int key, int action) {
			if(action == press) {
				if(key == GLFW_KEY_Q) {
					x = 0; y = 0;
				}
				if(key == GLFW_KEY_E) {
					x = 3; y = 3;
				}
				message = "place";
				notifyObservers();
			}
		} //TODO

		/**
		 * Called when the mouse cursor enters the window.
		 * @param entered
		 * if the mouse cursor entered the window.
		 */
		public void windowEvent(boolean entered) {
			mouseOverWindow = entered;
		}

		/**
		 * If LMB pressed, cast ray into scene and try to place.
		 * If RMB pressed, begin dragging.
		 * @param button
		 * @param action
		 */
		public void mouseButton(int button, int action) {
			if(!mouseOverWindow) return;
			if(action == press) {
				mousePressed(button);
			} else if(action == release) {
				if(isPressing) {
					mouseClicked(button, xPos, yPos);
				}
				isPressing = false;
			}
			
		}

		/**
		 * If the mouse moved while dragging, move the camera.
		 * If mouse moved while not dragging, cast ray into scene.
		 * @param xPos
		 * @param yPos
		 */
		public void mouseMoved(double xPos, double yPos) {
			if(!mouseOverWindow) return;
			this.xPos = (int)xPos; this.yPos = (int)yPos;
			if(isDragging) mouseDragged((int)xPos, (int)yPos);
			else ;/*castRay(); action == hover...*/ //TODO
		}

		/**
		 * Scroll up means decrease the camera distance to the board.
		 * Scroll down means increase the distance of the camera to the board.
		 * @param yScroll
		 * +ve value == scrolled up
		 * -ve value == scrolled down
		 */
		public void mouseScrolled(double yScroll) {
			if(!mouseOverWindow) return;
			if(yScroll > 0) {
				camera.radius--;
			} else if(yScroll < 0) {
				camera.radius++;
			}
		}
		
		/**
		 * Called when a mouse button is pressed down.
		 * @param button
		 *  The button that was pressed.
		 */
		private void mousePressed(int button) {
			isPressing = true;
			if(button == LMB) {
				isDragging = true;
			} else if(button == RMB) {
				isDragging = true; isRMBdragging = true;
				lastX = xPos; lastY = yPos;
			}
		}
		
		/**
		 * called when a mouse button that was pressed down was released.
		 * @param button
		 *  The mouse button that was clicked.
		 * @param xPos
		 *  The horizontal screen location that the click occurred on.
		 * @param yPos
		 *  The vertical screen location that the click occurred on.
		 */
		private void mouseClicked(int button, int xPos, int yPos) {
			if(button == RMB) isRMBdragging = false;
			else if (button == LMB) {
				//castRayIntoScene()
				//action = "place"
				//notify()....
			}
		}
		
		/**
		 * called when the mouse is being dragged.
		 * @param xPos
		 *  The mouse position during the drag event.
		 * @param yPos
		 *  The mouse position during the drag event.
		 */
		private void mouseDragged(int xPos, int yPos) {
			if(isRMBdragging) {
				float deltaPhi = xPos - lastX;
				float deltaTheta = yPos - lastY;
				camera.addToPhi(deltaPhi);
				camera.addToTheta(-deltaTheta);
				lastX = xPos; lastY = yPos;
			}
		}
		
	}
}

/**
 * The shader program class encapsulates shader related code.
 * @author Benjamin
 *
 */
class ShaderProgram {
	
	private final int programID;
	
	private int vertexShaderID;
	
	private int fragmentShaderID;
	
	private final Map<String, Integer> uniforms = new HashMap<>();
	
	ShaderProgram() throws Exception {
		programID = glCreateProgram();
		if(programID == 0) throw new Exception("Shader creation failed!");
	}

	/**
	 * 
	 * @param source
	 *  The source code of the vertex shader
	 * @throws Exception
	 */
	void createVertexShader(String source) throws Exception {
		vertexShaderID = createShader(source, GL_VERTEX_SHADER);
	}
	
	/**
	 * 
	 * @param source
	 *  The source code of the fragment shader
	 * @throws Exception
	 */
	void createFragmentShader(String source) throws Exception {
		fragmentShaderID = createShader(source, GL_FRAGMENT_SHADER);
	}
	/**
	 * Creates a shader, compiles it, and attaches it to the GL program.
	 * @param source
	 * @param type
	 * @return
	 *  The ID of the shader program.
	 * @throws Exception
	 */
	private int createShader(String source, int type) throws Exception {
		int shaderID = glCreateShader(type);
		if(shaderID == 0) throw new Exception("Failed to create shader! >>"+type);
		glShaderSource(shaderID, source);
		
		glCompileShader(shaderID);
		
		if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Shader compilation failed! >>"+glGetShaderInfoLog(shaderID,1024));
		}
		
		glAttachShader(programID, shaderID);
		return shaderID;
	}
	/**
	 * Attempts to link the GL program.
	 * Checks for errors.
	 * @throws Exception
	 */
	void link() throws Exception {
		glLinkProgram(programID);
		if(glGetProgrami(programID, GL_LINK_STATUS) == 0){
			throw new Exception("GL program linking failed! >>"+glGetProgramInfoLog(programID, 1024));
		}
		//detatch shaders, they are not needed after linking is completed
		if(vertexShaderID != 0) {
			glDetachShader(programID, vertexShaderID);
		}
		
		if(fragmentShaderID != 0) {
			glDetachShader(programID, fragmentShaderID);
		}
		
		glValidateProgram(programID);
		if(glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) {
			System.err.println("Warning! Shader program linking >>"+glGetProgramInfoLog(programID,1024));
		}
	}
	
	void bind() {
		glUseProgram(programID);
	}
	
	void unbind() {
		glUseProgram(0);
	}
	
	void delete() {
		unbind();
		if(programID != 0) glDeleteProgram(programID);
	}
	
	/**
	 * Checks to see if the shader program has a uniform with uniformName.
	 * Adds uniformLocation to Map, if it exists.
	 * @param uniformName
	 * @throws Exception
	 *  Thrown if the shader program does not have a uniform with uniformName.
	 */
	void createUniform(String uniformName) throws Exception {
		int uniformLocation = glGetUniformLocation(programID, uniformName);
		if(uniformLocation < 0) throw new Exception("Shader Program:"+programID+" Could not find uniform with name: "+uniformName);
		uniforms.put(uniformName, uniformLocation);
	}
	
	/**
	 * Checks to see if the shader program has a DirectionalLight uniform with the supplied name.
	 * @param uniformName
	 *  The name of the uniform.
	 * @throws Exception
	 *  Thrown if there is no uniform with the supplied name.
	 */
	public void createDirectionalLightUniform(String uniformName) throws Exception{
		createUniform(uniformName+".color");
		createUniform(uniformName+".direction");
		createUniform(uniformName+".intensity");
	}
	
	/**
	 * Checks to see if the shader program has a PointLightUniform with the given name.
	 * @param uniformName
	 *  The name of the point light uniform.
	 * @throws Exception
	 *  Thrown if the shader program does not have a uniform with the provided name 
	 */
	public void createPointLightUniform(String uniformName) throws Exception {
		createUniform(uniformName+".color");
		createUniform(uniformName+".viewPosition");
		createUniform(uniformName+".intensity");
		createUniform(uniformName+".att.constant");
		createUniform(uniformName+".att.linear");
		createUniform(uniformName+".att.exponent");
	}

	/**
	 * Checks to see if the shader program has a material uniform with the provided name.
	 * @param uniformName 
	 *  The name of the material uniform being checked for.
	 * @throws Exception 
	 *  Thrown if there is no material uniform with the provided name.
	 * 
	 */
	public void createMaterialUniform(String uniformName) throws Exception {
		createUniform(uniformName+".ambient");
		createUniform(uniformName+".diffuse");
		createUniform(uniformName+".specular");
		createUniform(uniformName+".hasTexture");
		createUniform(uniformName+".reflectance");
	}
	
	/**
	 * Upload a matrix4f to the GPU via a uniform.
	 * @param uniformName
	 * @param data
	 */
	public void uploadMat4f(String uniformName, Matrix4f data) {
		//Use auto managed external memory, the 'stack'.
		try(MemoryStack stackFrame = MemoryStack.stackPush()){
			FloatBuffer buffer = stackFrame.mallocFloat(4*4);
			data.get(buffer); //transfer the data out of the Matrix4f and into the buffer
			glUniformMatrix4fv(uniforms.get(uniformName), false, buffer); //upload the buffer to the GPU
		}
	}
	
	/**
	 * Upload an integer to the GPU via a uniform in the shader program.
	 * @param uniformName
	 * @param data
	 */
	public void uploadInteger(String uniformName, int data) {
		glUniform1i(uniforms.get(uniformName), data);
	}
	
	
	/**
	 * Upload a vector3f to the GPU via a uniform.
	 * @param uniformName
	 * @param data
	 */
	public void uploadVec3f(String uniformName, Vector3f data) {
		glUniform3f(uniforms.get(uniformName), data.x, data.y, data.z);
	}

	/**
	 * Upload material data to the GPU via a uniform in the shader program.
	 * @param uniformName
	 *  The name of the uniform that will recieve the material data.
	 * @param material
	 *  The material data.
	 */
	public void uploadMaterial(String uniformName, Material material) {
		uploadVec4f(uniformName+".ambient", material.getAmbient());
		uploadVec4f(uniformName+".diffuse", material.getDiffuse());
		uploadVec4f(uniformName+".specular", material.getSpecular());
		uploadInteger(uniformName+".hasTexture", 
				material.texture.isPresent() ? 1 : 0);
		uploadFloat(uniformName+".reflectance", material.getReflectance());
	}
	
	/**
	 * Upload a directional light to the shader program on the GPU via a uniform.
	 * @param uniformName
	 *  The name of the uniform
	 * @param light
	 *  The directional light being uploaded.
	 */
	public void uploadDirectionalLight(String uniformName, DirectionalLight light) {
		uploadVec3f(uniformName+".color", light.getColor());
		uploadVec3f(uniformName+".direction", light.getDirection());
		uploadFloat(uniformName+".intensity", light.getIntensity());
	}
	
	/**
	 * Upload the vector4f to the GPU via a uniform
	 * @param uniformName
	 *  The name of the uniform
	 * @param data
	 *  The data.
	 */
	public void uploadVec4f(String uniformName, Vector4f data) {
		glUniform4f(uniforms.get(uniformName), 
				data.x, data.y, data.z, data.w);
	}

	/**
	 * Upload point light data to the GPU via a uniform in the shader program.
	 * @param uniformName
	 *  The name of the uniform.
	 * @param light
	 *  The light.
	 */
	public void uploadPointLight(String uniformName, PointLight light) {
		uploadVec3f(uniformName+".color", light.getColor());
		uploadFloat(uniformName+".intensity", light.getIntensity());
		uploadVec3f(uniformName+".viewPosition", light.getViewPosition());
		PointLight.Attenuation att = light.getAttenuation();
		uploadFloat(uniformName+".att.constant", att.getConstant());
		uploadFloat(uniformName+".att.linear", att.getLinear());
		uploadFloat(uniformName+".att.exponent", att.getExponant());
	}

	/**
	 * Upload a float to the GPU via a uniform in the shader program.
	 * @param uniformName
	 *  The name of the uniform.
	 * @param f
	 *  The data being sent.
	 */
	public void uploadFloat(String uniformName, float f) {
		glUniform1f(uniforms.get(uniformName), f);
	}
}

/**
 * A model is a Mesh combined with transformations that move it into world space when applied.
 * The transformations are mutable objects.
 * Multiple models can reuse the same mesh object.
 * @author Benjamin
 *
 */
class Model{
	
	private final Mesh mesh;
	private final Vector3f position;
	private float scale;
	private final Vector3f rotation;
	private final Optional<Vector3f> color;
	
	/**
	 * Create a new model.
	 * @param mesh
	 *  The mesh that this model will use.
	 * @param color
	 *  The color of this model. The mesh will be drawn with this color iff it does not have a texture.
	 *  This value can be null. 
	 */
	Model(Mesh mesh, Vector3f color){
		this.mesh = mesh;
		this.position = new Vector3f(0,0,0);
		this.scale = 1.0f;
		this.rotation = new Vector3f(0,0,0);
		this.color = ((color == null) ? (Optional.empty()) : (Optional.of(color)));
	}
	
	/**
	 * Readies this model's mesh with its color information if there is no texture.
	 */
	public void ready() {
		Material material = mesh.getMaterial();
		if(material.texture.isEmpty() && color.isPresent()) {
			Vector4f col = new Vector4f(color.get(),1.0f);
			material.updateColor(col);
		}
	}

	/**
	 * Get the color that this model is using
	 * @return
	 *  The color of this model. May be null.
	 */
	Optional<Vector3f> getColor() {
		return this.color;
	}
	
	/**
	 * Return the position of this model in world space.
	 * @return
	 *  The mutable position object of this model. 
	 */
	Vector3f getPosition() {
		return position;
	}
	
	/**
	 * Return the rotation values this model uses.
	 * @return
	 *  The mutable rotation object of this model.
	 */
	Vector3f getRotation() {
		return rotation;
	}
	
	/**
	 * Return the scale value of this model.
	 * @return
	 *  The scale being applied to this model.
	 */
	float getScale() {
		return scale;
	}
	
	/**
	 * Update the transform that moves this model from model space to world space.
	 * @param x
	 *  The distance to move in the X dimension.
	 * @param y
	 *  The distance to move in the Y dimension.
	 * @param z
	 *  The distance to move in the Z dimension.
	 */
	void updatePosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}
	
	/**
	 * Update the scale this model will use.
	 * @param scale
	 *  The new scale from model space to world space.
	 */
	void updateScale(float scale) {
		this.scale = scale;
	}
	
	/**
	 * Update the rotation values this model will use.
	 * @param pitch
	 *  The rotation along the X axis IN DEGREES
	 * @param roll
	 *  The rotation along the Z axis IN DEGREES
	 * @param yaw
	 *  The rotation along the Y axis IN DEGREES
	 */
	void updateRotation(float pitch, float roll, float yaw) {
		rotation.x = pitch;
		rotation.y = yaw;
		rotation.z = roll;
	}
	
	/**
	 * Get the mesh that this model is using.
	 * @return
	 *  The mesh that this model is using
	 */
	Mesh getMesh() {
		return this.mesh;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mesh, position, rotation, scale);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Model other = (Model) obj;
		return Objects.equals(mesh, other.mesh) && Objects.equals(position, other.position)
				&& Objects.equals(rotation, other.rotation)
				&& Float.floatToIntBits(scale) == Float.floatToIntBits(other.scale);
	}
}