package util.java.pathcompare.settings;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SettingsUI extends JPanel {

	private Settings settings;
	private JLabel ignorePathLabel;
	private JTextArea textArea;

	public SettingsUI(Settings settings) {
		this.settings = settings;
		this.setLayout(new BorderLayout(10, 10));

		JPanel top = new JPanel(new BorderLayout(10, 10));
		ignorePathLabel = new JLabel();
		top.add(ignorePathLabel, BorderLayout.CENTER);
		top.add(createChangeButton(), BorderLayout.EAST);

		this.add(top, BorderLayout.NORTH);

		textArea = new JTextArea(20, 80);
		this.add(new JScrollPane(textArea), BorderLayout.CENTER);
		JPanel bottom = new JPanel();
		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener(a -> {
			try {
				settings.setIgnorePath(ignorePathLabel.getText());
				settings.setIgnoreList(textArea.getText());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Failed to Save Settings",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			this.getTopLevelAncestor().setVisible(false);
		});
		bottom.add(saveBtn);
		JButton discardBtn = new JButton("Discard");
		discardBtn.addActionListener(a -> this.getTopLevelAncestor().setVisible(false));

		bottom.add(discardBtn);
		this.add(bottom, BorderLayout.SOUTH);
		loadData();
	}

	private void loadData() {
		try {
			List<String> lines = settings.getIgnoreList();
			updateTextArea(lines);
			ignorePathLabel.setText(settings.getIgnorePath());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Failed to load Ignore file",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private JButton createChangeButton() {
		JButton changeBtn = new JButton("Change");
		changeBtn.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int r = fileChooser.showOpenDialog(null);
			if (r == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				ignorePathLabel.setText(selectedFile.getAbsolutePath());
				try {
					List<String> lines = Files.readAllLines(Paths.get(selectedFile.getAbsolutePath()));
					updateTextArea(lines);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Failed to load Ignore file",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		});
		return changeBtn;
	}

	private void updateTextArea(List<String> lines) {
		textArea.setText("");
		lines.stream().forEach(l -> textArea.append(l + "\n"));
	}

}
