package celesteeditor.data;

public class EntityProperty {
	
	public enum PropertyType {
		String, Integer, Float, Boolean
	}
	
	public String name;
	
	public PropertyType type = PropertyType.String;
	
	public Object value;
	
	public EntityProperty() {}
	
	public EntityProperty(String name, PropertyType type, Object val) {
		this.name = name;
		this.type = type;
		value = val;
	}
	
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
