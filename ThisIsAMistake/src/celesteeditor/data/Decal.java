package celesteeditor.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import celesteeditor.BinaryPacker.Element;
import celesteeditor.ui.MapPanel;
import celesteeditor.util.Util;

public class Decal implements ElementEncoded {
	/**
	 * Maps the decal texture paths to their actual location (in the mods folder or graphics)
	 */
	public static HashMap<String, String> decalLocations = new HashMap<>();
	
	static {
		File decalFolder = new File("bin/Atlases/Gameplay/decals");
		if(decalFolder.exists()) {
			loadDecalsFromFolder(decalFolder, "Atlases/Gameplay/decals", "");
		}
	}
	
	private static void loadDecalsFromFolder(File folder, String prefix, String subfolders) {
		for(File decal : folder.listFiles()) {
			if(decal.isDirectory()) {
				loadDecalsFromFolder(decal, prefix, subfolders + "\\" + decal.getName());
			} else if(decal.getPath().endsWith(".png")) {
				String name = subfolders + "\\" + decal.getName();
				decalLocations.put(name.substring(1), prefix + name);
			}
		}
	}
	
	public int x, y;
	
	public int scaleX = 1, scaleY = 1;
	
	private String texture;
	
	private BufferedImage image;
	
	public String getTexturePath() {
		return texture;
	}
	
	public void setTexture(String path) {
		image = Util.getImage("/" + decalLocations.getOrDefault(path, path).replace('\\', '/'));
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
