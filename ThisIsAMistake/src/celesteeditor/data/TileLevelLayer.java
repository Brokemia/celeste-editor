package celesteeditor.data;

import celesteeditor.BinaryPacker.Element;
import celesteeditor.util.TextureArea;

public class TileLevelLayer extends LevelLayer {
	public int exportMode;
	
	public String tileset;
		
	public char[][] tileMap;
	
	private int width, height;
	
	public TextureArea[][] tileImgs;
	
	public TileLevelLayer(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setSize(int w, int h) {
		width = w;
		height = h;
	}
	
	public char getTile(int x, int y) {
		if(y < 0 || y >= getHeight() || x < 0 || x >= getWidth()) {
			throw new IndexOutOfBoundsException("Tile coords (" + x + "," + y + ") are out of bounds");
		}
		
		if(y >= tileMap.length || x >= tileMap[y].length) return '0';
		
		return tileMap[y][x];
	}
	
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
