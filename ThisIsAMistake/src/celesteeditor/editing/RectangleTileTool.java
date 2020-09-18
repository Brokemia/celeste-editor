package celesteeditor.editing;

import java.awt.Point;
import java.util.Arrays;

import javax.swing.Icon;

import celesteeditor.Main;
import celesteeditor.data.TileLevelLayer;

public class RectangleTileTool extends TileTool {
	
	Point rectStart;
	
	private char[][] currentTileMap;
	
	public RectangleTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(TileLevelLayer tileLayer, Tiletype tileType, Point p, MouseAction action) {
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
	public boolean[][] getTileOverlay() {
		if(lastMousePos == null || rectStart == null) return new boolean[][] {{true}};
		Point topLeft = new Point(Math.min(lastMousePos.x, rectStart.x), Math.min(lastMousePos.y, rectStart.y));
		Point bottomRight = new Point(Math.max(lastMousePos.x, rectStart.x), Math.max(lastMousePos.y, rectStart.y));
		boolean[][] res = new boolean[bottomRight.y - topLeft.y + 1][bottomRight.x - topLeft.x + 1];
		for(boolean[] row : res) {
			Arrays.fill(row, true);
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
