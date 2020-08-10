package celesteeditor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;

import github.MichaelBeeu.util.EndianDataInputStream;
import github.MichaelBeeu.util.EndianDataOutputStream;

public class BinaryPacker {
    public static class Element {
        public String Package;

        public String Name;

        public HashMap<String, Object> Attributes;

        public ArrayList<Element> Children;
        
        public Element() {
        	
        }
        
        public Element(String name) {
        	this();
        	Name = name;
        }

        public boolean HasAttr(String name) {
        	if(Attributes == null) {
        		return false;
        	}
        	
        	return Attributes.containsKey(name) || Attributes.containsKey(name.toLowerCase());
        }
        
        public String Attr(String name) {
            return Attr(name, "");
        }

        public String Attr(String name, String defaultValue) {
        	if (Attributes != null) {
                return Attributes.getOrDefault(name, 
                		Attributes.getOrDefault(name.toLowerCase(), defaultValue)).toString();
        	}
            return defaultValue;
        }
        
        public boolean AttrBool(String name) {
        	return AttrBool(name, false);
        }

        public boolean AttrBool(String name, boolean defaultValue) {
        	if (Attributes == null) {
        		return defaultValue;
        	}
        	
            Object value = Attributes.getOrDefault(name, 
                		Attributes.getOrDefault(name.toLowerCase(), defaultValue));
            if(value instanceof Boolean) {
            	return (boolean)value;
            }
            return Boolean.parseBoolean(value.toString());
        }
        
        public float AttrFloat(String name) {
        	return AttrFloat(name, 0);
        }

        public float AttrFloat(String name, float defaultValue) {
        	if (Attributes == null) {
        		return defaultValue;
        	}
        	
            Object value = Attributes.getOrDefault(name, 
                		Attributes.getOrDefault(name.toLowerCase(), defaultValue));
            if(value instanceof Float) {
            	return (float)value;
            }
            return Float.parseFloat(value.toString());
        }
        
        public int AttrInt(String name) {
        	return AttrInt(name, 0);
        }

        public int AttrInt(String name, int defaultValue) {
        	if (Attributes == null) {
        		return defaultValue;
        	}
        	
            Object value = Attributes.getOrDefault(name, 
                		Attributes.getOrDefault(name.toLowerCase(), defaultValue));
            if(value instanceof Integer) {
            	return (int)value;
            }
            return Integer.parseInt(value.toString());
        }
    }
    
    private static class AttributeValue {
    	public byte type;
    	public Object value;
    }
    
    public static String InnerTextAttributeName = "innerText";
    
    public static final HashSet<String> IgnoreAttributes = new HashSet<>(Arrays.asList("_eid"));

    public static String OutputFileExtension = ".bin";

    private static HashMap<String, Short> stringValue = new HashMap<>();

    private static String[] stringLookup;

    private static short stringCounter;
    
    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    
    private static DocumentBuilder docBuilder;
    
    static {
    	try {
    		docBuilder = docBuilderFactory.newDocumentBuilder();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
//    public static void toBinary(String filename, String outdir) throws SAXException, IOException {
//        String extension = FilenameUtils.getExtension(filename);
//        File outputFile;
//        if(outdir != null) {
//        	outputFile = new File(outdir, FilenameUtils.getName(filename.replaceAll(extension, OutputFileExtension)));
//        } else {
//        	outputFile = new File(filename.replaceAll(extension, OutputFileExtension));
//        }
//        Document xml = docBuilder.parse(filename);
//        toBinary(xml.getDocumentElement(), outputFile);
//    }

    public static void toBinary(Element rootElement, File outFile) {
        stringValue.clear();
        stringCounter = 0;
        createLookupTable(rootElement);
        addLookupValue(InnerTextAttributeName);
        try (EndianDataOutputStream outStream = new EndianDataOutputStream(new FileOutputStream(outFile)).order(ByteOrder.LITTLE_ENDIAN)) {
            writeString(outStream, "CELESTE MAP");
            writeString(outStream, FilenameUtils.removeExtension(outFile.getName()));
            outStream.writeShort((short)stringValue.size() & 0xffff);
            for (Entry<String, Short> item : stringValue.entrySet().stream().sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue())).collect(Collectors.toList())) {
                writeString(outStream, item.getKey());
            }
            writeElement(outStream, rootElement);
            outStream.flush();
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    private static void createLookupTable(Element element) {
        addLookupValue(element.Name);
        if(element.Attributes != null) {
	        for (Entry<String, Object> entry : element.Attributes.entrySet()) {
	        	System.out.println(entry.getKey());
	        	if(!IgnoreAttributes.contains(entry.getKey()) && !entry.getKey().equals(InnerTextAttributeName) && entry.getValue().toString().length() > 0) {
	                addLookupValue(entry.getKey());
	                AttributeValue val = getAttr(entry.getValue());
	                if (val != null && val.type == 5) {
	                    addLookupValue(val.value.toString());
	                }
	            }
	        }
        }
        if(element.Children != null) {
	        for (Element child : element.Children) {
	            createLookupTable(child);
	        }
        }
    }

    private static void addLookupValue(String name) {
        if (!stringValue.containsKey(name)) {
            stringValue.put(name, stringCounter);
            stringCounter++;
        }
    }

    private static void writeElement(EndianDataOutputStream outStream, Element element) throws IOException {
    	int numAttr = 0;
        if(element.Attributes != null) {
	        for(Entry<String, Object> entry : element.Attributes.entrySet()) {
	            if (!IgnoreAttributes.contains(entry.getKey()) && entry.getValue().toString().length() > 0) {
	                numAttr++;
	            }
	        }
        }
        outStream.writeShort(stringValue.get(element.Name));
        outStream.writeByte((byte)numAttr);
        if(element.Attributes != null) {
	        for (Entry<String, Object> entry : element.Attributes.entrySet()) {
	            if (!IgnoreAttributes.contains(entry.getKey()) && !entry.getKey().equals(InnerTextAttributeName) && entry.getValue().toString().length() > 0) {
	                AttributeValue val = getAttr(entry.getValue());
	                outStream.writeShort(stringValue.get(entry.getKey()));
	                outStream.writeByte(val.type);
	                switch (val.type) {
	                    case 0:
	                        outStream.writeBoolean((boolean)val.value);
	                        break;
	                    case 1:
	                    	outStream.write((byte)val.value);
	                        break;
	                    case 2:
	                    	outStream.writeShort((short)val.value);
	                        break;
	                    case 3:
	                    	outStream.writeInt((int)val.value);
	                        break;
	                    case 4:
	                    	outStream.writeFloat((float)val.value);
	                        break;
	                    case 5:
	                    	outStream.writeShort(stringValue.get((String)val.value));
	                        break;
	                }
	            }
	        }
	        if (element.Attr(InnerTextAttributeName).length() > 0) {
	            outStream.writeShort(stringValue.get(InnerTextAttributeName));
	            if (element.Name == "solids" || element.Name == "bg") {
	                byte[] array = RunLengthEncoding.encode(element.Attr(InnerTextAttributeName));
	                outStream.writeByte((byte)7);
	                outStream.writeShort((short)array.length);
	                outStream.write(array);
	            } else {
	                outStream.writeByte((byte)6);
	                writeString(outStream, element.Attr(InnerTextAttributeName));
	            }
	        }
        }
        
        if(element.Children != null) {
	        outStream.writeShort((short)element.Children.size());
	        for (int i = 0; i < element.Children.size(); i++) {
	            writeElement(outStream, element.Children.get(i));
	        }
        } else {
        	outStream.writeShort(0);
        }
    }
    
    private static AttributeValue getAttr(Object val) {
    	AttributeValue res = null;
        if (val instanceof Boolean) {
        	res = new AttributeValue();
        	res.type = 0;
            res.value = (Boolean) val;
        } else if(tryParseByte(val.toString()) && Byte.parseByte(val.toString()) >= 0) {
        	res = new AttributeValue();
            res.type = 1;
            res.value = Byte.parseByte(val.toString());
        } else if (tryParseShort(val.toString())) {
        	res = new AttributeValue();
            res.type = 2;
            res.value = Short.parseShort(val.toString());
        } else if (tryParseInt(val.toString())) {
        	res = new AttributeValue();
            res.type = 3;
            res.value = Integer.parseInt(val.toString());
        } else if (tryParseFloat(val.toString())) {
        	res = new AttributeValue();
            res.type = 4;
            res.value = Float.parseFloat(val.toString().trim());
        } else {
        	res = new AttributeValue();
            res.type = 5;
            res.value = val.toString();
        }
        return res;
    }

    private static AttributeValue parseValue(String value) {
    	AttributeValue res = null;
        if (value.equalsIgnoreCase(Boolean.TRUE.toString()) || value.equalsIgnoreCase(Boolean.FALSE.toString())) {
        	res = new AttributeValue();
        	res.type = 0;
            res.value = Boolean.parseBoolean(value);
        } else if(tryParseByte(value) && Byte.parseByte(value) >= 0) {
        	res = new AttributeValue();
            res.type = 1;
            res.value = Byte.parseByte(value);
        } else if (tryParseShort(value)) {
        	res = new AttributeValue();
            res.type = 2;
            res.value = Short.parseShort(value);
        } else if (tryParseInt(value)) {
        	res = new AttributeValue();
            res.type = 3;
            res.value = Integer.parseInt(value);
        } else if (tryParseFloat(value)) {
        	res = new AttributeValue();
            res.type = 4;
            res.value = Float.parseFloat(value.trim());
        } else {
        	res = new AttributeValue();
            res.type = 5;
            res.value = value;
        }
        return res;
    }
    
    public static boolean tryParseFloat(String value) {
    	try {
    		Float.parseFloat(value.trim());
    	} catch(NumberFormatException e) {
    		return false;
    	}
    	return true;
    }
    
    public static boolean tryParseInt(String value) {
    	try {
    		Integer.parseInt(value);
    	} catch(NumberFormatException e) {
    		return false;
    	}
    	return true;
    }
    
    public static boolean tryParseShort(String value) {
    	try {
    		Short.parseShort(value);
    	} catch(NumberFormatException e) {
    		return false;
    	}
    	return true;
    }
    
    public static boolean tryParseByte(String value) {
    	try {
    		Byte.parseByte(value);
    	} catch(NumberFormatException e) {
    		return false;
    	}
    	return true;
    }
    
    private static void write7BitEncodedInt(OutputStream stream, int val) throws IOException {
    	long num;
    	for (num = val & 0xFFFFFFFFL; num >= 128; num >>= 7)
    	{
    		stream.write((byte)(num | 0x80));
    	}
    	stream.write((byte)num);
    }
    
    private static void writeString(OutputStream stream, String str) throws IOException {
    	write7BitEncodedInt(stream, str.length());
    	for(int i = 0; i < str.length(); i++) {
    		stream.write(str.charAt(i));
    	}
    }
    
    private static int read7BitEncodedInt(InputStream stream) throws IOException {
    	int num = 0;
    	int num2 = 0;
    	byte b;
    	do {
    		if(num2 == 35) {
    			throw new IOException("Something is wrong with the string format. AAAA");
    		}
    		b = (byte)stream.read();
    		num |= (b & 0x7F) << num2;
    		num2 += 7;
    	} while((b & 0x80) != 0);
    	return num;
    }
    
    private static String readString(InputStream stream) throws IOException {
    	String res = "";
    	int len = read7BitEncodedInt(stream);
    	for(int i = 0; i < len; i++) {
    		res += (char)stream.read();
    	}
    	return res;
    }

    public static Element fromBinary(String filename) throws FileNotFoundException, IOException {
        try (@SuppressWarnings("resource")
		EndianDataInputStream reader = new EndianDataInputStream(new FileInputStream(filename)).order(ByteOrder.LITTLE_ENDIAN)) {
            readString(reader);
            String pkg = readString(reader);
            short num = reader.readShort();
            stringLookup = new String[num];
            for (int i = 0; i < num; i++) {
                stringLookup[i] = readString(reader);
            }
            Element element = readElement(reader);
            element.Package = pkg;
            return element;
        }
    }

    private static Element readElement(EndianDataInputStream reader) throws IOException {
        Element element = new Element();
        short lookup = reader.readShort();
        element.Name = stringLookup[lookup];
        byte b = reader.readByte();
        if (b > 0) {
            element.Attributes = new HashMap<String, Object>();
        }
        for (int i = 0; i < b; i++) {
        	lookup = reader.readShort();
            String key = stringLookup[lookup];
            byte type = reader.readByte();
            Object value = null;
            switch (type) {
                case 0:
                    value = reader.readBoolean();
                    break;
                case 1:
                    value = (int)reader.readByte() & 0xff;
                    break;
                case 2:
                    value = (int)reader.readShort();
                    break;
                case 3:
                    value = reader.readInt();
                    break;
                case 4:
                    value = reader.readFloat();
                    break;
                case 5:
                    value = stringLookup[reader.readShort()];
                    break;
                case 6:
                    value = readString(reader);
                    break;
                case 7: {
                        short count = reader.readShort();
                        byte[] val = new byte[count];
                        reader.read(val, 0, count);
                        value = RunLengthEncoding.decode(val);
                        break;
                    }
            }
            element.Attributes.put(key, value);
        }
        short num = reader.readShort();
        if (num > 0) {
            element.Children = new ArrayList<Element>();
        }
        for (int j = 0; j < num; j++) {
            element.Children.add(readElement(reader));
        }
        return element;
    }
}

