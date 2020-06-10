package nu.rydin.minect;

import org.junit.Assert;
import org.junit.Test;

public class TestCoordinateTransform {
  @Test
  public void testChunkOffset() {
    Assert.assertEquals("Chunk offset failed", 15, PaintEngine.getChunkOffset(-17));
    Assert.assertEquals("Chunk offset failed", 15, PaintEngine.getChunkOffset(-1));
    Assert.assertEquals("Chunk offset failed", 8, PaintEngine.getChunkOffset(-8));
    Assert.assertEquals("Chunk offset failed", 1, PaintEngine.getChunkOffset(17));
    Assert.assertEquals("Chunk offset failed", 1, PaintEngine.getChunkOffset(1));
    Assert.assertEquals("Chunk offset failed", 8, PaintEngine.getChunkOffset(8));
    Assert.assertEquals("Chunk offset failed", 1, PaintEngine.getChunkOffset(8193));
  }
}
