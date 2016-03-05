package expert.optimist;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Filter {

	public List<File> finder(File file) {
		if (file.isDirectory()) {
			return Arrays.asList(file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".properties");
				}
			}));
		}

		List<File> found = new ArrayList<File>();
		if (file.getName().endsWith(".properties")) {
			found.add(file);
		}
		return found;
	}

	public List<File> finder(File file, boolean recursively, Set<File> foldersToIgnore) {
		if (foldersToIgnore.contains(file)) {
			return new ArrayList<File>();
		}
		if (!recursively) {
			return finder(file);
		}
		List<File> found = new ArrayList<File>();
		if (file.isDirectory()) {
			for (File current : file.listFiles()) {
				found.addAll(finder(current, true, foldersToIgnore));
			}
		} else {
			found.addAll(finder(file));
		}
		return found;
	}

}
