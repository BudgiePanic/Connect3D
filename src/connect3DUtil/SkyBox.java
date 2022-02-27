package connect3DUtil;

/**
 * 
 * @author Benjamin
 *
 */
public class SkyBox extends Model{
	
	/**
	 * 
	 * @param skyBoxMesh
	 */
	public SkyBox(Mesh skyBoxMesh) {
		super(skyBoxMesh, null);
		this.updateScale(20.0f);
	}
	
	/**
	 * Frees the resources this skybox is holding.
	 */
	public void delete() {
		this.getMesh().delete();
	}

}
