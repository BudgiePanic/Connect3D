package connect3DUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import connect3DRender.Observer;

/**
 * Selection manager handles the conversion of mouse clicks to (x,z) column placement in the model.
 * @author Benjamin
 *
 */
public class SelectionManager implements Observer{
	
	/**
	 * The n*n*n dimension of the board.
	 * Used to determine if the selection is valid.
	 */
	private final int dimension;
	
	/**
	 * The mesh used by the bounding boxes.
	 */
	private final Mesh boundingBoxMesh;
	
	/**
	 * The object that contains the coordinate tranforms.
	 */
	private final TransformManager transforms;
	
	/**
	 * The boxes we are currently testing for intersection against.
	 */
	private final List<BoundingBox> hitboxes;
	
	/**
	 * The bounding boxes at the bottom of the column stacks.
	 */
	private final Map<Pointi, BoundingBox> baseBoxes;
	
	/**
	 * The minimum corner of the AABB currently being tested for intersection.
	 */
	private final Vector3f min;

	/**
	 * The maximum corner of the AABB currently being tested for intersection.
	 */
	private final Vector3f max;
	
	/**
	 * The intersection point, if one exists.
	 */
	private final Vector2f points;
	
	/**
	 * The direction of the camera.
	 * Updated each call.
	 */
	private Vector3f dir;
	
	/**
	 * Inverse projection matrix.
	 */
	private final Matrix4f invProjMat;
	
	/**
	 * Inverse view matrix
	 */
	private final Matrix4f invViewMat;
	
	/**
	 * A temporary vector for calculations
	 */
	private final Vector4f tempVec;
	/*
	 * Strategy:
	 * Accumulate Bounding boxes from EMPTY draws of pieces.
	 * Get the ray from the camera.
	 * perform intersection tests and keep closest + lowest box.
	 * convert box back to (x,z) column and return it as a point.
	 */
	
	
	//TODO we will start by just selecting the closest one.
	
	/**
	 * Selection manager handles the user piece placement location selection.
	 * @param dimension
	 *  The N*N*N size of the board. 
	 * @param transforms 
	 *  The object that holds the coordinate transforms.
	 * @param boundingBoxMesh 
	 *  The mesh that bounding boxes use. The cube.
	 */
	public SelectionManager(int dimension, TransformManager transforms, Mesh boundingBoxMesh) {
		int capacity = dimension * dimension * dimension;
		this.boundingBoxMesh = boundingBoxMesh;
		this.hitboxes = new ArrayList<>(capacity);
		this.baseBoxes = new HashMap<>();
		this.dimension = dimension;
		this.transforms = transforms;
		this.points = new Vector2f();
		this.max = new Vector3f();
		this.min = new Vector3f();
		this.dir = new Vector3f();
		this.invProjMat = new Matrix4f();
		this.invViewMat = new Matrix4f();
		this.tempVec = new Vector4f();
		init(1.5f); //TODO replace these magic shift amounts with calculated shift amounts
	}
	
	/**
	 * Populates the hitboxes and base boxes data structures such that the first row is populates (z = 0 is populated.)
	 * @param shift
	 *  The model shift amount, if any. 
	 */
	private void init(float shift) {
		int y = 0;
		for(int x = 0; x < dimension; x++) {
			for(int z = 0; z < dimension; z++) {
				BoundingBox box = new BoundingBox(boundingBoxMesh, new Vector3i(x,y,z));
				hitboxes.add(box);
				baseBoxes.put(new Pointi(x,z), box);
				box.updatePosition(x-shift, y, z-shift);
				box.updateScale(0.5f);
			}
		}
	}
	
	/**
	 * Determine which (x,z) column is being selected by the user for piece placement.
	 * @see "https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter23/chapter23.html" 
	 * @param width
	 *  width of the window in pixels.
	 * @param height 
	 *  height of the window in pixels.
	 * @param mousePos
	 *  The pixel space position of the mouse. 
	 * @param camera
	 *  The camera.
	 * @return the (x,z) column that the user is selecting.
	 *  Empty if no column is being selected.
	 */
	public Optional<Point> selectPlacement(int width, int height, Point mousePos, Camera camera){
		//screen space -> NDC
		float x,y,z;
		x = (float)(2 * mousePos.a) / (float)width - 1.0f;
		y = 1.0f - (float)(2 * mousePos.b) / (float)height;
		z = -1.0f;
		
		invProjMat.set(transforms.projectionMatrix);
		invProjMat.invert();
		//NDC -> view space
		tempVec.set(x, y, z, 1.0f);
		tempVec.mul(invProjMat);
		tempVec.z = -1.0f;
		tempVec.w = 0.0f;
		//View space -> world space
		invViewMat.set(transforms.viewMatrix);
		invViewMat.invert();
		tempVec.mul(invViewMat);
		dir.set(tempVec.x, tempVec.y, tempVec.z);
		
		BoundingBox selected = null;
		float closest = Float.MAX_VALUE;
		
		for(BoundingBox box : hitboxes) {
			min.set(box.getPosition());
			max.set(box.getPosition());
			min.add(-box.getScale(), -box.getScale(), -box.getScale());
			max.add(box.getScale(), box.getScale(), box.getScale());
			if(Intersectionf.intersectRayAab(camera.worldPosition, dir, min, max, points) && points.x < closest) {
				selected = box;
			}
		}
		if(selected == null) {
			return Optional.empty();
		} else {
			return Optional.of(new Point(selected.gameCoords.x, selected.gameCoords.z));
		}
	}
	
	/**
	 * Add a new Bounding box to test against.
	 * @param b
	 *  The new bounding box.
	 */
	private void add(BoundingBox b) {
		baseBoxes.get(new Pointi(b.gameCoords.x, b.gameCoords.z)).setAbove(b);
		hitboxes.add(b);
		
	}
	
	/**
	 * Add a new Bounding box to the data structured.
	 * @param x
	 *  The lateral.
	 * @param z
	 *  The depth.
	 * @param shift
	 *  The offset (if any) that should be applied to the model. 
	 */
	private void placed(int x, int z, float shift) {
		int y = (baseBoxes.get(new Pointi(x,z)).getTop().gameCoords.y)+1;
		if(y >= dimension) return;
		BoundingBox adding = new BoundingBox(boundingBoxMesh, new Vector3i(x,y,z));
		adding.updatePosition(x-shift, y, z-shift);
		adding.updateScale(0.5f);
		add(adding);
	}
	
	/**
	 * Get the list of bounding boxes
	 * @return
	 *  The bounding boxes
	 */
	public List<BoundingBox> getAABB(){
		return Collections.unmodifiableList(hitboxes);
	}
	
	/**
	 * Deletes any resources held by this SelectionManager
	 */
	public void delete() {
		this.boundingBoxMesh.delete();
	}

	@Override
	public void update(int x, int y, int z, String type) {
		switch(type.toLowerCase()) {
		case "place":
			placed(x,z,1.5f);
			break;
		default:
			break;
		}
	}

}
/**
 * Helper class to store a 2D point integer.
 * @author Benjamin
 *
 */
class Pointi{
	public final int x,y;
	Pointi(int x, int y){
		this.x = x;
		this.y = y;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pointi other = (Pointi) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
}
