package util.java.pathcompare;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathCompare {
	private List<String> pathsStr;
	private List<String> folderNames;

	public PathCompare(List<String> pathsStr) {
		this.pathsStr = pathsStr;
		this.folderNames = new ArrayList<>();
	}

	public List<FileData> compare() throws IOException {
		Map<String, List<FileData>> foldersPaths = new LinkedHashMap<>();
		for (String pathStr : pathsStr) {
			Path path = Paths.get(pathStr);
			foldersPaths.put(path.getFileName().toString(), getFiles(path));
		}
		Set<FileData> base = new TreeSet<>();
		foldersPaths.values().forEach(base::addAll);
		extractFoldersNames(foldersPaths, base);
		return display(foldersPaths, base);
	}

	private List<FileData> display(Map<String, List<FileData>> foldersPaths, Set<FileData> base) {
		List<FileData> result = new ArrayList<>();
		base.forEach(b -> {
			foldersPaths.forEach((k, v) -> {
				Optional<FileData> item = v.stream().filter(p -> p.equals(b)).findFirst();
				if (item.isPresent()) {
					b.getExistance().put(k, true);
					b.getSizes().put(k, item.get().getSize());
					b.getAbsolutePaths().add(item.get().getAbsolutePath());
				} else {
					b.getExistance().put(k, false);
					b.getSizes().put(k, null);
				}
			});
			result.add(b);
		});
		return result;
	}

	private void extractFoldersNames(Map<String, List<FileData>> foldersPaths, Set<FileData> base) {
		folderNames.add(String.format("Base (%d)", base.size()));
		foldersPaths.keySet().forEach(k -> folderNames.add(String.format("%s (%d)", k, foldersPaths.get(k).size())));
	}

	private List<FileData> getFiles(Path path) throws IOException {
		try (Stream<Path> paths = Files.walk(path)) {
			return paths.filter(Files::isRegularFile).map(f -> new FileData(path.relativize(f), f.toFile().length(), f))
					.collect(Collectors.toList());
		}
	}

	public List<String> getFolderNames() {
		return folderNames;
	}

}
