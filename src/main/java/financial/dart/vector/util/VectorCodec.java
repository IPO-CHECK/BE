package financial.dart.vector.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VectorCodec {

    private VectorCodec() {}

    public static byte[] toBytes(float[] vector) {
        ByteBuffer buf = ByteBuffer.allocate(4 * vector.length).order(ByteOrder.LITTLE_ENDIAN);
        for (float v : vector) buf.putFloat(v);
        return buf.array();
    }

    public static float[] fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int n = bytes.length / 4;
        float[] v = new float[n];
        for (int i = 0; i < n; i++) v[i] = buf.getFloat();
        return v;
    }

    public static double l2Norm(float[] vector) {
        double sum = 0.0;
        for (float v : vector) sum += (double)v * v;
        return Math.sqrt(sum);
    }
}