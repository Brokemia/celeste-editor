package celesteeditor.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import celesteeditor.Main;

public class EditingPanel extends JPanel {
	
	public enum EditPanel {
		Tiles, Entities;
	}

	public JTabbedPane tabbedPane = new JTabbedPane();
	
	public TilesTab tiles = new TilesTab();
	
	public EntitiesTab entities = new EntitiesTab();
	
	public EditingPanel() {
		setLayout(new BorderLayout());
		tabbedPane.addTab("Tiles", tiles);
		tabbedPane.addTab("Entities", entities);
		add(tabbedPane);
	}
	
	public EditPanel getCurrentPanel() {
		if(Main.editingPanel.tabbedPane.getSelectedComponent() == Main.editingPanel.tiles) {
			return EditPanel.Tiles;
		}
		
		return EditPanel.Entities;
	}
	
}
