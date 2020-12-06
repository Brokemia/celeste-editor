package celesteeditor.editing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import celesteeditor.AtlasUnpacker;
import celesteeditor.ui.MapPanel;
import celesteeditor.util.Util;

public class EntityConfig {
	
	public enum VisualType {
		Image, Box, ImageBox
	}
	
	public String name;
	
	public VisualType visualType = VisualType.Image;
	
	private String imagePath;
	
	private BufferedImage image;
	
	public int imgOffsetX = 4, imgOffsetY = 8;
	
	public Color borderColor = Color.black, fillColor = Color.white;
		
	public String getImagePath() {
		return imagePath;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setImage(BufferedImage img) {
		image = img;
	}
	
	public void setImage(String imgPath) {
		image = null;
		if(imgPath.startsWith("Gameplay:")) {
			image = AtlasUnpacker.gameplay.get(imgPath.replace('\\', '/').substring("Gameplay:".length()));
		}
		
		if(image == null) {
			image = Util.getImage(imgPath);
		}
		imagePath = imgPath;
		if(image == null) {
			image = MapPanel.defaultEntityImg;
		}
	}
	
	@Override
	public String toString() {
		String res = "ENTITY CONFIG";
		res += "\nname=" + name;
		res += "\nvisualType=" + visualType;
		if(imagePath != null && !imagePath.isBlank())
			res += "\nimagePath=" + imagePath;
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
						res.setImage(parts[1]);
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
		if(res.image == null) {
			res.setImage("");
		}
		
		return res;
	}
}
