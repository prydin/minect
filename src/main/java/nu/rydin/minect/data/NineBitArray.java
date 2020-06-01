package nu.rydin.minect.data;

public class NineBitArray {
    private long[] data;

    public NineBitArray(int capacity) {
        this.data = new long[capacity * 9 / 64 + 1];
    }

    public int get(int idx) {
        idx *= 9;
        int slot = idx / 64;
        int bit = idx % 64;
        long h = (data[slot] >>> bit) & 0x1ff;
        int overflow = bit - (64 - 9);
        if(overflow > 0) {
            // Value stretches into next slot
            long mask = (1 << overflow) - 1;
            h |= (data[slot+1] & mask) << (9 - overflow);
        }
        return (int) (h & 0x1ff);
    }

    /**
     * Put a value into the array. IMPORTANT: This only supports one put per index. Subsequent puts
     * will not overwrite all bits, but will result in a logical OR of the old and new bits.
     * @param idx The index
     * @param value The value to put
     */
    public void put(int idx, int value) {
        if(value == 0) {
            return;
        }
        idx *= 9;
        value &= 0x1ff;
        int slot = idx / 64;
        int bit = idx % 64;
        long h = (long) value << bit;
        data[slot] |= h;
        int overflow = bit - (64 - 9);
        if(overflow > 0) {
            data[slot + 1] |= value >>> (9 - overflow);
        }
    }
}
