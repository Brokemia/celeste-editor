package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import celesteeditor.editing.PlacementConfig;
import celesteeditor.editing.PlacementConfig.PlacementType;
import celesteeditor.util.Util;

public class EntitiesTab extends JPanel {
	
	public static HashMap<String, PlacementConfig> placementConfig = new HashMap<>();
	
	JTabbedPane tabbedPane = new JTabbedPane();
	
	JPanel entities = new JPanel();
	
	JPanel triggers = new JPanel();

	public EntitiesTab() {
		setLayout(new BorderLayout());
		tabbedPane.addTab("Entities", entities);
		tabbedPane.addTab("Triggers", triggers);
		add(tabbedPane);
		
		entities.setLayout(new BorderLayout());
		JLabel addTool = new JLabel(new ImageIcon(Util.getImage("/assets/add.png")));
		addTool.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO add placementconfig
				PlacementConfigPopup popup = new PlacementConfigPopup(null, PlacementType.Entity);
				popup.setVisible(true);
			}
		});
		addTool.addMouseListener(new ColoredHoverListener(addTool));
		entities.add(addTool, BorderLayout.NORTH);
		
		JList<String> entityList = new JList<>(placementConfig.entrySet().stream().filter((e) -> e.getValue().placementType == PlacementType.Entity).map((e) -> e.getValue().name).toArray(String[]::new));
		entityList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scrollPane = new JScrollPane(entityList);
		scrollPane.setViewportView(entityList);
		entities.add(scrollPane);
	}
	
public static class ColoredHoverListener extends MouseAdapter {
		
		public final Color hoveringBg = new Color(40, 160, 80);
						
		public JLabel buttonLbl;
		
		public ColoredHoverListener(JLabel lbl) {
			buttonLbl = lbl;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			buttonLbl.setBackground(hoveringBg);
			buttonLbl.setOpaque(true);
			buttonLbl.repaint();
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			buttonLbl.setOpaque(false);
			buttonLbl.repaint();
		}
		
	}
	
}
