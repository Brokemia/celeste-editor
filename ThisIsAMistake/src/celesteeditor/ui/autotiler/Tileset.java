package celesteeditor.ui.autotiler;

import java.awt.image.BufferedImage;

public class Tileset
{
	private BufferedImage[][] tiles;

	public BufferedImage texture;

	public int tileWidth;

	public int tileHeight;

	public Tileset(BufferedImage texture, int tileWidth, int tileHeight)
	{
		this.texture = texture;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		tiles = new BufferedImage[texture.getWidth() / tileWidth][texture.getHeight() / tileHeight];
		for (int i = 0; i < texture.getWidth() / tileWidth; i++) {
			for (int j = 0; j < texture.getHeight() / tileHeight; j++) {
				tiles[i][j] = texture.getSubimage(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
			}
		}
	}
	
	public BufferedImage getTile(int x, int y) {
		return tiles[x][y];
	}
	
	public BufferedImage getTile(int index) {
		if(index < 0) {
			return null;
		}
		return tiles[index % tiles.length][index / tiles.length];
	}
}