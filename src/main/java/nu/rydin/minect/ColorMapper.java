package nu.rydin.minect;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorMapper
{
	private static final class BlockType {
		private final String name;

		private final Color color;

		private final int biomeMod;

		public BlockType(String name, Color color, int biomeMod) {
			this.name = name;
			this.color = color;
			this.biomeMod = biomeMod;
		}

		public String getName() {
			return name;
		}

		public Color getColor() {
			return color;
		}

		public int getBiomeMod() {
			return biomeMod;
		}
	}

	private Map<String, Short> nameToIndex = new HashMap<>();

	private ArrayList<BlockType> blockTypes = new ArrayList<>();
	
	private final Color[][] biomes = new Color[256][];
	
	public ColorMapper(URL blockFile, URL biomeFile)  throws IOException {
		
		// Load block colors
		//
		BufferedReader rdr = new BufferedReader(new InputStreamReader(blockFile.openStream()));
		String line;
		Pattern pattern = Pattern.compile("(\\w*)\\s+0x([0-9A-F]+)\\s+([0-9]+)?.*");
		while((line = rdr.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0)
				continue;
			if(line.charAt(0) == '#') 
				continue;
			Matcher m = pattern.matcher(line);
			if(!m.matches())
				continue;
			String blockId = "minecraft:" + m.group(1);
			String colorStr = m.group(2);
			String biomeStr = m.group(3);
			int color = (int) Long.parseLong(colorStr, 16) & 0x00ffffff; // Strip transparency for now
			int biome = biomeStr != null ? Integer.parseInt(biomeStr.trim()) : -1;
			BlockType blockType = new BlockType(blockId, new Color(color), biome);
			nameToIndex.put(blockId, (short) blockTypes.size());
			blockTypes.add(blockType);
		}
		
		// Load biome colors
		//
		rdr = new BufferedReader(new InputStreamReader(biomeFile.openStream()));
		pattern = Pattern.compile("0x([0-9A-F]+)\\s+0x([0-9A-F]+)\\s+0x([0-9A-F]+)\\s+0x([0-9A-F]+).*");
		while((line = rdr.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0)
				continue;
			if(line.charAt(0) == '#') 
				continue;
			Matcher m = pattern.matcher(line);
			if(!m.matches())
				continue;
			int biomeId = Integer.parseInt(m.group(1), 16);
			int grass = (int) Long.parseLong(m.group(2), 16) & 0x00fffff;
			int foliage = (int) Long.parseLong(m.group(3), 16) & 0x00fffff;
			int water = (int) Long.parseLong(m.group(3), 16) & 0x00fffff;
			biomes[biomeId] = new Color[] { new Color(grass), new Color(foliage), new Color(water) };
		}
	}
	
	public Color mapColor(int blockId, int biome) {
		BlockType bt = blockTypes.get(blockId);
		Color base = bt.getColor();
		int bm = bt.getBiomeMod();
		if(bm != -1) {
			Color multiplier = biomes[biome][bm];
			return new Color((base.getRed() * multiplier.getRed()) / 255,
					(base.getGreen() * multiplier.getGreen()) / 255,
					(base.getBlue() * multiplier.getBlue()) / 255);
		} else
			return base;
	}

	public short getIdForName(String name) {
		Short id = nameToIndex.get(name);
		if(id != null) {
			return id;
		}
		System.err.println("Warning: Block type " + name + " not defined");
		return nameToIndex.get("minecraft:default");
	}
}
