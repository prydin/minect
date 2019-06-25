package nu.rydin.minect;

import net.querz.nbt.mca.Chunk;
import nu.rydin.minect.data.ChunkManager;
import nu.rydin.minect.data.OptimizedChunk;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestChunkRetrieval {

    @Test
    public void testReadChunk() throws IOException {
        ColorMapper colorMapper = new ColorMapper(this.getClass().getResource("/resources/block-colors.dat"), this.getClass().getResource("/resources/biome-colors.dat"));
        ChunkManager cm = new ChunkManager(new File("src/test/resources"), colorMapper);
        OptimizedChunk c = cm.getChunk(0, 0);
    }
}
