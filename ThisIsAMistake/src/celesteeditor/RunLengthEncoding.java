package celesteeditor;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

public class RunLengthEncoding
{
	public static byte[] encode(String str)
	{
		ArrayList<Byte> list = new ArrayList<>();
		for (int i = 0; i < str.length(); i++)
		{
			byte b = 1;
			char c = str.charAt(i);
			while (i + 1 < str.length() && str.charAt(i + 1) == c && b < 255)
			{
				b++;
				i++;
			}
			list.add(b);
			list.add((byte)c);
		}
		return ArrayUtils.toPrimitive(list.toArray(new Byte[0]));
	}

	public static String decode(byte[] bytes) {
		String res = "";
		for (int i = 0; i < bytes.length; i += 2) {
			for(int j = 0; j < ((int)bytes[i] & 0xff); j++) {
				res += (char)bytes[i + 1];
			}
		}
		return res;
	}
}
