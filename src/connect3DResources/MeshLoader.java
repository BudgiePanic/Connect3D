package connect3DResources;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import connect3DUtil.Mesh;
import connect3DUtil.Texture;

/**
 * Loads model information from OBJ files.
 * @see "https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter09/chapter9.html"
 * @author Benjamin
 *
 */
public final class MeshLoader {
	/**
	 * static class.
	 */
	private MeshLoader() {}
	
	/**
	 * Converts OBJ file format lines into a Mesh object.
	 * @param lines
	 *  The source lines from an OBJ file.
	 * @param texture
	 *  The texture this Mesh will use. Can be null for a mesh with no texture.
	 * @return
	 *  A mesh object containing the file's information.
	 * @throws Exception
	 *  Thrown if there is a problem when creating the mesh. 
	 */
	public static Mesh loadMesh(List<String> lines, Texture texture) throws Exception{
		if(lines == null || lines.isEmpty()) throw new Exception("source lines must be provided!");
		if(texture == null) System.out.println("Creating a mesh with no texture...");
		
		List<Vector3f> vertices = new ArrayList<>();
		List<Vector2f> textureCoords = new ArrayList<>();
		List<Vector3f> normals = new ArrayList<>();
		List<Face> faces = new ArrayList<>();
		
		for(String line : lines) {
			System.out.println("Parsing line: ["+line+"]");
			String[] tokens = line.split("\\s+"); //split lines based on spaces
			switch(tokens[0]) {
				case "v":
					vertices.add(
						new Vector3f(
							Float.parseFloat(tokens[1]),
							Float.parseFloat(tokens[2]),
							Float.parseFloat(tokens[3])
						)
					);
					break;	
				case "vt":
					textureCoords.add(
						new Vector2f(
							Float.parseFloat(tokens[1]),
							Float.parseFloat(tokens[2])
						)
					);
					break;
				case "vn":
					normals.add(
						new Vector3f(
							Float.parseFloat(tokens[1]),
							Float.parseFloat(tokens[2]),
							Float.parseFloat(tokens[3])
						)
					);
					break;
				case "f":
					faces.add(
						new Face(tokens[1], tokens[2], tokens[3])
					);
					break;
				default:
					break; //ignore other tokens.
			}
		}
		
		return parseMeshInfo(vertices, textureCoords, normals, faces, texture);
	}
	
	/**
	 * @param vertices
	 * @param textureCoords
	 * @param normals
	 * @param faces
	 * @return
	 * @throws Exception 
	 */
	private static Mesh parseMeshInfo(List<Vector3f> vertices, List<Vector2f> textureCoords, List<Vector3f> normals, List<Face> faces, Texture texture) throws Exception {
		List<Integer> indices = new ArrayList<>();
		float[] arr_vertices = new float[vertices.size() * 3];
		int index = 0;
		for(Vector3f vertex : vertices) {
			arr_vertices[index * 3] = vertex.x;
			arr_vertices[(index * 3) + 1] = vertex.y;
			arr_vertices[(index * 3) + 2] = vertex.z;
			index++;
		}
		
		float[] arr_textureCoords = new float[vertices.size() * 2];
		float[] arr_normals = new float[vertices.size() * 3];
		
		for(Face face : faces) {
			for(IndexGroup idx : face.indexGroups) {
				processFace(idx, textureCoords, normals, indices, arr_textureCoords, arr_normals);
			}
		}
		int[] arr_indices = new int[indices.size()];
		arr_indices = indices.stream().mapToInt((Integer i) -> i).toArray();
		Mesh mesh = new Mesh(arr_vertices, arr_textureCoords, arr_indices, arr_normals, texture);
		return mesh;
	}

	/**
	 * 
	 * @param idx
	 * @param textureCoords
	 * @param normals
	 * @param indices
	 * @param arr_textureCoords
	 * @param arr_normals
	 */
	private static void processFace(IndexGroup idx, List<Vector2f> textureCoords, List<Vector3f> normals,
			List<Integer> indices, float[] arr_textureCoords, float[] arr_normals) {
		int vertexIndex = idx.indexPosition;
		indices.add(vertexIndex);
		if(idx.indexTextureCoord != IndexGroup.NO_VALUE) {
			Vector2f textureCoordinate = textureCoords.get(idx.indexTextureCoord);
			arr_textureCoords[vertexIndex * 2] = textureCoordinate.x;
			arr_textureCoords[(vertexIndex * 2) + 1] = 1.0f - textureCoordinate.y; //flip UV coordinate...
		}
		if(idx.indexVectorNormal != IndexGroup.NO_VALUE) {
			Vector3f normal = normals.get(idx.indexVectorNormal);
			arr_normals[vertexIndex * 3] = normal.x;
			arr_normals[(vertexIndex * 3) + 1] = normal.y;
			arr_normals[(vertexIndex * 3) + 2] = normal.z;
		}
	}
	
}

/**
 * A face is a series of index groups.
 * Typical face format:
 * "f 11/1/1 17/2/1 13/3/1"
 * @author Benjamin
 *
 */
class Face{
	
	static final int size = 3;
	final IndexGroup[] indexGroups = new IndexGroup[size];
	
	/**
	 * 
	 * @param group1
	 * @param group2
	 * @param group3
	 * @throws Exception 
	 */
	Face(String group1, String group2, String group3) throws Exception{
		System.out.println("Parsing Face: ["+group1+"] ["+group2+"] ["+group3+"]");
		indexGroups[0] = IndexGroup.parseLine(group1);
		indexGroups[1] = IndexGroup.parseLine(group2);
		indexGroups[2] = IndexGroup.parseLine(group3);
	}
}

class IndexGroup{
	
	static final int NO_VALUE = -1;
	
	int indexPosition;
	
	int indexTextureCoord;
	
	int indexVectorNormal;
	
	/**
	 * Convert a Face index group string into a index group object.
	 * @param line
	 * @return
	 *  An index group object.
	 * @throws Exception
	 *  Thrown in a problem occurs while parsing the line.
	 */
	static IndexGroup parseLine(String line) throws Exception{
		IndexGroup answer = new IndexGroup();
		System.out.println("Generating index group from: ["+line+"]");
		String[] tokens = line.split("/");
		int length = tokens.length;
		
		answer.indexPosition = Integer.parseInt(tokens[0]) - 1; // subtract one because our data structure starts from index zero.
		if(length > 1) {
			String textureCoordinate = tokens[1];
			if(textureCoordinate.length() > 0) {
				answer.indexTextureCoord = Integer.parseInt(textureCoordinate) - 1;
			}
			if(length > 2) {
				answer.indexVectorNormal = Integer.parseInt(tokens[2]) - 1;
			}
		}
		
		return answer;
	}
	
	private IndexGroup() {
		indexPosition = NO_VALUE;
		indexTextureCoord = NO_VALUE;
		indexVectorNormal = NO_VALUE;
	}
}




