package nu.rydin.minect;

import java.awt.Color;


public class PaintEngine {
	
	private ColorMapper colorMapper;
	
	private int highlight = -1;
	
	public PaintEngine(ColorMapper colorMapper)
	{
		super();
		this.colorMapper = colorMapper;
	}

	public void setHighlight(int h) {
		this.highlight = h;
	}

	/*
	
	public boolean paintChunkSlice(Graphics2D g, int y, int x0, int z0, int scale, Chunk chunk, JPanel panel) {
		byte[] data = chunk.getRawData();
		boolean highlightChunk = false;
		for(int idx = 0; idx < 256; ++idx) {
			int p = idx + y * 256;
			if(p >= data.length)
				return highlightChunk;
			byte blockId = data[p];
			if(blockId == highlight)
				highlightChunk = true;
			if(blockId != 0) {
				g.setColor(mapColor(blockId));
				g.fillRect(scale * (x0 + (idx & 0x0f)), scale * (z0 + (idx >> 4)), scale, scale);
			}
		}
		// g.drawImage(bi, 0, 0, panel);
		return highlightChunk;
	}


	public void paintSurfaceMap(Graphics2D g, int x0, int z0, int scale, Chunk chunk) {
		byte[] data = chunk.getRawData(); 
		int[] hm = chunk.getHeightMap();
		for(int idx = 0; idx < 256; ++idx) {
			int y = hm[idx] - 1;
			int p = idx + y * 256;
			if(p >=  data.length)
				return;
			byte blockId = data[p];
			if(blockId != 0) {
				int size = blockId == 54 ? 10 : 1;
				g.setColor(mapColor(blockId)); 
				g.fillRect(scale * (x0 + (idx & 0x0f)), scale * (z0 + (idx >> 4)), scale * size, scale * size);
			}
		}
	}
	 */
	
	public Color mapColor(byte blockId) {
		int b = Math.abs(blockId);
		return colorMapper.mapColor(b, (byte) 0);
	}
	
	/*public void drawHighlights() {
		for(Point p : highlightedChunks)
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(5));
		g.drawArc(x0 * scale, z0 * scale, 16, 16, 0, 360);
	} */
}
