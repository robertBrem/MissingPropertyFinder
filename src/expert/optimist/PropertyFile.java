package expert.optimist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class PropertyFile {
	public static final String FILE_EXTENSION = ".properties";

	private File file;
	private Locale locale;

	public PropertyFile(File file) {
		this.file = file;
	}

	public PropertyFile(File file, Locale locale) {
		this(file);
		this.locale = locale;
	}

	public Properties getProperties() {
		FileInputStream fis = getFileInputStream();
		return loadProperty(fis);
	}

	public Properties loadProperty(FileInputStream fis) {
		Properties prop = new Properties();
		try {
			prop.load(fis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return prop;
	}

	public FileInputStream getFileInputStream() {
		FileInputStream fis;
		try {
			fis = new FileInputStream(getFile().getAbsolutePath());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return fis;
	}

	public String getBasePath() {
		return file.getParentFile().getAbsolutePath();
	}

	public String getBaseFileName() {
		if (isDefault()) {
			return file.getName().replaceAll(FILE_EXTENSION, "");
		}
		String name = file.getName();
		String[] nameParts = name.split("_");
		return nameParts[0].replaceAll(FILE_EXTENSION, "");
	}

	@Override
	public String toString() {
		return "PropertyFile [file=" + file.getName() + ", locale=" + locale + ", defaultProperty=" + isDefault() + "]";
	}

	public File getFile() {
		return file;
	}

	public Boolean isDefault() {
		return locale == null;
	}

	public Locale getLocale() {
		return locale;
	}

}
