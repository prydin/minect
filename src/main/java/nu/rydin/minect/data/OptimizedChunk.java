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

    private OptimizedSection[] sections = new OptimizedSection[16];

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
        return biomes[z * 16 + x];
    }

    public byte getBlockLightAt(int x, int y, int z) {
        OptimizedSection s = sections[y / 16];
        if(s == null) {
            return 0;
        }
        int idx = (x%16) + (z%16) * 8;
        if(idx > s.blockLights.length - 1) {
            return 0;
        }
        int l = s.blockLights[idx];
        if((idx & 1) != 0) {
            l >>>= 4;
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
        int yIndex = y/16;
        if(sections[yIndex] == null) {
            return 0;
        }
        return sections[yIndex].blockIds[x%16][y%16][z%16];
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

        // 0001 1100

        if(overflow > 0) {
            // Value stretches into next slot
            long mask = (1 << overflow) - 1;
            h |= (map[slot+1] & mask) << (9 - overflow);
        }
       // System.err.println(x + " " + z + ": " + h);
        return ((int) (h & 0x1ff)) - 1;
    }
}
