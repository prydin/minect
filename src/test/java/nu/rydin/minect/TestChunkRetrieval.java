package nu.rydin.minect;

import net.querz.mca.Chunk;
import net.querz.mca.Section;
import nu.rydin.minect.data.DataManager;

import static org.junit.Assert.assertEquals;

import nu.rydin.minect.data.Surface;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestChunkRetrieval {

    private int toChunkIndex(int c) {
        return c < 0 ? ((c + 1) / 16) - 1 : c / 16;
    }

    private DataManager getDataManager() throws IOException {
        BlockMapper blockMapper = new BlockMapper(this.getClass().getResource("/block-colors.dat"), this.getClass().getResource("/biome-colors.dat"));
       return new DataManager(new File("src/test/resources"), blockMapper);
    }

    @Test
    public void testReadChunk() throws IOException {
        DataManager dm = this.getDataManager();
        Chunk c = dm.getChunk(0, 0);
        assertEquals("Heightmap", 36, c.getHeightMaps().getLongArray("WORLD_SURFACE").length);
        assertEquals("Section", 256, c.getSection(0).getBlockStates().length);
    }

    @Test
    public void testGetSurface() throws IOException {
        DataManager dm = this.getDataManager();
        Surface s = dm.getSurface(0, 0, false);
        Chunk c = dm.getChunk(0, 0);
        long[] map = c.getHeightMaps().getLongArray("WORLD_SURFACE");
        for(int x = 0; x < 16; ++x) {
            for(int z = 0; z < 16; ++z) {

                // Check height map
                int ys = s.getHeight(x, z);
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
                int yc = ((int) (h & 0x1ff)) - 1;
                assertEquals(String.format("Heightmap at %d,%d", x, z), yc, ys);

                // Check block ids
                int sId = s.getBlockId(x, z);
                int cId = dm.getBlockMapper().getIdForName(c.getBlockStateAt(x, ys, z).getString("Name"));
                assertEquals(String.format("Block id at %d,%d", x, z), cId, sId);

                // Check block lights
                int sLight = s.getBlockLight(x, z);
                int cLight = 0;
                Section section = c.getSection(ys / 16);
                if(section != null) {
                    byte[] blockLights = section.getBlockLight();
                    if (blockLights != null) {
                        int i = (x % 16) + (z % 16) * 8;
                        if (idx <= blockLights.length - 1) {
                            cLight = blockLights[i];
                            if ((i & 1) != 0) {
                                cLight >>>= 4;
                            }
                            cLight &= 0x0f;
                        }
                    }
                }
                assertEquals(String.format("Block light at %d,%d", x, z), cLight, sLight);

                // Check biomes
                int sB = s.getBiome(x, z);
                int cB = c.getBiomeAt(x, ys, z);
                assertEquals(String.format("Biome at %d,%d", x, z), cB, sB);

            }
        }
    }
}
