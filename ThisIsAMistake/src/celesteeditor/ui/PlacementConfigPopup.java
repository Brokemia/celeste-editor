package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import celesteeditor.Main;
import celesteeditor.data.EntityProperty;
import celesteeditor.data.EntityProperty.PropertyType;
import celesteeditor.editing.EntityConfig;
import celesteeditor.editing.PlacementConfig;
import celesteeditor.editing.PlacementConfig.PlacementType;
import celesteeditor.ui.PlacementsTab.ColoredHoverListener;
import celesteeditor.util.Util;

public class PlacementConfigPopup extends JFrame {

public static final int width = 450;
	
	public static PlacementConfigPopup currentPopup;
	
	public PlacementConfig config;
	
	public boolean editing;
	
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
		editing = pc != null;
		
		if(config == null) {
			config = new PlacementConfig();
			config.placementType = type;
		}
		// Add width and height if missing
		if(type == PlacementType.Trigger) {
			System.out.println("EditingTrigger");
			if(!config.defaultProperties.stream().anyMatch((ep) -> ep.name.equals("width"))) {
				System.out.println("w");
				config.defaultProperties.add(new EntityProperty("width", PropertyType.Integer, 8));
			}
			if(!config.defaultProperties.stream().anyMatch((ep) -> ep.name.equals("height"))) {
				config.defaultProperties.add(new EntityProperty("height", PropertyType.Integer, 8));
			}
		}
		
		JPanel entryPanel = new JPanel();
		entryPanel.setLayout(new GridBagLayout());
		
		nameField = new JTextField(pc == null ? "" : pc.name);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.weightx = 0.5;
		c.weighty = 1;
		entryPanel.add(nameField, c);
		
		JLabel addTool = new JLabel(new ImageIcon(Util.getImage("/assets/add.png"))) {
			public Dimension getPreferredSize() {
		       return new Dimension(currentPopup.getSize().width, super.getPreferredSize().height);
		   }
		};
		addTool.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				propsPanel.setVisible(true);
				JComboBox<PropertyType> ptBox = new JComboBox<>(PropertyType.values());
				ptBox.setSelectedItem(PropertyType.String);
			    propsPanel.add(ptBox);
				JTextField nf = new JTextField();
				propsPanel.add(nf);
			    JTextField vf = new JTextField();
			    propsPanel.add(vf);
			    revalidate();
			}
		});
		addTool.addMouseListener(new ColoredHoverListener(addTool));
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		if(!editing) {
			c.gridwidth = 2;
		}
		c.weightx = 0.5;
		c.weighty = 1;
		entryPanel.add(addTool, c);
		
		if(editing) {
			JLabel configButton = new JLabel(new ImageIcon(Util.getImage("/assets/options.png")));
			configButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					EntityConfig ec = Main.entityConfig.get(nameField.getText());
					if(ec == null) {
						ec = new EntityConfig();
						ec.name = nameField.getText();
						Main.entityConfig.put(ec.name, ec);
					}
					new EntityConfigPopup(ec).setVisible(true);
				}
			});
			configButton.addMouseListener(new ColoredHoverListener(configButton));
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 1;
			c.gridy = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			entryPanel.add(configButton, c);
		}
		
		JScrollPane propsScroll = new JScrollPane(propsPanel);
		propsPanel.setLayout(new GridLayout(0, 3));
		propsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		propsPanel.add(new JLabel("Type", JLabel.CENTER));
		propsPanel.add(new JLabel("Name", JLabel.CENTER));
		propsPanel.add(new JLabel("Default", JLabel.CENTER));
		if(config == null || config.defaultProperties.size() == 0) {
			propsPanel.setVisible(false);
		}
		
		if(config != null) {
			for(EntityProperty p : config.defaultProperties) {
				JComboBox<PropertyType> ptBox = new JComboBox<>(PropertyType.values());
				ptBox.setSelectedItem(p.type);
			    propsPanel.add(ptBox);
				JTextField nf = new JTextField(p.name);
				propsPanel.add(nf);
			    JTextField vf = new JTextField(p.value.toString());
			    propsPanel.add(vf);
			}
		}
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 2;
		c.gridheight = 9;
		c.gridwidth = 2;
		c.weightx = 0.5;
		c.weighty = 9;
		entryPanel.add(propsScroll, c);
		
		add(entryPanel);
	    
	    submitButton = new JButton(editing ? "Update" : "Add");
	    submitButton.addActionListener(this::setOnAddAction);
		add(submitButton, BorderLayout.SOUTH);
	    
		setSize(width, 300);
		setLocationRelativeTo(null);
	}
	
	@SuppressWarnings("unchecked")
	public void setOnAddAction(ActionEvent e) {
		String name = nameField.getText();
		if(name.isBlank()) {
			JOptionPane.showMessageDialog(null, "Placement name cannot be blank");
			return;
		} else if((!editing || !config.name.equals(name)) && PlacementsTab.placementConfig.entrySet().stream().anyMatch((entry) -> entry.getKey().equals(name))) {
			JOptionPane.showMessageDialog(null, "Placement \"" + name + "\" already exists");
			return;
		}
		
		EntityProperty prop = null;
		ArrayList<EntityProperty> props = new ArrayList<>();
		for(int i = 3; i < propsPanel.getComponentCount(); i++) {
			switch(i % 3) {
			case 0:
				prop = new EntityProperty();
				prop.type = (PropertyType)((JComboBox<PropertyType>)propsPanel.getComponent(i)).getSelectedItem();
				break;
			case 1:
				prop.name = ((JTextField)propsPanel.getComponent(i)).getText();
				final EntityProperty propFinal = prop;
				if(prop.name.isBlank()) {
					JOptionPane.showMessageDialog(null, "Property name cannot be blank");
					return;
				} else if(props.stream().anyMatch((ep) -> ep.name.equals(propFinal.name))) {
					JOptionPane.showMessageDialog(null, "Property \"" + prop.name + "\" is defined more than once");
					return;
				}
				break;
			case 2:
				try {
					prop.value = EntityProperty.convertFromString(((JTextField)propsPanel.getComponent(i)).getText(), prop.type);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Invalid default value for property \"" + prop.name + "\"");
					return;
				}
				props.add(prop);
				break;
			}
		}
		
		boolean renamed = editing && !name.equals(config.name);
		String oldName = config.name;
		
		config.name = name;
		config.defaultProperties = props;
		
		if(!editing) {
			PlacementsTab.placementConfig.put(config.name, config);
		} else if(renamed) {
			// Delete the old config
			File old = new File("config/placement/" + oldName + ".config");
			if(old.exists()) {
				old.delete();
			}
			
			PlacementsTab.placementConfig.remove(oldName);
			PlacementsTab.placementConfig.put(config.name, config);
		}
		
		Main.editingPanel.placements.refreshLists();
		
		try {
			File dir = new File("config/placement/");
			dir.mkdir();
			File f = new File("config/placement/" + config.name + ".config");
			PrintWriter pw = new PrintWriter(f);
			pw.append(config.toString());
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		dispose();
	}
	
}
