package nu.rydin.minect.data;

import java.io.*;
import java.util.HashSet;
import net.querz.mca.MCAFile;
import net.querz.mca.Chunk;
import nu.rydin.minect.ColorMapper;


public class DataManager {
	private final ColorMapper colorMapper;
	
	private Cache<String, MCAFile> regionCache = new Cache<>(6);

	private final HashSet<Long> empties = new HashSet<>();
	
	private final File regionPath;

	private int reads = 0;

	public DataManager(File regionPath, ColorMapper cm) {
		super();
		this.regionPath = regionPath;
		colorMapper = cm;
	}

	public MCAFile getRegion(int x, int z) throws IOException {
		long key = z << 32 | x;
		if (empties.contains(key)) {
			return null;
		}
		String filename = "r." + x + "." + z + ".mca";
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
		return mca;
	}

	public final int getReads() {
		return reads;
	}
}
 