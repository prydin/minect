package nu.rydin.minect.data;

import java.util.Objects;
import net.querz.mca.Chunk;
import nu.rydin.minect.BlockMapper;

public class Surface extends AbstractSurface {
    public static class Key extends AbstractSurface.Key {
        private boolean dry;

        public Key(int x, int z, boolean dry) {
            super(x, z);
            this.dry = dry;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Key key = (Key) o;
            return dry == key.dry;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), dry);
        }
    }

    private ArbitraryWordArray heightMap;

    public static Surface fromChunk(Chunk chunk, BlockMapper bm, boolean dry) {
        Surface s = new Surface(chunk, dry);
        s.load(chunk, bm);
        return s;
    }

    protected Surface(Chunk chunk, boolean dry) {
        super();
        this.heightMap = new ArbitraryWordArray(chunk.getHeightMaps().getLongArray(dry ? "OCEAN_FLOOR" : "WORLD_SURFACE"), 9);
    }

    @Override
    public int getHeight(int x, int z) {
        return this.heightMap.get(z * 16 + x) - 1; // TODO: Why -1?
    }
}
