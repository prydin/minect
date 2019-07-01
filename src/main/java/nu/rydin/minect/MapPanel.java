package nu.rydin.minect;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.querz.nbt.mca.Chunk;

import javax.swing.JPanel;

import nu.rydin.minect.data.ChunkManager;
import nu.rydin.minect.data.OptimizedChunk;

public class MapPanel extends JPanel {
	
	static final int SURFACE = 0;
	
	static final int SLICE = 1;
	
	private int sliceY;
	
	private int xMin;
	
	private int zMin;
	
	private BufferedImage cachedImage;
	
	private int cacheImgX;
	
	private int cacheImgZ;
	
	//private int xMax;
	
	//private int zMax;
	
	private double scale;

	private ChunkManager chunkMgr;
	
	private boolean dragging = false;
	
	private int lastDragX;
	
	private int lastDragY;
	
	private boolean paintContour = false;
	
	private boolean paintLight = false;

	private boolean paintShade = true;
	
	private boolean highlightTorches = false;

	private boolean dry = false;
	
	private boolean night = false;
	
	private final List<BlockListener> blockListeners = new ArrayList<>();
	
	private int mapMode = SLICE;
	
	private final PaintEngine paintEngine;
	
	private final ColorMapper colorMapper;
	
	MapPanel(ColorMapper colorMapper) {
		super();
		this.colorMapper = colorMapper;
		this.paintEngine = new PaintEngine(colorMapper);
		this.setDoubleBuffered(false);
		this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {				
			}
			
			@Override
			public void componentResized(ComponentEvent event) {
				cachedImage = null;
				MapPanel.this.setView(xMin, zMin, sliceY, scale);
			}
			
			@Override
			public void componentMoved(ComponentEvent arg0) {				
			}
			
			@Override
			public void componentHidden(ComponentEvent arg0) {				
			}
		});
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent event) {
				if(chunkMgr == null) {
					return;
				}
				int x = transform(event.getX()) + xMin;
				int z = transform(event.getY()) + zMin;
				int y = sliceY;
				for(BlockListener bl : blockListeners) {
					if(mapMode == SURFACE)
						y = getHeightAt(x, z);
					bl.mouseOverBlock(new BlockEvent(x, y, z, getBlockIdAt(x, y ,z)));
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent event) {
				System.out.println("Dragged: " + event.getX() + " " + event.getY());
				if(dragging) {
					int dx = transform(event.getX() - lastDragX);
					int dy = transform(event.getY() - lastDragY);
					xMin -= dx;
					zMin -= dy;
					repaint();
				}
				lastDragX = event.getX();
				lastDragY = event.getY();
				dragging = true;
			}
		});
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				dragging = false;
			}
			
			@Override 
			public void mousePressed(MouseEvent event) {
				dragging = true;
				lastDragX = event.getX();
				lastDragY = event.getY();
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}


	private int getHeightAt(int x, int z)  {
		try {
			OptimizedChunk chunk = chunkMgr.getChunk(toChunkIndex(x), toChunkIndex(z));
			return isValidChunk(chunk) ? chunk.getHeight(toChunkLocal(x), toChunkLocal(z), dry) : 0;
		} catch(IOException e) {
			return 0;
		}
	}

	private short getBlockIdAt(int x, int y, int z) {
		try {
			OptimizedChunk chunk = chunkMgr.getChunk(toChunkIndex(x), toChunkIndex(z));
			return isValidChunk(chunk) ? chunk.getBlockIdAt(x & 15, y, z & 15) : 0;
		} catch(IOException e) {
			return 9;
		}
	}


	private static boolean isValidChunk(OptimizedChunk c) {
		return c != null && c.isValid();
	}
	
	private static int toChunkLocal(int c) {
		return c & 15;
	}
	
	private int transform(int c) {
		return (int) ((double) c / scale);
	}
	
	private int toChunkIndex(int c) {
		return c < 0 ? ((c + 1) / 16) - 1 : c / 16; 
	}
	
	public void addBlockListener(BlockListener bl) {
		blockListeners.add(bl);
	}
	
	public void setHighlight(int highlight) {
		paintEngine.setHighlight(highlight);
	}
	
	public void setPaintContour(boolean b) {
		paintContour = b;
		this.flush();
		this.repaint();
	}
	
	public void setDry(boolean b) {
		dry = b;
		this.flush();
		this.repaint();
	}
	
	public void setPaintLight(boolean b) {
		paintLight = b;
		this.flush();
		this.repaint();
	}
	
	public void setNight(boolean b) {
		night = b;
		this.flush();
		this.repaint();
	}

	
	public void setHighlightTorches(boolean b) {
		highlightTorches = b;
		this.flush();
		this.repaint();
	}

	public void setView(int xMin, int zMin, int sliceY, double scale) {
		this.xMin = xMin;
		this.zMin = zMin;
		if(this.sliceY != sliceY || this.scale != scale)
			this.flush();
		this.sliceY = sliceY;
		this.scale = scale;
		this.repaint();
	}
	
	public void setChunkManager(ChunkManager cm) {
		this.chunkMgr = cm;
		this.flush();
		this.repaint();
	}
	
	public void flush() {
		cachedImage = null;
	}
	
	public void setLayer(int sliceY) {
		this.sliceY = sliceY;
		this.flush();
		this.repaint();
	}
	
	public void setScale(int scale) {
		this.scale = scale;
		this.flush();
		this.repaint();
	}
	
	public void setMode(int mode) {
		this.mapMode = mode;
		this.flush();
		this.repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// No world open yet? Skip all painting!
		//
		if(chunkMgr == null) 
			return;

		// Compute scaled and offset bounds
		int startReads = chunkMgr.getReads();
		final int xMax = xMin + transform(this.getWidth());
		final int zMax = zMin + transform(this.getHeight());

		// Create scale transform
		//
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);

		// Workaround for Apple Retina Displays
		//
		AffineTransform displayScale = g2.getFontRenderContext ().getTransform ();
		transform.concatenate(displayScale);
		g2.setTransform(transform);

		// Can we reuse the cached image in its entirety?
		//
		if(cachedImage != null && xMin == cacheImgX && zMin == cacheImgZ) {
			g.drawImage(cachedImage, 0, 0, this);
			return;
		}
		
		// Can we reuse part of the cached image?
		//
		//cachedImage = null;
		final BufferedImage img = new BufferedImage(transform(this.getWidth()), transform(this.getHeight()), BufferedImage.TYPE_INT_RGB);
		if(cachedImage != null && cacheImgX < xMax && cacheImgZ < zMax && cacheImgX + cachedImage.getWidth() > xMin && cacheImgZ + cachedImage.getHeight() > zMin) {
			Graphics2D bg = (Graphics2D) img.getGraphics();
			System.out.println("cachedX: " + cacheImgX + ", width: " + cachedImage.getWidth() + ", xMin: " + xMin + " drawMax: " + (cacheImgX + cachedImage.getWidth()));
			bg.drawImage(cachedImage, cacheImgX - xMin, cacheImgZ - zMin, null);
						
			// Need to fill on the left?
			//
			if(xMin < cacheImgX)
				this.paintRegion(img, xMin, zMin, cacheImgX + 1, zMax);
				
			
			// Need to fill on the right?
			//
			if(xMin > cacheImgX)
				this.paintRegion(img, cacheImgX + cachedImage.getWidth(), zMin, xMax, zMax);
			
			// Need to fill on top?
			//
			if(zMin < cacheImgZ)
				this.paintRegion(img, xMin, zMin, xMax, cacheImgZ + 1);
			
			// Need to fill on the bottom? 
			//
			if(zMin > cacheImgZ)
				this.paintRegion(img, xMin, cacheImgZ + cachedImage.getHeight(), xMax, zMax);
		} else {
			// No overlap/no image. Complete repaint.
			//
			this.paintRegion(img, xMin, zMin, xMax, zMax);
		}
		g.drawImage(img, 0, 0, this);
		cachedImage = img;
		cacheImgX = xMin;
		cacheImgZ = zMin;
		System.out.println("Rendering required " + (chunkMgr.getReads() - startReads) + " reads");
	}

	private void paintRegion(BufferedImage img, int x0, int z0, int xMax, int zMax)
	{

		ArrayList<Point> torches = new ArrayList<>();
		int chunk0 = toChunkIndex(x0);
		int chunkMax = toChunkIndex(xMax);
		int stripSize = chunkMax - chunk0 + 1;
		System.out.println("Repainting area: " + x0 + "," + z0 + "," + xMax + "," + zMax + " Stripsize=" + stripSize);
		try {
			OptimizedChunk[] strip = null;
			OptimizedChunk[] nextStrip = null;

			for(int z = z0; z < zMax; ++z) {
				// Load strip if needed
				//
				if(strip == null || (z & 15) == 0) {
					if(nextStrip == null) {
						strip = new OptimizedChunk[stripSize];
						for(int idx = 0; idx < stripSize; ++idx) 
							strip[idx] = chunkMgr.getChunk(chunk0 + idx , toChunkIndex(z));						
					} else
						strip = nextStrip;
					nextStrip = new OptimizedChunk[stripSize];
					for(int idx = 0; idx < stripSize; ++idx) 
						nextStrip[idx] = chunkMgr.getChunk(chunk0 + idx , toChunkIndex(z) + 1);
				}
				for(int x = x0; x < xMax; ++x) {
					int cx = x & 15, cz = z & 15;
					int chunkIdx = toChunkIndex(x) - chunk0;
					OptimizedChunk chunk = strip[chunkIdx];
					if(!isValidChunk(chunk))
					{
						// Skip empty chunk
						//
						x += 15 - (x & 15); 
						continue;
					}
					Color pixel = Color.GREEN;
					int y = chunk.getHeight(cx, cz, dry);
					switch(mapMode) {
					case SURFACE: 
						pixel = colorMapper.mapColor(chunk.getBlockIdAt(cx , y, cz), chunk.getBiomeAt(cx, cz));
						if(paintShade && isValidChunk(chunk)) {
							int y1 = y;
							if(x < xMax - 1) {
								if(cz < 15) {
									if(cx < 15)
										y1 = chunk.getHeight(cx + 1, cz + 1, dry);
									else 
										y1 = chunkIdx < stripSize - 1 && isValidChunk(strip[chunkIdx + 1]) ? strip[chunkIdx + 1].getHeight(0, cz + 1, dry) : y;
								} else {
									if(nextStrip != null) {
										if(cx < 15)
											y1 = chunkIdx < stripSize && isValidChunk(nextStrip[chunkIdx]) ? nextStrip[chunkIdx].getHeight(cx + 1, 0, dry) : y;
										else 
											y1 = chunkIdx < stripSize - 1 && isValidChunk(nextStrip[chunkIdx + 1]) ? nextStrip[chunkIdx + 1].getHeight(0, 0, dry) : y;
									} 
								}
							}
			
							float factor = 1.0F - (((float) y1 - (float) y) / 10.0F);
							if(factor < 0.0F) {
								//System.out.println(factor);
								factor = 0.0F;
							}
								
						//	if(factor != 1.0F) 
							//	System.out.println(factor);
							pixel = new Color((int) Math.min((float) pixel.getRed() * factor, 255), 
									(int) Math.min((float) pixel.getGreen() * factor, 255),
									(int) Math.min((float) pixel.getBlue() * factor, 255));
						}
						if(paintContour) { 
							int layer = y / 5;
							boolean darken = false;
							if(cx < 15) {
								if(layer != chunk.getHeight(cx + 1, cz, dry) / 5)
									darken = true;
							} else {
								OptimizedChunk neighbor = chunkIdx < stripSize - 1
									? strip[chunkIdx + 1]
									: chunkMgr.getChunk(x + 1, z);
								if(isValidChunk(neighbor) && layer != neighbor.getHeight( 0, cz, dry) / 5)
									darken = true;
							}
							if(!darken) {
								if(cz < 15) {
									if(layer != chunk.getHeight(cx, cz + 1, dry) / 5)
										darken = true;
								} else {
 									OptimizedChunk neighbor = nextStrip[chunkIdx];
									if(isValidChunk(neighbor) && layer != neighbor.getHeight(cx, 0,dry ) / 5)
										darken = true;
								}
							} 
							if(darken)
								pixel = pixel.darker();
								
						}
						if(night) {
							float factor = 0.2F + ((float) chunk.getSurfaceBlockLight(cx, cz) / 16.0F) * 0.8F;
							pixel = new Color((int) Math.min((float) pixel.getRed() * factor, 255), 
									(int) Math.min((float) pixel.getGreen() * factor, 255),
									(int) Math.min((float) pixel.getBlue() * factor, 255));
						}
						break;
					case SLICE:
						if(sliceY >= y)
						{
							pixel = Color.WHITE;
							break;
						}
						short blockId = chunk.getBlockIdAt(cx , sliceY, cz);
						int biome = chunk.getBiomeAt(cx, cz);
						if(highlightTorches && colorMapper.isLightSource(blockId))
							torches.add(new Point(x, z));
						if(paintLight && blockId == 0) {
							pixel = new Color(240, 240, 255 - 16 * chunk.getBlockLightAt(cx, sliceY, cz));
						} else
							pixel = sliceY < y ? colorMapper.mapColor(blockId, biome) : Color.WHITE;
						break;
					default:
						System.err.println("Unknown map mode. Aborting redraw");
						return;
					} 
					img.setRGB(x - this.xMin, z - this.zMin, pixel.getRGB());
				}
			}
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		// Deal with torches
		//
		for(Point p : torches) {
			int x = p.x;
			int z = p.y;
			Graphics2D bg = (Graphics2D) img.getGraphics();
			bg.setStroke(new BasicStroke(4.0F));
			bg.setColor(new Color(255, 255, 0, 150));
			bg.drawArc(x - this.xMin - 3, z - this.zMin - 3, 6, 6, 0, 360);
			bg.setColor(new Color(255, 255, 0, 80));
			bg.drawArc(x - this.xMin - 5, z - this.zMin - 5, 10, 10, 0, 360);
		}
	}

	private int getHeightAt(Chunk c, int x, int z, boolean dry) {
		String key = dry ? "OCEAN_FLOOR" : "WORLD_SURFACE";
		return 0; //c.getHeightMaps().getLongArrayTag(key).getValue()[x + z * 16];
	}
}
