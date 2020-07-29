package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import celesteeditor.data.Decal;

public class DecalPropertyPopup extends JFrame {
	
	public static final int width = 450;
	
	public static DecalPropertyPopup currentPopup;
	
	public Decal decal;
	
	public JTextField xField, yField, scaleXField, scaleYField, textureField;
	
	public DecalPropertyPopup(Decal d) {
		super(d.getTexturePath() + " Properties");
		if(currentPopup != null) {
			currentPopup.dispose();
		}
		if(EntityPropertyPopup.currentPopup != null) {
			EntityPropertyPopup.currentPopup.dispose();
		}
		currentPopup = this;
		decal = d;
		
		JPanel fieldPanel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(fieldPanel);
		add(scrollPane);
		JButton submitButton = new JButton("Update");
		submitButton.addActionListener(this::actionPerformed);
		add(submitButton, BorderLayout.SOUTH);
		
		BoxLayout layout = new BoxLayout(fieldPanel, BoxLayout.Y_AXIS);
		fieldPanel.setLayout(layout);
		fieldPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel staticPanel = new JPanel();
		staticPanel.setLayout(new GridLayout(2, 4));
		staticPanel.add(new JLabel("X"));
		staticPanel.add(xField = new JTextField(d.x + "", 4));
		staticPanel.add(new JLabel("Y"));
		staticPanel.add(yField = new JTextField(d.y + "", 4));
		
		staticPanel.add(new JLabel("Scale X"));
		staticPanel.add(scaleXField = new JTextField(d.scaleX + "", 4));
		staticPanel.add(new JLabel("Scale Y"));
		staticPanel.add(scaleYField = new JTextField(d.scaleY + "", 4));
		fieldPanel.add(staticPanel);
		
		JPanel texturePanel = new JPanel();
		texturePanel.setLayout(new GridLayout(1, 2));
		texturePanel.add(new JLabel("Texture"));
		textureField = new JTextField(d.getTexturePath());
		texturePanel.add(textureField);
		fieldPanel.add(texturePanel);
		
		setSize(width, 200);
		setLocationRelativeTo(null);
	}
	
	public void actionPerformed(ActionEvent e) {
		ArrayList<String> invalid = new ArrayList<>();
		
		try {
			decal.x = Integer.parseInt(xField.getText());
		} catch(NumberFormatException ex) {
			invalid.add("x");
		}
		try {
			decal.y = Integer.parseInt(yField.getText());
		} catch(NumberFormatException ex) {
			invalid.add("y");
		}
		try {
			decal.scaleX = Integer.parseInt(scaleXField.getText());
		} catch(NumberFormatException ex) {
			invalid.add("scaleX");
		}
		try {
			decal.scaleY = Integer.parseInt(scaleYField.getText());
		} catch(NumberFormatException ex) {
			invalid.add("scaleY");
		}
		
		decal.setTexture(textureField.getText());
		
		if(invalid.size() != 0) {
			String msg = "Invalid value" + (invalid.size() == 1 ? "" : "s") + " provided for: ";
			for(int i = 0; i < invalid.size(); i++) {
				msg += invalid.get(i);
				if(i != invalid.size() - 1) {
					msg += ", ";
				}
			}
			JOptionPane.showMessageDialog(this, msg);
		}
		
		this.dispose();
	}
	
}
