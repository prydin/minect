package nu.rydin.minect.data;

import net.querz.mca.Chunk;
import net.querz.mca.Section;
import nu.rydin.minect.BlockMapper;

public class ChunkWrapper {
    private long[] heightMap;

    private long[] dryHeightMap;

    private int[] biomes;

    private int x;

    private int z;

    private Chunk chunk;

    private BlockMapper cm;

    public ChunkWrapper(Chunk chunk, int x, int z, BlockMapper cm) {
        this.chunk = chunk;
        this.cm = cm;
        this.x = x;
        this.z = z;
        biomes = chunk.getBiomes();
        heightMap = chunk.getHeightMaps().getLongArray("WORLD_SURFACE");
        dryHeightMap = chunk.getHeightMaps().getLongArray("OCEAN_FLOOR");
    }

    public int getBiomeAt(int blockX, int blockY, int blockZ) {
        return chunk.getBiomeAt(blockX, blockY, blockZ);
    }

    public byte getBlockLightAt(int x, int y, int z) {
        Section s = chunk.getSection(y / 16);
        if(s == null) {
            return 0;
        }
        byte[] blockLights = s.getBlockLight();
        int idx = (x%16) + (z%16) * 8;
        if(idx > blockLights.length - 1) {
            return 0;
        }
        int l = blockLights[idx];
        if((idx & 1) != 0) {
            l >>>= 4;
        }
        return (byte) (l & 0x0f);
    }

    public byte getSurfaceBlockLight(int x, int z) {
        int y = getHeight(x, z, false);
        return getBlockLightAt(x, y, z);
    }

    public short getBlockIdAt(int x, int y, int z) {
        return cm.getIdForName(chunk.getBlockStateAt(x, y, z).getString("Name"));
    }

    public int getHeight(int x, int z, boolean dry) {
        return decodeHeightMapAt(dry ? dryHeightMap : heightMap, x, z);
    }

    public boolean isValid() {
        return heightMap.length > 0;
    }

    private int decodeHeightMapAt(long[] map, int x, int z) {
        if( map.length != 36) {
            System.err.println(x + " " + z + ": " + map.length);
        }
        int idx = ((x%16) + (z%16) * 16) * 9; // 16x16 chunk, 9 bits per entry
        int slot = idx / 64;
        int bit = idx % 64;
        long h = (map[slot] >>> bit) & 0x1ff;
        int overflow = bit - (64 - 9);
        if(overflow > 0) {
            // Value stretches into next slot
            long mask = (1 << overflow) - 1;
            h |= (map[slot+1] & mask) << (9 - overflow);
        }
        return ((int) (h & 0x1ff)) - 1;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
