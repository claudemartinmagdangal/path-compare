package util.java.pathcompare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import util.java.pathcompare.settings.Settings;
import util.java.pathcompare.settings.SettingsUI;
import util.java.pathcompare.ui.TreeView;

public class GUI extends JFrame {
	private JPanel topPanel;
	private JPanel resultPanel;
	private TreeView treeView;
	private DefaultListModel<CheckListItem> listModel;
	private JTextField searchText;
	private boolean difference;

	private Settings settings;

	public GUI() {
		super("Folder Compare");
		try {
			settings = new Settings();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Failed to Load Settings file",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		initMenuBar();
		initTopPanel();
		initTablePanel();

		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(resultPanel, BorderLayout.CENTER);

		this.getContentPane().add(mainPanel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		this.pack();
		this.setVisible(true);
	}

	private void initTablePanel() {
		resultPanel = new JPanel(new BorderLayout());
		resultPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Result"));
		treeView = new TreeView();
		JScrollPane sp = new JScrollPane(treeView);
		sp.setBorder(new EmptyBorder(2, 2, 2, 2));
		resultPanel.add(sp, BorderLayout.CENTER);
	}

	private void initTopPanel() {
		topPanel = new JPanel(new BorderLayout(10, 10));
		topPanel.add(createFileSelectionPanel(), BorderLayout.CENTER);
		topPanel.add(createFilterPanel(), BorderLayout.SOUTH);
	}

	public JPanel createSidePanel() {
		String homePath = settings.getHomePath();

		JFileChooser fileChooser = homePath == null ? new JFileChooser() : new JFileChooser(homePath);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);

		JPanel sidePanel = new JPanel(new GridLayout(0, 1, 5, 10));

		JButton addFolderBtn = new JButton("Add Folder(s)");
		addFolderBtn.addActionListener(e -> {
			int r = fileChooser.showOpenDialog(null);
			if (r == JFileChooser.APPROVE_OPTION) {
				File[] selectedFiles = fileChooser.getSelectedFiles();
				try {
					settings.setHomePath(fileChooser.getCurrentDirectory().getAbsolutePath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Arrays.asList(selectedFiles).stream()
						.forEach(s -> listModel.addElement(new CheckListItem(s.getAbsolutePath())));
			}
		});

		JButton clearListBtn = new JButton("Clear List");
		clearListBtn.addActionListener(e -> {
			listModel.clear();
			treeView.resetView();
			searchText.setText("");
		});

		sidePanel.add(addFolderBtn);
		sidePanel.add(clearListBtn);
		sidePanel.add(new JPanel());
		return sidePanel;
	}

	private JPanel createFileSelectionPanel() {
		JPanel fileSelectionPanel = new JPanel(new BorderLayout(10, 10));
		fileSelectionPanel.setBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "File Selection"));
		listModel = new DefaultListModel<>();
		JList<CheckListItem> foldersList = new JList<>(listModel);
		foldersList.setVisibleRowCount(4);
		foldersList.setCellRenderer(new CheckListRenderer());
		foldersList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				@SuppressWarnings("unchecked")
				JList<CheckListItem> list = (JList<CheckListItem>) event.getSource();
				int index = list.locationToIndex(event.getPoint());
				if (index > -1) {
					CheckListItem item = list.getModel().getElementAt(index);
					item.setSelected(!item.isSelected()); // Toggle selected state
					list.repaint(list.getCellBounds(index, index));// Repaint cell
				}
			}
		});
		fileSelectionPanel.add(new JScrollPane(foldersList), BorderLayout.CENTER);
		fileSelectionPanel.add(createSidePanel(), BorderLayout.EAST);
		return fileSelectionPanel;
	}

	private JPanel createFilterPanel() {
		JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
		filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Filter"));
		filterPanel.add(new JLabel("Search"), BorderLayout.WEST);
		searchText = new JTextField();
		filterPanel.add(searchText, BorderLayout.CENTER);
		filterPanel.add(createCompareButton(), BorderLayout.SOUTH);
		JCheckBox comp = new JCheckBox("Show Difference only");
		comp.addItemListener(e -> difference = e.getStateChange() == ItemEvent.SELECTED);
		filterPanel.add(comp, BorderLayout.EAST);
		return filterPanel;
	}

	private JPanel createCompareButton() {
		JButton compareBtn = new JButton("Compare");
		compareBtn.addActionListener(e -> {
			treeView.resetView();
			List<CheckListItem> checkListItems = Arrays.asList(listModel.toArray()).stream()
					.map(CheckListItem.class::cast).collect(Collectors.toList());

			List<String> folders = checkListItems.stream().filter(CheckListItem::isSelected)
					.map(CheckListItem::toString).collect(Collectors.toList());
			PathCompare pathCompare = new PathCompare(folders);
			try {
				List<FileData> data = pathCompare.compare();
				List<String> folderNames = pathCompare.getFolderNames();
				treeView.createView(folderNames, data, searchText.getText(), difference, settings);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Compare Failed", JOptionPane.ERROR_MESSAGE);
			}

		});
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(compareBtn);
		return btnPanel;
	}

	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Edit");
		menuBar.add(menu);
		JMenuItem settingsMenu = new JMenuItem("Settings");
		settingsMenu.addActionListener(a -> {
			JDialog d = new JDialog();
			d.setModal(true);
			d.add(new SettingsUI(settings));
			d.pack();
			d.setVisible(true);
		});
		menu.add(settingsMenu);
		this.setJMenuBar(menuBar);
	}
}

class CheckListRenderer extends JCheckBox implements ListCellRenderer<CheckListItem> {

	@Override
	public Component getListCellRendererComponent(JList<? extends CheckListItem> list, CheckListItem value, int index,
			boolean isSelected, boolean cellHasFocus) {
		setEnabled(list.isEnabled());
		setSelected(value.isSelected());
		setFont(list.getFont());
		setBackground(list.getBackground());
		setForeground(list.getForeground());
		setText(value.toString());
		return this;
	}
}

class CheckListItem {

	private String label;
	private boolean isSelected = true;

	public CheckListItem(String label) {
		this.label = label;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}
}
