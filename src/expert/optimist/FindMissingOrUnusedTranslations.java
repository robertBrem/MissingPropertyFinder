package expert.optimist;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FindMissingOrUnusedTranslations {

	public static void main(String[] args) {
		Filter filter = new Filter();

		String currentDirectory = getStartDirectory(args);
		System.out.println("It's checking " + currentDirectory + " and its subfolder.");

		Set<Locale> localesToContain = getLocalsToCheck(args);
		System.out.println("Locales to find are: " + localesToContain);

		Set<String> fileNamesToIgnore = getFileNamesToIgnore(args);
		System.out.println("Filenames to ignore are: " + fileNamesToIgnore);

		Set<File> foldersToIgnore = new HashSet<File>();
		if (args.length > 3) {
			String[] folderNames = args[3].split(",");
			for (String folderName : folderNames) {
				foldersToIgnore.add(new File(folderName));
			}
		}
		System.out.println("Folders to ignore are: " + foldersToIgnore);

		System.out.println();

		Map<File, List<File>> foldersWithProperties = findPropertyFiles(filter, currentDirectory, foldersToIgnore);
		Map<File, List<PropertyFile>> properties = convertToPropertyFile(foldersWithProperties);

		Set<WrongPropertyEntry> wrongProperties = new HashSet<WrongPropertyEntry>();
		Set<File> notFoundFileNames = new HashSet<File>();
		Set<File> checkedFiles = new HashSet<File>();
		for (File folder : properties.keySet()) {
			List<PropertyFile> propertyFiles = properties.get(folder);
			for (PropertyFile file : propertyFiles) {
				if (!file.isDefault()) {
					PropertyFile defaultProperty = null;
					for (PropertyFile current : propertyFiles) {
						if (current.isDefault() && file.getBaseFileName().equals(current.getBaseFileName())) {
							defaultProperty = current;
						}
					}
					if (defaultProperty == null) {
						notFoundFileNames.add(new File(
								file.getBasePath() + "/" + file.getBaseFileName() + PropertyFile.FILE_EXTENSION));
					} else {
						continue;
					}

				}
				if (fileNamesToIgnore.contains(file.getFile().getName())) {
					continue;
				}
				PropertyFile defaultProperty = file;
				String baseFileName = defaultProperty.getBaseFileName();
				for (Locale localeToContain : localesToContain) {
					PropertyFile targetLanguage = null;
					for (PropertyFile current : propertyFiles) {
						if (localeToContain.equals(current.getLocale())
								&& current.getBaseFileName().equals(baseFileName)) {
							targetLanguage = current;
						}
					}
					if (targetLanguage == null) {
						notFoundFileNames.add(new File(defaultProperty.getBasePath() + "/" + baseFileName + "_"
								+ localeToContain.toString() + PropertyFile.FILE_EXTENSION));
					} else {
						checkedFiles.add(defaultProperty.getFile());
						wrongProperties.addAll(getWrongPropertyEntries(defaultProperty, targetLanguage));
					}
				}
			}
		}

		System.out.println("Missing property files are: " + notFoundFileNames);
		System.out.println("Checked files are: " + checkedFiles);
		System.out.println("Wrong entries are: " + wrongProperties);

		for (WrongPropertyEntry entry : wrongProperties) {
			System.out.println(entry.getWrongKey() + ";"
					+ entry.getDefaultProperty().getProperties().getProperty(entry.getWrongKey()) + "' " + ";'"
					+ entry.getTargetProperty().getProperties().getProperty(entry.getWrongKey()) + "' ("
					+ entry.getTargetProperty().getLocale().toString() + ")");
		}
	}

	private static Set<WrongPropertyEntry> getWrongPropertyEntries(PropertyFile defaultProperty,
			PropertyFile targetProperty) {
		Set<WrongPropertyEntry> missingPropertyEntries = new HashSet<WrongPropertyEntry>();

		Properties defaultProperties = defaultProperty.getProperties();
		Properties targetProperties = targetProperty.getProperties();

		for (String defaultKey : defaultProperties.stringPropertyNames()) {
			if (targetProperties.containsKey(defaultKey)
					&& !defaultProperties.getProperty(defaultKey).equals(targetProperties.getProperty(defaultKey))) {
				continue;
			} else {
				missingPropertyEntries.add(new WrongPropertyEntry(defaultProperty, targetProperty, defaultKey));
			}
		}
		for (String targetKey : targetProperties.stringPropertyNames()) {
			if (defaultProperties.containsKey(targetKey)) {
				continue;
			} else {
				missingPropertyEntries.add(new WrongPropertyEntry(defaultProperty, targetProperty, targetKey));
			}
		}

		return missingPropertyEntries;
	}

	private static Set<String> getFileNamesToIgnore(String[] args) {
		Set<String> fileNamesToIgnore = new HashSet<String>();
		if (args.length > 2) {
			String[] fileNames = args[2].split(",");
			for (String fileName : fileNames) {
				fileNamesToIgnore.add(fileName + PropertyFile.FILE_EXTENSION);
			}
		}
		return fileNamesToIgnore;
	}

	private static String getStartDirectory(String[] args) {
		String currentDirectory = System.getProperty("user.dir");
		if (args.length > 0) {
			currentDirectory = args[0];
		}
		return currentDirectory;
	}

	private static Set<Locale> getLocalsToCheck(String[] args) {
		Set<Locale> localesToContain = new HashSet<Locale>();
		if (args.length > 1) {
			String[] locales = args[1].split(",");
			for (String locale : locales) {
				String[] localeParts = locale.split("_");
				if (localeParts.length == 1) {
					localesToContain.add(getLocale(localeParts[0]));
				} else if (localeParts.length == 2) {
					localesToContain.add(getLocale(localeParts[0], localeParts[1]));
				} else {
					throw new IllegalArgumentException(locale + " is not a correct Locale text.");
				}
			}
		}
		return localesToContain;
	}

	private static Map<File, List<PropertyFile>> convertToPropertyFile(Map<File, List<File>> foldersWithProperties) {
		Map<File, List<PropertyFile>> properties = new HashMap<File, List<PropertyFile>>();
		for (File folder : foldersWithProperties.keySet()) {
			List<File> propertyFiles = foldersWithProperties.get(folder);
			List<PropertyFile> files = new ArrayList<PropertyFile>();
			for (File propertyFile : propertyFiles) {
				Locale locale = getLocale(propertyFile);
				files.add(new PropertyFile(propertyFile, locale));
			}
			properties.put(folder, files);
		}
		return properties;
	}

	private static Map<File, List<File>> findPropertyFiles(Filter filter, String currentDirectory,
			Set<File> foldersToIgnore) {
		List<File> found = filter.finder(new File(currentDirectory), true, foldersToIgnore);
		Map<File, List<File>> foldersWithProperties = new HashMap<File, List<File>>();
		for (File file : found) {
			File parentFolder = file.getParentFile();
			if (foldersWithProperties.containsKey(parentFolder)) {
				continue;
			}
			foldersWithProperties.put(parentFolder, filter.finder(parentFolder));
		}
		return foldersWithProperties;
	}

	private static Locale getLocale(File file) {
		String name = file.getName();
		String[] nameParts = name.split("_");
		if (nameParts.length > 1) {
			String lastPart = nameParts[nameParts.length - 1].replaceAll(PropertyFile.FILE_EXTENSION, "");
			String secondLastPart = nameParts[nameParts.length - 2];
			return getLocale(lastPart, secondLastPart);
		}
		return null;
	}

	private static Locale getLocale(String lastPart, String secondLastPart) {
		if (lastPart.length() != 2) {
			throw new IllegalArgumentException(secondLastPart + "_" + lastPart + " is not a valid Locale extension!");
		}

		if (secondLastPart != null && secondLastPart.length() == 2) {
			return new Locale(secondLastPart, lastPart);
		} else {
			return new Locale(lastPart);
		}
	}

	private static Locale getLocale(String lastPart) {
		return getLocale(lastPart, null);
	}

}
