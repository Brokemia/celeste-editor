package celesteeditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GlobalConfig {
	
	public String celesteDir;
	
	@Override
	public String toString() {
		String res = "GLOBAL CONFIG";
		res += "\ncelesteDir=" + celesteDir;
		return res;
	}
	
	public static GlobalConfig fromFile(File f) {
		try (Scanner sc = new Scanner(f)) {
			String contents = "";
			while(sc.hasNextLine()) {
				contents += sc.nextLine() + "\n";
			}
			
			return fromString(contents);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new GlobalConfig();
	}
	
	public static GlobalConfig fromString(String s) {
		GlobalConfig res = new GlobalConfig();
		String[] split = s.split("\\n\\r|\\r\\n|\\r|\\n");
		if(split.length != 0 && split[0].equals("GLOBAL CONFIG")) {
			for(int i = 1; i < split.length; i++) {
				String[] parts = split[i].split("=");
				if(parts.length >= 2) {
					switch(parts[0]) {
					case "celesteDir":
						res.celesteDir = parts[1];
						break;
					}
				}
			}
		}
		
		return res;
	}
	
}
