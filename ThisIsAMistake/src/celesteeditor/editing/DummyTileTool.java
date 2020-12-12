package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

import celesteeditor.data.TileLevelLayer;
import celesteeditor.ui.autotiler.TerrainType;

public class DummyTileTool extends TileTool {
	
	public DummyTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(TileLevelLayer tileLayer, TerrainType tileType, Point p, MouseAction action) {
		
	}

	@Override
	public char[][] getTileOverlay(char tile) {
		return null;
	}

	@Override
	public Point getTileOverlayPos() {
		return null;
	}
	
}
