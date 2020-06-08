package nu.rydin.minect.data;

import java.util.Objects;
import net.querz.mca.Chunk;
import net.querz.mca.Section;
import nu.rydin.minect.BlockMapper;

public abstract class AbstractSurface {
    private ArbitraryWordArray blockIds = new ArbitraryWordArray(16 * 16, 9);

    private ArbitraryWordArray blockLight = new ArbitraryWordArray(16 * 16, 4);

    private short[][] biomes = new short[4][4];

    protected static class Key {
        private int x;

        private int z;

        public Key(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return x == key.x &&
                    z == key.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    protected AbstractSurface() {
    }

    protected void load(Chunk chunk, BlockMapper bm) {
        int airId = bm.getUndefinedId();
        for(int z = 0; z < 16; ++z) {
            for (int x = 0; x < 16; ++x) {
                int idx = 16 * z + x;
                int y = this.getHeight(x, z);

                // Handle blocklights
                Section s = chunk.getSection(y / 16); // TODO: Handle sparse chunks (missing sections)
                if(s != null) {
                    byte[] blockLights = s.getBlockLight();
                    if(blockLights != null) {
                        int i = (x % 16) + (z % 16) * 8;
                        if (i <= blockLights.length - 1) {
                            int l = blockLights[i];
                            if ((idx & 1) != 0) {
                                l >>>= 4;
                            }
                            blockLight.put(z * 16 + x, (l & 0x0f));
                        }
                    }
                }

                // Handle block ID
                int id = airId;
                try {
                    id = bm.getIdForName(chunk.getBlockStateAt(x, y, z).getString("Name"));
                } catch(NullPointerException e) {
                    // This means that we're above the world surface. This is a faster way of
                    // handling this than pulling out the height map.
                }
                blockIds.put(idx, id);

                // Handle biomes
                if(x % 4 == 0 && z % 4 == 0) {
                    biomes[x / 4][z / 4] = (short) chunk.getBiomeAt(x, y, z);
                }
            }
        }
    }

    public int getBlockLight(int x, int z) {
        return blockLight.get(z * 16 + x);
    }

    public int getBlockId(int x, int z) {
        return blockIds.get(z * 16 + x);
    }

    public int getBiome(int x, int z) {
        return biomes[x / 4][z / 4];
    }

    public abstract int getHeight(int x, int z);
}
