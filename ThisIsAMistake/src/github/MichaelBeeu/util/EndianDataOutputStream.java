package github.MichaelBeeu.util;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Simple class to add endian support to DataOutputStream.
 * Adapted from EndianDataInputStream
 */
public class EndianDataOutputStream extends OutputStream implements DataOutput {
    DataOutputStream dataOut;
    private ByteBuffer buffer = ByteBuffer.allocate(8);
    ByteOrder order = ByteOrder.BIG_ENDIAN;

    public EndianDataOutputStream(OutputStream stream){
        dataOut = new DataOutputStream(stream);
    }

    public EndianDataOutputStream order(ByteOrder o){
        order = o;
        return this;
    }
    
	@Override
	public void writeBoolean(boolean v) throws IOException {
		dataOut.writeBoolean(v);		
	}

	@Override
	public void writeByte(int v) throws IOException {
		dataOut.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		buffer.clear();
        buffer.order(ByteOrder.BIG_ENDIAN)
                .putShort((short)v)
                .flip();
        dataOut.writeShort(buffer.order(order).getShort());
	}

	@Override
	public void writeChar(int v) throws IOException {
		dataOut.writeChar(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		buffer.clear();
        buffer.order(ByteOrder.BIG_ENDIAN)
                .putInt(v)
                .flip();
        dataOut.writeInt(buffer.order(order).getInt());
	}

	@Override
	public void writeLong(long v) throws IOException {
		buffer.clear();
        buffer.order(ByteOrder.BIG_ENDIAN)
                .putLong(v)
                .flip();
        dataOut.writeLong(buffer.order(order).getLong());
	}

	@Override
	public void writeFloat(float v) throws IOException {
        dataOut.writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(double v) throws IOException {
		dataOut.writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeBytes(String s) throws IOException {
		buffer.clear();
		buffer.order(ByteOrder.BIG_ENDIAN);
		int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            buffer.put((byte)s.charAt(i));
        }
        buffer.flip().order(order);
        byte[] res = new byte[len];
        buffer.get(res);
        
        dataOut.write(res);
	}

	@Override
	public void writeChars(String s) throws IOException {
		buffer.clear();
		buffer.order(ByteOrder.BIG_ENDIAN);
		int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
            buffer.put((byte)((v >>> 8) & 0xFF));
            buffer.put((byte)((v >>> 0) & 0xFF));
        }
        buffer.flip().order(order);
        byte[] res = new byte[len];
        buffer.get(res);
        
        dataOut.write(res);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		dataOut.writeUTF(s);
	}

	@Override
	public void write(int b) throws IOException {
		dataOut.write(b);
	}
}