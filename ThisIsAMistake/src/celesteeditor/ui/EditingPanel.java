package celesteeditor.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import celesteeditor.Main;

public class EditingPanel extends JPanel {
	
	public enum EditPanel {
		Tiles, Entities, Selection;
	}

	public JTabbedPane tabbedPane = new JTabbedPane();
	
	public TilesTab tiles = new TilesTab();
	
	public EntitiesTab entities = new EntitiesTab();
	
	public JPanel selection = new JPanel();
	
	public EditingPanel() {
		setLayout(new BorderLayout());
		tabbedPane.addTab("Tiles", tiles);
		tabbedPane.addTab("Entities", entities);
		tabbedPane.addTab("Selection", selection);
		add(tabbedPane);
	}
	
	public EditPanel getCurrentPanel() {
		if(Main.editingPanel.tabbedPane.getSelectedComponent() == tiles) {
			return EditPanel.Tiles;
		}
		if(Main.editingPanel.tabbedPane.getSelectedComponent() == entities) {
			return EditPanel.Entities;
		}
		
		return EditPanel.Selection;
	}
	
}
