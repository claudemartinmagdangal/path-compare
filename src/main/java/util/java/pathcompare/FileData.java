package util.java.pathcompare;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileData implements Comparable<FileData> {
	private String relativeName;
	private Path relativePath;
	private String fileName;
	private long size;
	private Path absolutePath;
	private List<FileData> children;
	private List<Path> absolutePaths = new ArrayList<>();
	private String comment;
	private boolean folder;
	private String root;
	private Map<String, Long> sizes = new HashMap<>();
	private Map<String, Boolean> existance = new HashMap<>();

	public FileData() {
		children = new ArrayList<>();
	}

	public FileData(Path relativePath, long size, Path absolutePath) {
		this.relativeName = relativePath.toString();
		this.setRelativePath(relativePath);
		this.size = size;
		this.absolutePath = absolutePath;
		this.folder = false;
		this.fileName = relativePath.getFileName().toString();
	}

	public FileData(String fileName, boolean folder) {
		this();
		this.fileName = fileName;
		this.folder = folder;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Path getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(Path absolutePath) {
		this.absolutePath = absolutePath;
	}

	public String getRelativeName() {
		return relativeName;
	}

	public void setRelativeName(String relativeName) {
		this.relativeName = relativeName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<FileData> getChildren() {
		return children;
	}

	public void setChildren(List<FileData> children) {
		this.children = children;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isFolder() {
		return folder;
	}

	public void setFolder(boolean folder) {
		this.folder = folder;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public Map<String, Long> getSizes() {
		return sizes;
	}

	public void setSizes(Map<String, Long> sizes) {
		this.sizes = sizes;
	}

	public Map<String, Boolean> getExistance() {
		return existance;
	}

	public void setExistance(Map<String, Boolean> existance) {
		this.existance = existance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((relativeName == null) ? 0 : relativeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileData other = (FileData) obj;
		if (relativeName == null) {
			if (other.relativeName != null)
				return false;
		} else if (!relativeName.equals(other.relativeName))
			return false;
		return true;
	}

	@Override
	public int compareTo(FileData o) {
		return this.getRelativeName().compareTo(o.getRelativeName());
	}

	public Path getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(Path relativePath) {
		this.relativePath = relativePath;
	}

	public FileData getChildByName(String name) {
		if (folder) {
			return children.stream().filter(i -> i.fileName.equals(name)).findFirst().orElse(null);
		}
		return null;
	}

	public void addChild(FileData child) {
		if (folder)
			children.add(child);
	}

	@Override
	public String toString() {
		return fileName;
	}

	public List<Path> getAbsolutePaths() {
		return absolutePaths;
	}

	public void setAbsolutePaths(List<Path> absolutePaths) {
		this.absolutePaths = absolutePaths;
	}
}
