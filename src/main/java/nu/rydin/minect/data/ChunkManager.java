package nu.rydin.minect.data;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import net.querz.nbt.mca.MCAFile;
import net.querz.nbt.mca.Chunk;
import nu.rydin.minect.ColorMapper;


public class ChunkManager {
	private final class Key {
		private long x;
		
		private long z;

		public Key(long x, long z) {
			super();
			this.x = x;
			this.z = z;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (x ^ (x >>> 32));
			result = prime * result + (int) (z ^ (z >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x != other.x)
				return false;
			if (z != other.z)
				return false;
			return true;
		}

		private ChunkManager getOuterType() {
			return ChunkManager.this;
		}
	}

	private final ColorMapper colorMapper;
	
	private Cache<String, MCAFile> regionCache = new Cache<>(10);
	
	private final HashMap<Key, SoftReference<OptimizedChunk>> cache = new HashMap<>();
	
	private final HashSet<Key> empties = new HashSet<>();
	
	private final File regionPath;

	private int reads = 0;

	public ChunkManager(File regionPath, ColorMapper cm) {
		super();
		this.regionPath = regionPath;
		colorMapper = cm;
	}
	
	public synchronized OptimizedChunk getChunk(int x, int z) throws IOException {
		Key key = new Key(x, z);
		SoftReference<OptimizedChunk> entry = cache.get(key);
		if (empties.contains(key))
			return null;
		if (entry != null && entry.get() != null)
			return entry.get();
		String filename = "r." + (x >> 5) + "." + (z >> 5) + ".mca";
		MCAFile mca = regionCache.get(filename);
		if (mca == null) {
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
				mca.deserialize(file);
				regionCache.put(filename, mca);
				++reads;
			} finally {
				file.close();
			}
		}
		Chunk c = mca.getChunk(x, z);
		if (c != null) {
			OptimizedChunk oc = new OptimizedChunk(c, colorMapper);
			cache.put(key, new SoftReference<>(oc));
			return oc;
		}
		empties.add(key);
		return null;
	}

	public final int getReads() {
		return reads;
	}
}
 