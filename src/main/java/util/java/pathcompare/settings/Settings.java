package util.java.pathcompare.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Settings {
	private static final Properties settingsProp = new Properties();
	private static final Properties ignoreProp = new Properties();

	private static final String IGNORE_FILE_PATH_KEY = "ignore.path";
	private static final String HOME_FILE_PATH_KEY = "home.path";
	private static final String IGNORE_ALL_KEY = "ignore.list";
	private static final String SETTINGS_FILE_NAME = "settings.properties";
	private static final String IGNORE_FILE_NAME = "ignore.properties";

	public Settings() throws IOException {
		loadSettings();
		loadIgnores();
	}

	public String getHomePath() {
		return settingsProp.getProperty(HOME_FILE_PATH_KEY);
	}

	public void loadSettings() throws IOException {
		if (!Files.exists(Paths.get(SETTINGS_FILE_NAME))) {
			Files.createFile(Paths.get(SETTINGS_FILE_NAME));
			setHomePath("");
		}

		try (InputStream in = new FileInputStream(SETTINGS_FILE_NAME)) {
			settingsProp.load(in);
		}
	}

	public void setHomePath(String path) throws IOException {
		settingsProp.setProperty(HOME_FILE_PATH_KEY, path);
		save(settingsProp, SETTINGS_FILE_NAME);
	}

	public String getIgnorePath() {
		return settingsProp.getProperty(IGNORE_FILE_PATH_KEY);
	}

	public void setIgnorePath(String path) throws IOException {
		settingsProp.setProperty(IGNORE_FILE_PATH_KEY, path);
		save(settingsProp, SETTINGS_FILE_NAME);
	}

	private void save(Properties properties, String fileName) throws IOException {
		try (final OutputStream outputstream = new FileOutputStream(fileName)) {
			properties.store(outputstream, "File Updated");
		}
	}

	public List<String> getIgnoreList() throws IOException {
		if (ignoreProp.entrySet().isEmpty()) {
			ignoreProp.put(IGNORE_ALL_KEY, "");
		}
		return ignoreProp.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList());
	}

	public void loadIgnores() throws IOException, FileNotFoundException {
		String ignorePath = getIgnorePath();
		if (null == ignorePath || ignorePath.trim().isEmpty() || !Files.exists(Paths.get(ignorePath))) {
			Path createFile = Files.createFile(Paths.get(IGNORE_FILE_NAME));
			setIgnorePath(createFile.toFile().getAbsolutePath());
			setIgnoreTarget(IGNORE_ALL_KEY, "");
		}

		try (InputStream in = new FileInputStream(getIgnorePath())) {
			ignoreProp.load(in);
		}
	}

	public void setIgnoreList(String text) throws IOException {
		if (null != text && !text.trim().isEmpty()) {
			List<String> lines = Arrays.asList(text.split("\n"));
			lines.forEach(l -> {
				String[] parts = l.split("=");
				if (parts.length == 2) {
					ignoreProp.setProperty(parts[0].trim(), parts[1].trim());
				} else if (parts.length == 1) {
					ignoreProp.setProperty(parts[0].trim(), "");
				}
			});
		} else {
			ignoreProp.clear();
			setIgnoreTarget(IGNORE_ALL_KEY, "");
		}
		save(ignoreProp, getIgnorePath());
	}

	public List<String> getIgnoreAllList() {
		return getIgnoreTargetList(IGNORE_ALL_KEY);
	}

	public List<String> getIgnoreTargetList(String target) {
		String ignoreAll = ignoreProp.getProperty(target);
		if (null != ignoreAll && !ignoreAll.trim().isEmpty()) {
			List<String> list = Arrays.asList(ignoreAll.split(","));
			return list.stream().map(String::trim).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public void setIgnoreTarget(String target, String text) throws IOException {
		if (text == null || text.trim().isEmpty()) {
			ignoreProp.remove(target);
		} else {
			ignoreProp.setProperty(target, text);
		}
		save(ignoreProp, getIgnorePath());
	}
}
