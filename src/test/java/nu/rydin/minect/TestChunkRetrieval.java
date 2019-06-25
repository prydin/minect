package nu.rydin.minect;

import net.querz.nbt.mca.Chunk;
import nu.rydin.minect.data.ChunkManager;
import nu.rydin.minect.data.OptimizedChunk;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestChunkRetrieval {

    private int toChunkIndex(int c) {
        return c < 0 ? ((c + 1) / 16) - 1 : c / 16;
    }

    @Test
    public void testReadChunk() throws IOException {
        ColorMapper colorMapper = new ColorMapper(this.getClass().getResource("/block-colors.dat"), this.getClass().getResource("/biome-colors.dat"));
        ChunkManager cm = new ChunkManager(new File("src/test/resources"), colorMapper);
        OptimizedChunk c = cm.getChunk(0, 0);
    }

    @Test
    public void testReadHeightMap() throws IOException {
        ColorMapper colorMapper = new ColorMapper(this.getClass().getResource("/block-colors.dat"), this.getClass().getResource("/biome-colors.dat"));
        ChunkManager cm = new ChunkManager(new File("src/test/resources"), colorMapper);
        OptimizedChunk c = cm.getChunk(toChunkIndex(128), toChunkIndex(-241));
        Assert.assertTrue(c.getHeight(1, 0, false) == 65);
        for(int x = 0; x < 16; ++x) {
            for(int z = 0; z < 16; ++z) {
                Assert.assertTrue(c.getHeight(x, z, false) < 80);
            }
        }
    }

    @Test
    public void tesGetSurfaceBlocks() throws IOException {
        ColorMapper colorMapper = new ColorMapper(this.getClass().getResource("/block-colors.dat"), this.getClass().getResource("/biome-colors.dat"));
        ChunkManager cm = new ChunkManager(new File("src/test/resources"), colorMapper);
        OptimizedChunk c = cm.getChunk(toChunkIndex(0), toChunkIndex(0));
        for(int x = 0; x < 16; ++x) {
            for(int z = 0; z < 16; ++z) {
                System.err.println(c.getBlockIdAt(x, c.getHeight(x, z, false) - 1, z));
            }
        }
    }
}
