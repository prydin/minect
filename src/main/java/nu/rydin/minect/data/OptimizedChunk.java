package nu.rydin.minect.data;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.mca.Chunk;
import net.querz.nbt.mca.Section;
import nu.rydin.minect.ColorMapper;

import java.awt.Color;
import java.util.ArrayList;

public class OptimizedChunk {
    static class OptimizedSection {
        private byte[] blockLights;

        private short[][][] blockIds;

        public OptimizedSection(byte[] blockLights, short[][][] blockIds) {
            this.blockLights = blockLights;
            this.blockIds = blockIds;
        }
    }

    private OptimizedSection[] sections;

    private long[] heightMap;

    private long[] dryHeightMap;

    private int[] biomes;

    public OptimizedChunk(Chunk chunk, ColorMapper cm) {
        heightMap = chunk.getHeightMaps().getLongArray("WORLD_SURFACE");
        dryHeightMap = chunk.getHeightMaps().getLongArray("OCEAN_FLOOR");
        biomes = chunk.getBiomes();
        for(int i = 0; i < 16; ++i) {
            Section s = chunk.getSection(i);
            if(s == null) {
                break;
            }
            sections[i] = new OptimizedSection(s.getBlockLight(), loadBlockStates(s, cm));
        }
    }

    public int getBiomeAt(int x, int z) {
        return biomes[z << 4 + x];
    }

    public byte getBlockLightAt(int x, int y, int z) {
        OptimizedSection s = sections[y >> 4];
        if(s == null) {
            return 0;
        }
        int idx = x + z << 4;
        int l = s.blockLights[idx >> 1];
        if((idx & 1) != 0) {
            l >>= 4;
        }
        return (byte) (l & 0x0f);
    }

    public byte getSurfaceBlockLight(int x, int z) {
        int y = getHeight(x, z, false);
        return getBlockLightAt(x, y, z);
    }

    public short[][][] loadBlockStates(Section s, ColorMapper cm) {
        short[][][] blockIds = new short[16][16][16];
        for(int x = 0; x < 16; ++x) {
            for(int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    CompoundTag bs = s.getBlockStateAt(x, y, z);
                    String name = bs.getString("Name");
                    short id = cm.getIdForName(name);
                    blockIds[x][y][z] = id;
                }
            }
        }
        return blockIds;
    }

    public short getBlockIdAt(int x, int y, int z) {
        return sections[y >> 4].blockIds[x][y & 15][z];
    }

    public int getHeight(int x, int z, boolean dry) {
        return decodeHeightMapAt(dry ? dryHeightMap :heightMap, x, z);
    }

    private int decodeHeightMapAt(long[] map, int x, int z) {
        int idx = x + z * 16 * 9; // 16x16 chunk, 9 bits per entry
        int slot = idx / 64;
        int bit = idx % 64;
        long h = map[slot] >> bit;
        if(bit > 61) { // 61 because 64 - 9 = 61
            // Value stretches into next slot
            long mask = (1 << (62 - bit)) - 1; // Produce 61 - bit set bits.
            h |= map[slot+1] & mask;
        }
        return (int) (h * 0x1f);
    }
}
