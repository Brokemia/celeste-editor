package celesteeditor.data;

import celesteeditor.BinaryPacker.Element;

public class IntTileLevelLayer extends LevelLayer {
	public int exportMode;
	
	public String tileset;
		
	public int[][] tileMap;
	
	public void setTileString(String tiles) {
		tileMap = convertToTilemap(tiles);
	}
	
	private static int[][] convertToTilemap(String tiles) {
		String[] split = tiles.split("\\r\\n|\\n\\r|\\n|\\r");
		int[][] res = new int[split.length][];
		
		for(int i = 0; i < split.length; i++) {
			String[] row = split[i].split(",");
			res[i] = new int[row.length];
			for(int j = 0; j < row.length; j++) {
				if(!row[j].equals("")) {
					res[i][j] = Integer.parseInt(row[j]);
				}
			}
		}
		
		assert(tiles.equals(convertToTilestring(res)));
		
		return res;
	}
	
	private static String convertToTilestring(int[][] map) {
		if(map.length == 0) return "";
		String res = "";
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[i].length; j++ ) {
				res += map[i][j];
				if(j != map[i].length - 1) {
					res += ",";
				}
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
	public IntTileLevelLayer fromElement(Element element) {
		super.fromElement(element);
		exportMode = element.AttrInt("exportMode", 0);
		tileset = element.Attr("tileset");
		tileMap = convertToTilemap(element.Attr("innerText"));
		
		return this;
	}
}
