package celesteeditor.data;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import celesteeditor.AtlasUnpacker;
import celesteeditor.BinaryPacker.Element;
import celesteeditor.ui.MapPanel;
import celesteeditor.util.TextureArea;

public class Decal implements ElementEncoded {
	public static ArrayList<String> decals = new ArrayList<>();

	public static void loadDecalsFromAtlas() {
		for(String path : AtlasUnpacker.gameplay.keySet()) {
			if(path.contains(":") && path.substring(path.indexOf(":") + 1).startsWith("decals/")) {
				decals.add(path.substring("decals/".length()));
			} else if(path.startsWith("decals/")) {
				decals.add(path.substring("decals/".length()));
			}
		}
		
		decals.sort(String.CASE_INSENSITIVE_ORDER);
	}
	
	public int x, y;
	
	public int scaleX = 1, scaleY = 1;
	
	private String texture;
		
	private TextureArea textureArea;
	
	public Decal(String tex) {
		setImagePath(tex);
	}
	
	public Decal() {}
	
	public String getTexturePath() {
		return texture;
	}
	
	public void setImagePath(String path) {
		texture = path;
		textureArea = null;
	}
	
	public TextureArea getTextureArea() {
		if(textureArea != null) return textureArea;
		textureArea = AtlasUnpacker.gameplay.get("decals/" + texture.replace('\\', '/'));
		if(textureArea == null) {
			textureArea = new TextureArea(MapPanel.defaultEntityTex, new Rectangle(0, 0, MapPanel.defaultEntityTex.getWidth(), MapPanel.defaultEntityTex.getHeight()));
		}
		return textureArea;
	}
	
	@Override
	public Element asElement() {
		Element res = new Element("decal");
		res.Attributes = new HashMap<>();
		res.Attributes.put("x", x);
		res.Attributes.put("y", y);
		res.Attributes.put("scaleX", scaleX);
		res.Attributes.put("scaleY", scaleY);
		res.Attributes.put("texture", texture);
		return res;
	}

	@Override
	public Decal fromElement(Element element) {
		float tempX = element.AttrFloat("x");
		float tempY = element.AttrFloat("y");
		if(((int) tempX) != tempX || ((int) tempY) != tempY) {
			System.out.println(element.Attr("texture") + ": Position isn't an integer (" + tempX + ", " + tempY + "). What the heck have you done?");
		}
		
		x = (int)tempX;
		y = (int)tempY;
		scaleX = element.AttrInt("scaleX", 1);
		scaleY = element.AttrInt("scaleY", 1);
		setImagePath(element.Attr("texture"));
		return this;
	}

}
