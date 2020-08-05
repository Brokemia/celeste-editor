package celesteeditor.editing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import celesteeditor.data.EntityProperty;
import celesteeditor.data.EntityProperty.PropertyType;

public class PlacementConfig {
	
	public enum PlacementType {
		Entity, Trigger, Decal
	}
	
	public String name;
	
	public PlacementType placementType = PlacementType.Entity;
	
	public ArrayList<EntityProperty> defaultProperties = new ArrayList<>();
	
	public PlacementConfig() {
		
	}
	
	public PlacementConfig(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		String res = "PLACEMENT CONFIG";
		res += "\nname=" + name;
		res += "\nplacementType=" + placementType;
		for(EntityProperty prop : defaultProperties) {
			res += "\nproperty=" + prop.name + "," + prop.type + "," + prop.value;
		}
		return res;
	}
	
	public static PlacementConfig fromFile(File f) {
		try (Scanner sc = new Scanner(f)) {
			String contents = "";
			while(sc.hasNextLine()) {
				contents += sc.nextLine() + "\n";
			}
			
			return fromString(contents);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new PlacementConfig();
	}
	
	public static PlacementConfig fromString(String s) {
		PlacementConfig res = new PlacementConfig();
		String[] split = s.split("\\n\\r|\\r\\n|\\r|\\n");
		if(split.length != 0 && split[0].equals("PLACEMENT CONFIG")) {
			for(int i = 1; i < split.length; i++) {
				String[] parts = split[i].split("=");
				if(parts.length >= 2) {
					switch(parts[0]) {
					case "name":
						res.name = parts[1];
						break;
					case "placementType":
						res.placementType = PlacementType.valueOf(parts[1]);
						break;
					case "property":
						String[] propParts = parts[1].split(",");
						String valueStr = String.join(",", Arrays.asList(propParts)
								.subList(2, propParts.length)
								.toArray(new String[0]));
						EntityProperty prop = new EntityProperty();
						prop.name = propParts[0];
						prop.type = PropertyType.valueOf(propParts[1]);
						prop.value = EntityProperty.convertFromString(valueStr, prop.type);
						res.defaultProperties.add(prop);
						break;
					}
				}
			}
		}
		
		return res;
	}
	
}
