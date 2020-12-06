package celesteeditor.data;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import celesteeditor.AtlasUnpacker;
import celesteeditor.BinaryPacker.Element;
import celesteeditor.ui.MapPanel;
import celesteeditor.util.Util;

public class Decal implements ElementEncoded {
	public static ArrayList<String> decals = new ArrayList<>();

	public static void loadDecalsFromAtlas() {
		for(String path : AtlasUnpacker.gameplay.keySet()) {
			if(path.startsWith("decals/")) {
				decals.add(path.substring("decals/".length()));
			}
		}
	}
	
	public int x, y;
	
	public int scaleX = 1, scaleY = 1;
	
	private String texture;
	
	private BufferedImage image;
	
	public Decal(String tex) {
		setTexture(tex);
	}
	
	public Decal() {}
	
	public String getTexturePath() {
		return texture;
	}
	
	public void setTexture(String path) {
		image = AtlasUnpacker.gameplay.get("decals/" + path.replace('\\', '/'));
		if(image == null) {
			image = Util.getImage("/" + path.replace('\\', '/'));
		}
		texture = path;
		if(image == null) {
			image = MapPanel.defaultEntityImg;
		}
	}
	
	public BufferedImage getImage() {
		return image;
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
			System.out.println(element.Name + ": Position isn't an integer (" + tempX + ", " + tempY + "). What the heck have you done?");
		}
		
		x = (int)tempX;
		y = (int)tempY;
		scaleX = element.AttrInt("scaleX", 1);
		scaleY = element.AttrInt("scaleY", 1);
		setTexture(element.Attr("texture"));
		return this;
	}

}
