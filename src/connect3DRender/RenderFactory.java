package connect3DRender;

public final class RenderFactory {
	
	public static Renderer Renderer(String type) throws IllegalArgumentException {
		//currently only supports the text renderer
		if(type.toLowerCase() == "text") return new TextRenderer();
		throw new IllegalArgumentException("Unknown renderer type! ->"+type);
	}
}
