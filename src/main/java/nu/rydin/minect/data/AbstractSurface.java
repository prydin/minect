package nu.rydin.minect.data;

public abstract class AbstractSurface {
    private short[][] blockIds;

    private byte[][] blockLight;

    protected AbstractSurface(ChunkWrapper chunk) {
        this.blockLight = new byte[8][16];
        for(int z = 0; z < 16; ++z) {
            for (int x = 0; x < 16; ++z) {
                int l = chunk.getBlockIdAt(x, this.getHeight(x, z), z) & 0x0f;
                blockLight[x / 2][z] |= (x & 1) != 0 ? l : l << 4;
            }
        }
    }

    public abstract short getHeight(int x, int z);
}
