package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import celesteeditor.editing.PlacementConfig;
import celesteeditor.editing.PlacementConfig.PlacementType;
import celesteeditor.ui.EntitiesTab.ColoredHoverListener;
import celesteeditor.util.EntityProperty;
import celesteeditor.util.Util;
import celesteeditor.util.EntityProperty.PropertyType;

public class PlacementConfigPopup extends JFrame {

public static final int width = 450;
	
	public static PlacementConfigPopup currentPopup;
	
	public PlacementConfig config;
	
	public JTextField nameField;
	
	public JPanel propsPanel = new JPanel();
		
	private JButton submitButton;
	
	public PlacementConfigPopup(PlacementConfig pc, PlacementType type) {
		super((pc == null ? "New" : "Edit") + " Placement");
		if(currentPopup != null) {
			currentPopup.dispose();
		}
		currentPopup = this;
		config = pc;
		
		this.setLayout(new GridLayout(4,1));
		
		nameField = new JTextField(pc == null ? "" : pc.name);
		add(nameField);
		
		JLabel addTool = new JLabel(new ImageIcon(Util.getImage("/assets/add.png")));
		addTool.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO add property
				
			}
		});
		addTool.addMouseListener(new ColoredHoverListener(addTool));
		add(addTool);
		
		propsPanel.setLayout(new GridLayout(0, 3));
		propsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		propsPanel.add(new JLabel("Type", JLabel.CENTER));
		propsPanel.add(new JLabel("Name", JLabel.CENTER));
		propsPanel.add(new JLabel("Default", JLabel.CENTER));
		
		if(pc != null) {
			for(EntityProperty p : pc.defaultProperties) {
				JTextField nf = new JTextField(p.name);
				propsPanel.add(nf);
				JComboBox<PropertyType> ptBox = new JComboBox<>(PropertyType.values());
				ptBox.setSelectedItem(p.type);
			    propsPanel.add(ptBox);
			    JTextField vf = new JTextField(p.value.toString());
				propsPanel.add(vf);
			}
		}
		
		add(propsPanel);
	    
	    submitButton = new JButton("Add");
		add(submitButton, BorderLayout.SOUTH);
	    
		setSize(width, 300);
		setLocationRelativeTo(null);
	}
	
	public void setOnAddAction(ActionListener listener) {
		submitButton.addActionListener(listener);
	}
	
}
