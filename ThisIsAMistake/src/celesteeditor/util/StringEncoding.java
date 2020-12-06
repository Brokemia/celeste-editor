package celesteeditor.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StringEncoding {
	public static void write7BitEncodedInt(OutputStream stream, int val) throws IOException {
    	long num;
    	for (num = val & 0xFFFFFFFFL; num >= 128; num >>= 7)
    	{
    		stream.write((byte)(num | 0x80));
    	}
    	stream.write((byte)num);
    }
    
    public static void writeString(OutputStream stream, String str) throws IOException {
    	write7BitEncodedInt(stream, str.length());
    	for(int i = 0; i < str.length(); i++) {
    		stream.write(str.charAt(i));
    	}
    }
    
    public static int read7BitEncodedInt(InputStream stream) throws IOException {
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
    
    public static String readString(InputStream stream) throws IOException {
    	String res = "";
    	int len = read7BitEncodedInt(stream);
    	for(int i = 0; i < len; i++) {
    		res += (char)stream.read();
    	}
    	return res;
    }
}
