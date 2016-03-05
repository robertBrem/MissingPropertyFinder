package expert.optimist;

public class WrongPropertyEntry {
	private PropertyFile defaultProperty;
	private PropertyFile targetProperty;
	private String wrongKey;

	public WrongPropertyEntry(PropertyFile defaultProperty, PropertyFile targetProperty, String wrongKey) {
		super();
		this.defaultProperty = defaultProperty;
		this.targetProperty = targetProperty;
		this.wrongKey = wrongKey;
	}

	@Override
	public String toString() {
		return defaultProperty.getBaseFileName() + " : '" + defaultProperty.getProperties().getProperty(wrongKey) + "' "
				+ targetProperty.getLocale().toString() + " -> '" + targetProperty.getProperties().getProperty(wrongKey)
				+ "'";
	}

	public PropertyFile getDefaultProperty() {
		return defaultProperty;
	}

	public PropertyFile getTargetProperty() {
		return targetProperty;
	}

	public String getWrongKey() {
		return wrongKey;
	}

}
