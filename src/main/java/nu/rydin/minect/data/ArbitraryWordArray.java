package nu.rydin.minect.data;

public class ArbitraryWordArray {
    private int wordLength;
    private long[] data;
    private long mask;

    private ArbitraryWordArray(int wordLength) {
        if(wordLength > 32) {
            throw new IllegalArgumentException("Wordlength must be <= 32");
        }
        this.wordLength = wordLength;
        this.mask = (1 << wordLength) - 1;
    }

    public ArbitraryWordArray(int capacity, int wordLength) {
        this(wordLength);
        this.data = new long[capacity * wordLength / 64 + 1];
    }

    public ArbitraryWordArray(long[] data, int wordLength) {
        this(wordLength);
        this.data = data;
    }

    /**
     * Gets the value at an index
     * @param idx The index
     * @return The value
     */
    public int get(int idx) {
        idx *= this.wordLength;
        int slot = idx / 64;
        int bit = idx % 64;
        long h = (data[slot] >>> bit) & this.mask;
        int overflow = bit - (64 - this.wordLength);
        if(overflow > 0) {
            // Value stretches into next slot
            long mask = (1 << overflow) - 1;
            h |= (data[slot+1] & mask) << (this.wordLength - overflow);
        }
        return (int) (h & this.mask);
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
        idx *= this.wordLength;
        value &= this.mask;
        int slot = idx / 64;
        int bit = idx % 64;
        long h = (long) value << bit;
        data[slot] |= h;
        if(bit + wordLength > 64) {
            data[slot + 1] |= value >>> 64 - bit;
        }
    }
}
