package nu.rydin.minect.data;

import java.util.Objects;
import net.querz.mca.Chunk;
import nu.rydin.minect.BlockMapper;

public class Slice extends AbstractSurface {
  private int sliceY;

  public static class Key extends AbstractSurface.Key {
    private int sliceY;

    public Key(int x, int z, int sliceY) {
      super(x, z);
      this.sliceY = sliceY;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      if (!super.equals(o)) return false;
      Key key = (Key) o;
      return sliceY == key.sliceY;
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), sliceY);
    }
  }

  public static Slice fromChunk(Chunk chunk, BlockMapper bm, int sliceY) {
    Slice s = new Slice(chunk, bm, sliceY);
    s.load(chunk, bm);
    return s;
  }

  protected Slice(Chunk chunk, BlockMapper bm, int sliceY) {
    super();
    this.sliceY = sliceY;
  }

  @Override
  public int getHeight(int x, int z) {
    return this.sliceY;
  }
}
