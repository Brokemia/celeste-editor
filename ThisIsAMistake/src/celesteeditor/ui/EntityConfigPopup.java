package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import celesteeditor.Main;
import celesteeditor.editing.EntityConfig;
import celesteeditor.editing.EntityConfig.VisualType;

public class EntityConfigPopup extends JFrame {
	
	public static EntityConfigPopup currentPopup;
	
	public EntityConfig entityConfig;
	
	public JComboBox<VisualType> visualTypeBox;
	
	public JPanel imgPanel, boxPanel;
	
	public JTextField imgPathField, imgOffsetXField, imgOffsetYField, borderColorField, fillColorField;
	
	public EntityConfigPopup(EntityConfig ec) {
		super("Configure: " + ec.name);
		if(currentPopup != null) {
			currentPopup.dispose();
		}
		currentPopup = this;
		entityConfig = ec;
		add(new JLabel(ec.name, JLabel.CENTER), BorderLayout.NORTH);
		JPanel fieldPanel = new JPanel();
		add(fieldPanel);
		JButton submitButton = new JButton("Update");
		submitButton.addActionListener(this::actionPerformed);
		add(submitButton, BorderLayout.SOUTH);
		BoxLayout layout = new BoxLayout(fieldPanel, BoxLayout.Y_AXIS);
		fieldPanel.setLayout(layout);
		fieldPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel visualTypePanel = new JPanel();
		visualTypePanel.setLayout(new GridLayout(1, 2));
		visualTypePanel.add(new JLabel("Visual Type"));
		visualTypeBox = new JComboBox<>(VisualType.values());
		visualTypePanel.add(visualTypeBox);
		fieldPanel.add(visualTypePanel);
		
		imgPanel = new JPanel();
		imgPanel.setLayout(new BoxLayout(imgPanel, BoxLayout.Y_AXIS));
		JPanel pathPanel = new JPanel();
		pathPanel.setLayout(new GridLayout(1, 2));
		pathPanel.add(new JLabel("Image Path"));
		pathPanel.add(imgPathField = new JTextField(ec.getImagePath()));
		imgPanel.add(pathPanel);
		final JPanel offsetXPanel = new JPanel();
		offsetXPanel.setLayout(new GridLayout(1, 2));
		offsetXPanel.add(new JLabel("Image Offset X"));
		offsetXPanel.add(imgOffsetXField = new JTextField(ec.imgOffsetX + ""));
		imgPanel.add(offsetXPanel);
		final JPanel offsetYPanel = new JPanel();
		offsetYPanel.setLayout(new GridLayout(1, 2));
		offsetYPanel.add(new JLabel("Image Offset Y"));
		offsetYPanel.add(imgOffsetYField = new JTextField(ec.imgOffsetY + ""));
		imgPanel.add(offsetYPanel);
		fieldPanel.add(imgPanel);
		
		boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		JPanel bcPanel = new JPanel();
		bcPanel.setLayout(new GridLayout(1, 2));
		bcPanel.add(new JLabel("Border Color"));
		bcPanel.add(borderColorField = new JTextField(String.format("%06x", ec.borderColor.getRGB() & 0xFFFFFF)));
		boxPanel.add(bcPanel);
		JPanel fcPanel = new JPanel();
		fcPanel.setLayout(new GridLayout(1, 2));
		fcPanel.add(new JLabel("Fill Color"));
		fcPanel.add(fillColorField = new JTextField(String.format("%06x", ec.fillColor.getRGB() & 0xFFFFFF)));
		boxPanel.add(fcPanel);
		fieldPanel.add(boxPanel);
		
		visualTypeBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					if(visualTypeBox.getSelectedItem().equals(VisualType.Box)) {
						boxPanel.setVisible(true);
						imgPanel.setVisible(false);
					} else {
						boxPanel.setVisible(false);
						imgPanel.setVisible(true);
						
						offsetXPanel.setVisible(true);
						offsetYPanel.setVisible(true);
					}
				}
			}
		});
		
		visualTypeBox.setSelectedItem(ec.visualType);
		if(visualTypeBox.getSelectedItem().equals(VisualType.Box)) {
			boxPanel.setVisible(true);
			imgPanel.setVisible(false);
		} else {
			boxPanel.setVisible(false);
			imgPanel.setVisible(true);
			
			offsetXPanel.setVisible(true);
			offsetYPanel.setVisible(true);
		}
		
		setSize(450, 200);
		setLocationRelativeTo(null);
	}
	
	public void actionPerformed(ActionEvent e) {
		entityConfig.visualType = (VisualType)visualTypeBox.getSelectedItem();
		entityConfig.setImage(imgPathField.getText());
		entityConfig.imgOffsetX = Integer.parseInt(imgOffsetXField.getText());
		entityConfig.imgOffsetY = Integer.parseInt(imgOffsetYField.getText());
		entityConfig.borderColor = Color.decode("0x" + borderColorField.getText());
		entityConfig.fillColor = Color.decode("0x" + fillColorField.getText());
		try {
			File config = new File("config/entity/" + entityConfig.name + ".config");
			PrintWriter pw = new PrintWriter(config);
			pw.append(entityConfig.toString());
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		// Force everything to redraw to update visuals of this entity in other rooms
		Main.mapPanel.firstDraw = true;
		this.dispose();
	}
	
}
