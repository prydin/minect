package nu.rydin.minect.data;

import java.awt.*;
import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import net.querz.mca.MCAFile;
import net.querz.mca.Chunk;
import nu.rydin.minect.BlockMapper;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import static net.querz.mca.LoadFlags.*;


public class DataManager {

	private final BlockMapper blockMapper;
	
	private Cache<Point, MCAFile> regionCache;

	private Cache<Surface.Key, Surface> surfaceCache;

	private Cache<Slice.Key, Slice> sliceCache;

	private final HashSet<Point> empties = new HashSet<>();
	
	private final File regionPath;

	private int reads = 0;

	public DataManager(File regionPath, BlockMapper cm) {
		super();
		regionCache = new Cache2kBuilder<Point, MCAFile>() {}
				.eternal(true)
				.entryCapacity(8)
				.build();
		surfaceCache = new Cache2kBuilder<Surface.Key, Surface>() {}
				.eternal(true)
				.entryCapacity(100000)
				.permitNullValues(true)
				.build();
		sliceCache = new Cache2kBuilder<Slice.Key, Slice>() {}
				.eternal(true)
				.entryCapacity(100000)
				.permitNullValues(true)
				.build();
		this.regionPath = regionPath;
		blockMapper = cm;
	}

	public Surface getSurface(int cx, int cz, boolean dry) throws IOException {
		Surface.Key key = new Surface.Key(cx, cz, dry);
		CacheEntry<Surface.Key, Surface> e = surfaceCache.peekEntry(key);
		if(e != null) {
			return e.getValue();
		}
		Chunk chunk = this.getChunk(cx, cz);
		if(chunk == null) {
			surfaceCache.put(key, null);
			return null;
		}

		Surface s = Surface.fromChunk(chunk, this.blockMapper, dry);
		surfaceCache.put(key, s);
		return s;
	}

	public Slice getSlice(int cx, int cz, int sliceY) throws IOException {
		Slice.Key key = new Slice.Key(cx, cz, sliceY);
		CacheEntry<Slice.Key, Slice> e = sliceCache.peekEntry(key);
		if(e != null) {
			return e.getValue();
		}
		Chunk chunk = this.getChunk(cx, cz);
		if(chunk == null) {
			sliceCache.put(key, null);
			return null;
		}
		Slice s = Slice.fromChunk(chunk, blockMapper, sliceY);
		sliceCache.put(key, s);
		return s;
	}

	public Chunk getChunk(int cx, int cz) throws IOException {
		MCAFile region = this.getRegion(cx >> 5, cz >> 5);
		if(region == null) {
			return null;
		}
		Chunk chunk = region.getChunk(cx, cz);
		return chunk != null && "full".equals(chunk.getStatus()) ? chunk : null;
	}

	public MCAFile getRegion(int x, int z) throws IOException {
		Point key = new Point(x, z);
		if (empties.contains(key)) {
			return null;
		}
		MCAFile mca = regionCache.peek(key);
		if (mca == null) {
			String filename = "r." + x + "." + z + ".mca";
			File f = new File(regionPath, filename);
			if(!f.exists()) {
				empties.add(key);
				return null;
			}
			RandomAccessFile file;
			try {
				file = new RandomAccessFile(f, "rw");
			} catch (FileNotFoundException e) {
				return null;
			}
			try {
				mca = new MCAFile(x, z);
				System.err.println("Deserializing file: " + filename);
				mca.deserialize(file, HEIGHTMAPS | BLOCK_LIGHTS | BLOCK_STATES | BIOMES);
				regionCache.put(key, mca);
				++reads;
			} finally {
				file.close();
			}
			System.err.println("Region cache: " + regionCache.toString());
			System.err.println("Surface cache: " + surfaceCache.toString());
			System.err.println("Slice cache: " + sliceCache.toString());
		}
		return mca;
	}

	public final int getReads() {
		return reads;
	}

	public BlockMapper getBlockMapper() {
		return blockMapper;
	}
}
 