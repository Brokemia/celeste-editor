package celesteeditor.ui.autotiler;

import celesteeditor.util.TextureArea;

public class Tileset
{
	private TextureArea[][] tiles;

	public TextureArea texture;

	public int tileWidth;

	public int tileHeight;

	public Tileset(TextureArea texture, int tileWidth, int tileHeight)
	{
		this.texture = texture;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		tiles = new TextureArea[texture.width / tileWidth][texture.height / tileHeight];
		for (int i = 0; i < texture.width / tileWidth; i++) {
			for (int j = 0; j < texture.height / tileHeight; j++) {
				tiles[i][j] = texture.getSubtexture(i * tileWidth, texture.height - j * tileHeight - tileHeight, tileWidth, tileHeight);
			}
		}
	}
	
	public TextureArea getTile(int x, int y) {
		return tiles[x][y];
	}
	
	public TextureArea getTile(int index) {
		if(index < 0) {
			return null;
		}
		return tiles[index % tiles.length][index / tiles.length];
	}
}