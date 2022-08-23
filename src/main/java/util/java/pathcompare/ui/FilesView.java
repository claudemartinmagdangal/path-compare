package util.java.pathcompare.ui;

import java.awt.GridLayout;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;

import com.rtfparserkit.converter.text.StringTextConverter;
import com.rtfparserkit.parser.RtfStreamSource;

public class FilesView extends JPanel {

	private List<FilePanel> filePanels;
	private BoundedRangeModel sharedScrollModel;

	public FilesView() {
		setLayout(new GridLayout(1, 0, 15, 10));
		filePanels = new ArrayList<>();
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	public void createView(List<Path> absolutePaths) throws IOException {
		List<String> baseLines = null;
		for (Path path : absolutePaths) {
			if (baseLines == null) {
				baseLines = readFile(path);
			}
			List<String> lines = compareLines(baseLines, readFile(path));
			String text = String.join("<br>" + System.lineSeparator(), lines);
			FilePanel filePanel = new FilePanel(path.toString(), text);
			filePanels.add(filePanel);
			if (sharedScrollModel == null) {
				sharedScrollModel = filePanel.getScrollPaneModel();
			}
			filePanel.setScrollPaneModel(sharedScrollModel);
			this.add(filePanel);
		}

		this.revalidate();
		this.repaint();
	}

	private List<String> readFile(Path path) throws IOException {
		if (path.toString().toLowerCase().endsWith("rtf")) {
			InputStream is = new FileInputStream(path.toFile());
			StringTextConverter converter = new StringTextConverter();
			converter.convert(new RtfStreamSource(is));
			return Arrays.asList(converter.getText().split("\n"));
		}
		return Files.readAllLines(path);
	}

	private List<String> compareLines(List<String> baseLines, List<String> lines) {
		List<String> newLines = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			if (i < baseLines.size() && !lines.get(i).equals(baseLines.get(i))) {
				newLines.add(String.format("<span style='background-color: #eec5b8;'>%s</span>", lines.get(i)));
			} else {
				newLines.add(lines.get(i));
			}
		}
		return newLines;
	}

}
