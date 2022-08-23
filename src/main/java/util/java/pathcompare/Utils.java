package util.java.pathcompare;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.azagniotov.matcher.AntPathMatcherArrays;

public class Utils {

	static final AntPathMatcherArrays pathMatcher = new AntPathMatcherArrays.Builder().withPathSeparator('\\').build();

	private Utils() {
	}

	public static FileData getAsTree(List<FileData> data, String root, String filter, boolean differences,
			List<String> ignores) {
		FileData rootItem = new FileData("root", true);
		root = removeCount(root);
		for (FileData base : data) {
			if (exclude(base, ignores, filter, differences)) {
				continue;
			}
			FileData file = new FileData(base.getRelativePath(), base.getSize(), base.getAbsolutePath());
			file.setRoot(root);
			file.setSizes(base.getSizes());
			file.setExistance(base.getExistance());
			file.setAbsolutePaths(base.getAbsolutePaths());
			Path relativePath = base.getRelativePath();
			int nameCount = relativePath.getNameCount();
			FileData parent = rootItem;
			for (int i = 0; i < nameCount - 1; i++) {
				String name = relativePath.getName(i).toString();
				FileData child = parent.getChildByName(name);
				if (child == null) {
					child = new FileData(name, true);
					parent.addChild(child);
				}
				parent = child;
			}
			parent.addChild(file);
		}
		return rootItem;
	}

	private static boolean exclude(FileData base, List<String> ignores, String filter, boolean differences) {
		return (ignore(base.getRelativePath().toString(), ignores) || !matchFilter(base.getRelativeName(), filter)
				|| (differences && !hasDifferences(base)));
	}

	/**
	 * library used https://github.com/azagniotov/ant-style-path-matcher
	 * 
	 * @param relativeName
	 * @param ignores2
	 * @return
	 * @throws IOException
	 */
	private static boolean ignore(String relativeName, List<String> ignores) {
		return ignores.stream().anyMatch(i -> pathMatcher.isMatch(i, relativeName));
	}

	private static boolean hasDifferences(FileData base) {
		Map<String, Long> sizes = base.getSizes();
		if (!sizes.isEmpty()) {
			Set<Long> distinct = new HashSet<>(sizes.values());
			if (distinct.size() > 1) {
				return true;
			}
		}
		Map<String, Boolean> existance = base.getExistance();
		if (!existance.isEmpty()) {
			Set<Boolean> distinct = new HashSet<>(existance.values());
			if (distinct.size() > 1) {
				return true;
			}
		}
		return false;
	}

	public static String removeCount(String root) {
		if (root.lastIndexOf('(') > 0) {
			root = root.substring(0, root.lastIndexOf('(')).trim();
		}
		return root;
	}

	private static boolean matchFilter(String path, String filter) {
		// no filter applied
		if (filter == null || filter.trim().isEmpty()) {
			return true;
		}
		return path.contains(filter);
	}
}
