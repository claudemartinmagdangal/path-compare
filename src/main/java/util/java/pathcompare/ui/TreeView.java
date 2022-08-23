package util.java.pathcompare.ui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import util.java.pathcompare.FileData;
import util.java.pathcompare.settings.Settings;

public class TreeView extends JPanel implements TreeSelectionListener {

	private List<TreePanel> treePanels;

	public TreeView() {
		setLayout(new GridLayout(1, 0, 15, 10));
		treePanels = new ArrayList<>();
	}

	public void createView(List<String> roots, List<FileData> data, String filter, boolean differences,
			Settings settings) {
		TreePanel treePanel = new TreePanel(roots.get(0), data, filter, differences, settings);
		treePanel.addSelectionListener(this);
		this.add(treePanel);
		for (int i = 1; i < roots.size(); i++) {
			treePanel = new TreePanel(roots.get(i), data, filter, differences, settings);
			this.add(treePanel);
			treePanels.add(treePanel);
		}

		this.revalidate();
		this.repaint();
	}

	public void resetView() {
		treePanels.clear();
		this.removeAll();
		this.revalidate();
		this.repaint();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if (e.getNewLeadSelectionPath() != null) {
			int[] selectionRows = ((JTree) e.getSource()).getSelectionRows();
			treePanels.forEach(p -> p.setSelectedNode(selectionRows));
		}
	}
}
