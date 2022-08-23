package util.java.pathcompare.ui;

import java.awt.BorderLayout;

import javax.swing.BoundedRangeModel;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class FilePanel extends JPanel {
	private JScrollPane scrollPane;

	public FilePanel(String title, String text) {
		super(new BorderLayout(5, 5));
		JLabel titleLbl = new JLabel("<html>"+title+"</html>");
		titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
		titleLbl.setVerticalAlignment(SwingConstants.CENTER);
		this.add(titleLbl, BorderLayout.NORTH);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setContentType("text/html");
		editorPane.setText("<html>"+text+"</html>");
		scrollPane = new JScrollPane(editorPane);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	public BoundedRangeModel getScrollPaneModel() {
		return scrollPane.getVerticalScrollBar().getModel();
	}

	public void setScrollPaneModel(BoundedRangeModel scrollPaneModel) {
		this.scrollPane.getVerticalScrollBar().setModel(scrollPaneModel);
	}
}