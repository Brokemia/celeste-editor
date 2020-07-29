package celesteeditor.util;

public class EntityProperty {
	
	public enum PropertyType {
		String, Integer, Float, Boolean
	}
	
	public String name;
	
	public PropertyType type = PropertyType.String;
	
	public Object value;
	
	public static Object convertFromString(String val, PropertyType type) {
		switch(type) {
		case Integer:
			return Integer.parseInt(val);
		case Float:
			return Float.parseFloat(val);
		case Boolean:
			return Boolean.parseBoolean(val);
		default:
			return val;
		}
	}
	
}
