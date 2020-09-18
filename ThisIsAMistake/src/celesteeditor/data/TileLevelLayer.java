package celesteeditor.data;

import celesteeditor.BinaryPacker.Element;

public class TileLevelLayer extends LevelLayer {
	public int exportMode;
	
	public String tileset;
		
	public char[][] tileMap;
	
	public void setTileString(String tiles) {
		tileMap = convertToTilemap(tiles);
	}
	
	private static char[][] convertToTilemap(String tiles) {
		String[] split = tiles.split("\\r\\n|\\n\\r|\\n|\\r");
		char[][] res = new char[split.length][];
		int maxLen = 0;
		
		for(int i = 0; i < split.length; i++) {
			maxLen = Math.max(maxLen, split[i].length());
		}
		
		for(int i = 0; i < split.length; i++) {
			res[i] = new char[split[i].length()];
			for(int j = 0; j < split[i].length(); j++) {
				res[i][j] = split[i].charAt(j);
			}
		}
		
		
		assert(tiles.equals(convertToTilestring(res)));
		
		return res;
	}
	
	private static String convertToTilestring(char[][] map) {
		if(map.length == 0) return "";
		String res = "";
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[i].length; j++ ) {
				res += map[i][j];
			}
			if(i != map.length - 1) {
				res += "\n";
			}
		}
		
		return res;
	}
	
	@Override
	public Element asElement() {
		Element res = super.asElement();
		res.Attributes.put("exportMode", exportMode);
		res.Attributes.put("tileset", tileset);
		res.Attributes.put("innerText", convertToTilestring(tileMap));
		return res;
	}

	@Override
	public TileLevelLayer fromElement(Element element) {
		super.fromElement(element);
		exportMode = element.AttrInt("exportMode", 0);
		tileset = element.Attr("tileset");
		tileMap = convertToTilemap(element.Attr("innerText"));
		
		return this;
	}
}
