package celesteeditor.editing;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Tiletype {

	public String name = "Air";
	
	public boolean fg;
	
	public char tile = '0';
	
	public Color color = Color.pink;
	
	public Tiletype(String name, boolean fg, char tile, Color color) {
		this.name = name;
		this.fg = fg;
		this.tile = tile;
		this.color = color;
	}
	
	public Tiletype(String name, char tile, Color color) {
		this(name, true, tile, color);
	}
	
	public Tiletype(String name, boolean fg, char tile) {
		this(name, fg, tile, Color.pink);
	}
	
	public Tiletype() {}
	
	@Override
	public String toString() {
		String res = "TILE CONFIG";
		res += "\nname=" + name;
		res += "\nfg=" + fg;
		res += "\ntile=" + (int)tile;
		if(color != null)
			res += "\ncolor=" + color.getRGB();
		return res;
	}
	
	public static Tiletype fromFile(File f) {
		try (Scanner sc = new Scanner(f)) {
			String contents = "";
			while(sc.hasNextLine()) {
				contents += sc.nextLine() + "\n";
			}
			
			return fromString(contents);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new Tiletype();
	}
	
	public static Tiletype fromString(String s) {
		Tiletype res = new Tiletype();
		String[] split = s.split("\\n\\r|\\r\\n|\\r|\\n");
		if(split.length != 0 && split[0].equals("TILE CONFIG")) {
			for(int i = 1; i < split.length; i++) {
				String[] parts = split[i].split("=");
				if(parts.length >= 2) {
					switch(parts[0]) {
					case "name":
						res.name = parts[1];
						break;
					case "fg":
						res.fg = Boolean.parseBoolean(parts[1]);
						break;
					case "tile":
						res.tile = (char)Integer.parseInt(parts[1]);
						break;
					case "color":
						res.color = new Color(Integer.parseInt(parts[1]));
						break;
					}
				}
			}
		}
		
		return res;
	}
	
}
