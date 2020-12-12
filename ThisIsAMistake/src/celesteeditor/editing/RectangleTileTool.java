package celesteeditor.editing;

import java.awt.Point;
import java.util.Arrays;

import javax.swing.Icon;

import celesteeditor.Main;
import celesteeditor.data.TileLevelLayer;
import celesteeditor.ui.autotiler.TerrainType;

public class RectangleTileTool extends TileTool {
	
	Point rectStart;
	
	private char[][] currentTileMap;
	
	public RectangleTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(TileLevelLayer tileLayer, TerrainType tileType, Point p, MouseAction action) {
		char[][] tileMap = tileLayer.tileMap;
		
		switch(action) {
		case PRESSED:
			rectStart = p;
			currentTileMap = tileMap;
			break;
		case DRAGGED:
			if(tileMap != currentTileMap) {
				rectStart = p;
				currentTileMap = tileMap;
			}
			lastMousePos = p;
			break;
		case RELEASED:
			if(rectStart != null) {
				Point topLeft = new Point(Math.min(p.x, rectStart.x), Math.min(p.y, rectStart.y));
				Point bottomRight = new Point(Math.max(p.x, rectStart.x), Math.max(p.y, rectStart.y));
				for(int i = topLeft.y; i <= bottomRight.y && i < Main.mapPanel.selectedLevel.bounds.height; i++) {
					for(int j = topLeft.x; j <= bottomRight.x && j < Main.mapPanel.selectedLevel.bounds.width; j++) {
						placeTile(tileLayer, tileType, new Point(j, i));
					}
				}
			}
			rectStart = null;
			break;
		}
		
	}

	@Override
	public char[][] getTileOverlay(char tile) {
		if(lastMousePos == null || rectStart == null) return new char[][] {{tile}};
		Point topLeft = new Point(Math.min(lastMousePos.x, rectStart.x), Math.min(lastMousePos.y, rectStart.y));
		Point bottomRight = new Point(Math.max(lastMousePos.x, rectStart.x), Math.max(lastMousePos.y, rectStart.y));
		char[][] res = new char[bottomRight.y - topLeft.y + 1][bottomRight.x - topLeft.x + 1];
		for(char[] row : res) {
			Arrays.fill(row, tile);
		}

		return res;
	}

	@Override
	public Point getTileOverlayPos() {
		if(rectStart == null || lastMousePos == null) {
			return lastMousePos;
		}
		return new Point(Math.min(lastMousePos.x, rectStart.x), Math.min(lastMousePos.y, rectStart.y));
	}
	
}
