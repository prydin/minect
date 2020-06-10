package nu.rydin.minect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import nu.rydin.minect.data.AbstractSurface;
import nu.rydin.minect.data.DataManager;


public class PaintEngine {

	public static class Context {
		private final Graphics graphics;
		private final BufferedImage img;
		private final int x0;
		private final int z0;
		private final int xMax;
		private final int zMax;
		private final int globalX0;
		private final int globalZ0;
		private final int sliceY;
		private final int mapMode;
		private final boolean dry;
		private final boolean paintShade;
		private final boolean paintElevation;
		private final boolean night;
		private final boolean highlightTorches;
		private final boolean paintLight;
		private final ImageObserver imageObserver;
		private boolean showChunkGrid = false;
		private List<Point> torches = new ArrayList<>();

		public Context(Graphics graphics, BufferedImage img, ImageObserver imageObserver, int x0, int z0, int xMax, int zMax,
					   int globalX0, int globalZ0, int sliceY, int mapMode, boolean dry, boolean paintShade, boolean paintElevation,
					   boolean night, boolean highlightTorches, boolean paintLight, boolean showChunkGrid) {
			this.graphics = graphics;
			this.img = img;
			this.x0 = x0;
			this.z0 = z0;
			this.xMax = xMax;
			this.zMax = zMax;
			this.globalX0 = globalX0;
			this.globalZ0 = globalZ0;
			this.mapMode = mapMode;
			this.dry = dry;
			this.paintElevation = paintElevation;
			this.paintShade = paintShade;
			this.night = night;
			this.sliceY = sliceY;
			this.highlightTorches = highlightTorches;
			this.paintLight = paintLight;
			this.imageObserver = imageObserver;
			if (highlightTorches) {
				this.torches = new ArrayList<>();
			}
			this.showChunkGrid = showChunkGrid;
		}
	}

	public class Worker extends SwingWorker<Void, Object> {
		private final Context ctx;

		public Worker(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected Void doInBackground() throws Exception {
			PaintEngine.this.paintArea(ctx);
			return null;
		}
	}
	
	private BlockMapper blockMapper;

	private DataManager chunkManager;

	public PaintEngine(BlockMapper blockMapper, DataManager chunkManager) {
		super();
		this.blockMapper = blockMapper;
		this.chunkManager = chunkManager;
	}

	public void paintAreaAsynch(Context ctx) {
		Worker w = new Worker(ctx);
		w.execute();
	}

	public void paintArea(Context ctx) throws IOException {
		System.out.println("Paint area:" + ctx.x0 + "," + ctx.z0 + "-->" + ctx.xMax + "," + ctx.zMax);
		int rx0 = toRegionIndex(ctx.x0);
		int rxMax = toRegionIndex(ctx.xMax);
		int rz0 = toRegionIndex(ctx.z0);
		int rzMax = toRegionIndex(ctx.zMax);
		for(int rz = rz0; rz <= rzMax; ++rz) {
			for(int rx = rx0; rx <= rxMax; ++rx) {
				this.paintRegion(rx, rz, ctx);
			}
		}

		// Deal with torches
		//
		if(ctx.highlightTorches) {
			for (Point p : ctx.torches) {
				int x = p.x;
				int z = p.y;
				Graphics2D bg = (Graphics2D) ctx.img.getGraphics();
				bg.setStroke(new BasicStroke(4.0F));
				bg.setColor(new Color(255, 255, 0, 150));
				bg.drawArc(x - 3, z - 3, 6, 6, 0, 360);
				bg.setColor(new Color(255, 255, 0, 80));
				bg.drawArc(x - 5, z - 5, 10, 10, 0, 360);
			}
		}
	}

	private void paintRegion(int rx, int rz, Context ctx) throws IOException {
		System.err.println("Memory: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + "/" + Runtime.getRuntime().maxMemory() / 1024 / 1024);
		int startX = rx * 512 - ctx.x0 < 0 ? (ctx.x0 & 511) / 16 : 0;
		int startZ = rz * 512 - ctx.z0 < 0 ? (ctx.z0 & 511) / 16 : 0;
		for(int cz = startZ; cz < 32; ++cz) {
			for(int cx = startX; cx < 32; ++cx) {
				this.paintChunk(cx, cz, rx, rz, ctx);
			}
		}
	}

	private void paintChunk(int cx, int cz, int rx, int rz, Context ctx) throws IOException{
		int globalX = rx * 32 + cx;
		int globalZ = rz * 32 + cz;
		AbstractSurface surface = ctx.mapMode == MapPanel.SURFACE
				? chunkManager.getSurface(globalX, globalZ, ctx.dry)
				: chunkManager.getSlice(globalX, globalZ, ctx.sliceY);
		if(surface == null) {
			return;
		}
		int globalXOffset = ctx.x0 - ctx.globalX0;
		int globalZOffset = ctx.z0 - ctx.globalZ0;

		// Calculate where in the resulting image this chunk starts
		int anchorX = rx * 512 + cx * 16 - ctx.x0;
		int anchorZ = rz * 512 + cz * 16 - ctx.z0;

		// If the chunk goes into negative coordinates, we have to adjust for that
		int startX = anchorX < 0 ? -anchorX : 0;
		int startZ = anchorZ < 0 ? -anchorZ : 0;

		// ImgX and ImgZ are normalized to zero-based coordinates within the rectangle
		// we're paining. They are NOT screen or image coordinates!
		for (int z = startZ; z < 16; ++z) {
			int imgZ = anchorZ + z + globalZOffset;
			if(imgZ >= ctx.img.getHeight()) {
				break; // Out of bounds
			}
			for (int x = startX; x < 16; ++x) {
				int imgX = anchorX + x + globalXOffset;
				if (imgX >= ctx.img.getWidth()) {
					break; // Out of bounds
				}

				int y = surface.getHeight(x, z);
				Color pixel = this.getPixelColor(surface, x, y, z, ctx);
				if(imgX < 0 || imgZ < 0) {
					System.err.println("Negative image coordinate: " + imgX + "," + imgZ + " cx=" + cx + " cz=" + cz);
					continue;
				}
				if(ctx.showChunkGrid && (x == 15 || z == 15)) {
					pixel = Color.RED;
				}
				ctx.img.setRGB(imgX, imgZ, pixel.getRGB());
			}
		}
		if(ctx.imageObserver != null) {
			ctx.imageObserver.imageUpdate(ctx.img, ImageObserver.SOMEBITS, anchorX < 0 ? 0 : anchorX,
					anchorZ < 0 ? 0 : anchorZ, 16, 16);
		}
	}

	private Color getPixelColor(AbstractSurface surface, int x, int y, int z, Context ctx) {
		Color pixel;
		int blockId = 0;
		switch (ctx.mapMode) {
			case MapPanel.SURFACE:
				blockId = surface.getBlockId(x, z);
				pixel = blockMapper.mapColor(blockId, surface.getBiome(x, z));
				if (ctx.paintShade) {

					// Calculations for shading effect
					int y1 = y;
					if (x < 15 && z < 15) {
						y1 = surface.getHeight(x + 1, z + 1);
					} else {
						// TODO: Deal with edges
					}

					float factor = 1.0F - (((float) y1 - (float) y) / 10.0F);
					if (factor < 0.0F) {
						factor = 0.0F;
					}
					pixel = new Color((int) Math.min((float) pixel.getRed() * factor, 255),
							(int) Math.min((float) pixel.getGreen() * factor, 255),
							(int) Math.min((float) pixel.getBlue() * factor, 255));
				}
				if (ctx.paintElevation) {
					int layer = y / 5;
					boolean darken = false;
					if (x < 15 && z < 15) {
						darken = layer != surface.getHeight(x + 1, z) / 5 ||  layer != surface.getHeight(x, z + 1) / 5;
					} else {
						// TODO: Deal with edges
					}
					if (darken)
						pixel = pixel.darker();

				}
				if (ctx.night) {
					float factor = 0.2F + ((float) surface.getBlockLight(x, z) / 16.0F) * 0.8F;
					pixel = new Color((int) Math.min((float) pixel.getRed() * factor, 255),
							(int) Math.min((float) pixel.getGreen() * factor, 255),
							(int) Math.min((float) pixel.getBlue() * factor, 255));
				}
				break;
			case MapPanel.SLICE:
				blockId = surface.getBlockId(x, z);
				int biome = surface.getBiome(x, z);
				if (ctx.paintLight && blockId == 0) {
					pixel = new Color(240, 240, 255 - 16 * surface.getBlockLight(x, z));
				} else
					pixel = blockMapper.mapColor(blockId, biome);
				break;
			default:
				System.err.println("Unknown map mode. Aborting redraw");
				return null;
		}
		return pixel;
	}

	private static int toRegionIndex(int c) {
		return c < 0 ? ((c + 1) / 512) - 1 : c / 512;
	}

	private static int toChunkIndex(int c) {
		return c < 0 ? ((c + 1) / 16) - 1 : c / 16;
	}

	public void setChunkManager(DataManager chunkManager) {
		this.chunkManager = chunkManager;
	}

	static int getChunkOffset(int c) {
		return c & 15;
	}

	static int getRegionOffset(int c) {
		return c & 511;
	}
}

