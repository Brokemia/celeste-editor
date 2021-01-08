package celesteeditor.editing;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import celesteeditor.AtlasUnpacker;
import celesteeditor.ui.MapPanel;
import celesteeditor.util.TextureArea;

public class EntityConfig {
	
	public enum VisualType {
		Image, Box, ImageBox
	}
	
	public String name;
	
	public VisualType visualType = VisualType.Image;
	
	private String texturePath;
		
	private TextureArea textureArea;
	
	public int imgOffsetX = 4, imgOffsetY = 8;
	
	public Color borderColor = Color.black, fillColor = Color.white;
		
	public String getTexturePath() {
		return texturePath;
	}
	
	public void setTexture(String texPath) {
		texturePath = texPath;
		textureArea = null;
	}
	
	public TextureArea getTextureArea() {
		if(textureArea != null) {
			return textureArea;
		}
		if(texturePath != null && texturePath.startsWith("Gameplay:")) {
			textureArea = AtlasUnpacker.gameplayTex.get(texturePath.replace('\\', '/').substring("Gameplay:".length()));
		} else {
			textureArea = new TextureArea(MapPanel.defaultEntityTex, new Rectangle(0, 0, MapPanel.defaultEntityTex.getWidth(), MapPanel.defaultEntityTex.getHeight()));
		}
		return textureArea;
	}
	
	@Override
	public String toString() {
		String res = "ENTITY CONFIG";
		res += "\nname=" + name;
		res += "\nvisualType=" + visualType;
		if(texturePath != null && !texturePath.isBlank())
			res += "\nimagePath=" + texturePath;
		res += "\nimageOffsetX=" + imgOffsetX;
		res += "\nimageOffsetY=" + imgOffsetY;
		if(borderColor != null)
			res += "\nborderColor=" + borderColor.getRGB();
		if(fillColor != null)
			res += "\nfillColor=" + fillColor.getRGB();
		return res;
	}
	
	public static EntityConfig fromFile(File f) {
		try (Scanner sc = new Scanner(f)) {
			String contents = "";
			while(sc.hasNextLine()) {
				contents += sc.nextLine() + "\n";
			}
			
			return fromString(contents);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new EntityConfig();
	}
	
	public static EntityConfig fromString(String s) {
		EntityConfig res = new EntityConfig();
		String[] split = s.split("\\n\\r|\\r\\n|\\r|\\n");
		if(split.length != 0 && split[0].equals("ENTITY CONFIG")) {
			for(int i = 1; i < split.length; i++) {
				String[] parts = split[i].split("=");
				if(parts.length >= 2) {
					switch(parts[0]) {
					case "name":
						res.name = parts[1];
						break;
					case "visualType":
						res.visualType = VisualType.valueOf(parts[1]);
						break;
					case "imagePath":
						res.setTexture(parts[1]);
						break;
					case "imageOffsetX":
						res.imgOffsetX = Integer.parseInt(parts[1]);
						break;
					case "imageOffsetY":
						res.imgOffsetY = Integer.parseInt(parts[1]);
						break;
					case "borderColor":
						res.borderColor = new Color(Integer.parseInt(parts[1]));
						break;
					case "fillColor":
						res.fillColor = new Color(Integer.parseInt(parts[1]));
						break;
					}
				}
			}
		}
		
		return res;
	}
}
