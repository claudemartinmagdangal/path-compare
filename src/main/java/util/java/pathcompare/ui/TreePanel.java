package util.java.pathcompare.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import util.java.pathcompare.FileData;
import util.java.pathcompare.Utils;
import util.java.pathcompare.settings.Settings;

public class TreePanel extends JPanel {

	private JTree tree;
	// private static BoundedRangeModel scrollModel;
	private Settings settings;
	private String title;
	private List<FileData> data;
	private String filter;
	private boolean differences;

	public TreePanel(String title, List<FileData> data, String filter, boolean differences, Settings settings) {
		super(new BorderLayout(5, 5));
		this.add(topPanel(title), BorderLayout.NORTH);
		this.settings = settings;
		this.title = title;
		this.data = data;
		this.differences = differences;
		this.filter = filter;

		List<String> ignores = new ArrayList<>();
		ignores.addAll(settings.getIgnoreAllList());
		ignores.addAll(settings.getIgnoreTargetList(Utils.removeCount(title)));

		generateTree(ignores);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
					if (node == null)
						return;
					Object nodeInfo = node.getUserObject();
					FileData fileData = (FileData) nodeInfo;
					List<Path> absolutePaths = fileData.getAbsolutePaths();
					if (absolutePaths != null && !absolutePaths.isEmpty()) {
						FilesView filesView = new FilesView();
						try {
							JDialog d = new JDialog();
							d.setModal(true);
							filesView.createView(absolutePaths);
							d.add(filesView);
							d.pack();
							d.setVisible(true);
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "Can't Open File", "Can't Open File",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
	}

	public void generateTree(List<String> ignores) {
		FileData asTree = Utils.getAsTree(data, title, filter, differences, ignores);
		createTree(title, asTree);
	}

	private JPanel topPanel(String title) {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		JToggleButton titleBtn = new JToggleButton(title);
		panel.add(titleBtn, BorderLayout.NORTH);
		JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 10));
		JTextArea txt = new JTextArea(5, 5);
		JButton apply = new JButton("Apply");
		apply.addActionListener(a -> {
			String text = txt.getText();
			if (text == null || text.trim().isEmpty()) {
				text = "";
			}
			List<String> ignores = new ArrayList<>();
			ignores.addAll(settings.getIgnoreAllList());
			ignores.addAll(Arrays.asList(text.split(",")).stream().map(String::trim).collect(Collectors.toList()));
			panel.remove(txt);
			panel.remove(buttons);
			titleBtn.doClick();
			panel.revalidate();
			panel.repaint();
			Arrays.asList(this.getComponents()).forEach(c -> {
				if (c instanceof JScrollPane)
					this.remove(c);
			});

			generateTree(ignores);
			this.revalidate();
			this.repaint();
		});
		JButton overwrite = new JButton("Overwrite");
		overwrite.addActionListener(a -> {
			String text = txt.getText();
			if (text == null || text.trim().isEmpty()) {
				text = "";
			}
			List<String> list = Arrays.asList(text.split(",")).stream().map(String::trim).collect(Collectors.toList());
			try {
				settings.setIgnoreTarget(Utils.removeCount(title), String.join(",", list));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Failed to Save Ignore List",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			List<String> ignores = new ArrayList<>();
			ignores.addAll(settings.getIgnoreAllList());
			ignores.addAll(list);
			panel.remove(txt);
			panel.remove(buttons);
			titleBtn.doClick();
			panel.revalidate();
			panel.repaint();
			Arrays.asList(this.getComponents()).forEach(c -> {
				if (c instanceof JScrollPane)
					this.remove(c);
			});

			generateTree(ignores);
			this.revalidate();
			this.repaint();
		});

		buttons.add(overwrite);
		buttons.add(apply);

		titleBtn.addItemListener(i -> {
			int stateChange = i.getStateChange();
			if (ItemEvent.SELECTED == stateChange) {
				List<String> ignoreTargetList = settings.getIgnoreTargetList(Utils.removeCount(title));
				txt.setText("");
				ignoreTargetList.stream().forEach(l -> txt.append(l + ","));
				panel.add(txt, BorderLayout.CENTER);
				panel.add(buttons, BorderLayout.SOUTH);
			} else if (ItemEvent.DESELECTED == stateChange) {
				panel.remove(txt);
				panel.remove(buttons);
			}
			panel.revalidate();
			panel.repaint();
		});

		return panel;
	}

	public void addSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
	}

	public void setSelectedNode(TreePath path) {
		if (path != null) {
			TreePath selectedPath = new TreePath(tree.getModel().getRoot());
			tree.setSelectionPath(selectedPath);
			tree.setExpandsSelectedPaths(true);
			tree.scrollPathToVisible(selectedPath);
			tree.makeVisible(selectedPath);
		}
	}

	public void setSelectedNode(int[] selectionRows) {
		if (selectionRows != null) {
			tree.setSelectionRows(selectionRows);
			TreePath selectedPath = tree.getSelectionPath();
			tree.setExpandsSelectedPaths(true);
			tree.scrollPathToVisible(selectedPath);
			tree.makeVisible(selectedPath);
		}
	}

	private void createTree(String root, FileData asTree) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileData(root, true));
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

		tree = new JTree(treeModel);
		ToolTipManager.sharedInstance().registerComponent(tree);

		JScrollPane scrollPane = new JScrollPane(tree);

		this.add(scrollPane, BorderLayout.CENTER);

		if (root.startsWith("Base")) {
			tree.setCellRenderer(new BaseCellRenderer());
			// scrollModel = scrollPane.getVerticalScrollBar().getModel();
		} else {
			tree.setCellRenderer(new CellRenderer());
			// scrollPane.getVerticalScrollBar().setModel(scrollModel);
		}
		tree.setShowsRootHandles(true);

		createTreeNodes(asTree, rootNode);
		treeModel.reload();
		expandAllNodes(0, tree.getRowCount());
	}

	public void createTreeNodes(FileData asTree, DefaultMutableTreeNode parent) {
		if (asTree.isFolder()) {
			for (FileData child : asTree.getChildren()) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
				parent.add(childNode);
				if (child.isFolder()) {
					createTreeNodes(child, childNode);
				}
			}
		}
	}

	private void expandAllNodes(int startingIndex, int rowCount) {
		for (int i = startingIndex; i < rowCount; ++i) {
			tree.expandRow(i);
		}

		if (tree.getRowCount() != rowCount) {
			expandAllNodes(rowCount, tree.getRowCount());
		}
	}

	class CellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			JLabel component = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
					hasFocus);
			Font oldFont = component.getFont();
			Color oldForeground = component.getForeground();
			FileData fileData = (FileData) ((DefaultMutableTreeNode) value).getUserObject();
			validateNode(fileData);
			@SuppressWarnings("unchecked")
			Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>) oldFont.getAttributes();

			if (!fileData.isFolder() && fileData.getComment() != null) {
				if (fileData.getComment().startsWith("<html>Not Found")) {
					renderMissingFile(component, attributes);
				} else {
					renderDifferentSize(component, attributes);
				}
				component.setToolTipText(fileData.getComment());
			} else if (!fileData.isFolder() && fileData.getComment() == null) {
				renderMatchedFile(component, oldForeground, fileData, attributes);
			} else {
				renderFolder(component, oldForeground, attributes);
			}
			return component;
		}

		private void renderDifferentSize(JLabel component, Map<TextAttribute, Object> attributes) {
			attributes.remove(TextAttribute.STRIKETHROUGH);
			Font newFont = new Font(attributes);
			component.setFont(newFont);
			component.setForeground(Color.ORANGE);
		}

		private void renderMissingFile(JLabel component, Map<TextAttribute, Object> attributes) {
			attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			Font newFont = new Font(attributes);
			component.setFont(newFont);
			component.setForeground(Color.RED);
		}

		private void renderMatchedFile(JLabel component, Color oldForeground, FileData fileData,
				Map<TextAttribute, Object> attributes) {
			attributes.remove(TextAttribute.STRIKETHROUGH);
			Font newFont = new Font(attributes);
			component.setFont(newFont);
			component.setForeground(oldForeground);
			component.setToolTipText("Size: " + fileData.getSize());
		}

		private void renderFolder(JLabel component, Color oldForeground, Map<TextAttribute, Object> attributes) {
			attributes.remove(TextAttribute.STRIKETHROUGH);
			Font newFont = new Font(attributes);
			component.setFont(newFont);
			component.setForeground(oldForeground);
			component.setToolTipText(null);
		}

		protected void validateNode(FileData fileData) {
			String comment = null;
			comment = validateMissingFile(fileData, comment);
			if (null == comment) {
				comment = validateDifferentSizes(fileData, comment);
			}
			fileData.setComment(comment);
		}

		protected String validateMissingFile(FileData fileData, String comment) {
			Boolean exists = fileData.getExistance().get(fileData.getRoot());
			if (exists != null && !exists) {
				comment = "<html>Not Found<br> Found in:<ul> <li>";
				List<String> found = new ArrayList<>();
				fileData.getExistance().forEach((k, v) -> {
					if (Boolean.TRUE.equals(v)) {
						found.add(k);
					}
				});
				comment += String.join("<li>", found) + "</ul></html>";
			}
			return comment;
		}

		protected String validateDifferentSizes(FileData fileData, String comment) {
			Long size = fileData.getSizes().get(fileData.getRoot());

			if (size != null) {
				fileData.setSize(size);

				List<String> diffSizes = new ArrayList<>();
				fileData.getSizes().forEach((k, v) -> {
					if (v != null && !size.equals(v)) {
						diffSizes.add(String.format("%s: %d", k, v));
					}
				});
				if (!diffSizes.isEmpty()) {
					comment = String.format("<html>Size: %d <br>Different Sizes:<ul><li>%s<ul></html>", size,
							String.join("<li>", diffSizes));
				}
			}
			return comment;
		}
	}

	class BaseCellRenderer extends CellRenderer {

		@Override
		protected String validateMissingFile(FileData fileData, String comment) {
			List<String> exists = new ArrayList<>();
			List<String> notExists = new ArrayList<>();
			fileData.getExistance().forEach((k, v) -> {
				if (Boolean.TRUE.equals(v))
					exists.add(k);
				else
					notExists.add(k);
			});

			if (!notExists.isEmpty()) {
				comment = String.format("<html>Not Found in:<br><ul><li>%s</ul>Found in:<ul><li>%s</ul></html>",
						String.join("<li>", notExists), String.join("<li>", exists));
			}
			return comment;
		}

		@Override
		protected String validateDifferentSizes(FileData fileData, String comment) {
			Collection<Long> sizes = fileData.getSizes().values();
			Set<Long> distinct = new HashSet<>(sizes);
			if (distinct.size() > 1) {
				List<String> diffSizes = new ArrayList<>();
				fileData.getSizes().forEach((k, v) -> diffSizes.add(String.format("%s: %d", k, v)));
				comment = String.format("<html>Different Sizes:<ul><li>%s<ul></html>", String.join("<li>", diffSizes));
			}
			return comment;
		}

	}
}
