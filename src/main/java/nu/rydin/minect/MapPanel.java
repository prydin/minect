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

import javax.swing.JPanel;

import nu.rydin.minect.data.AbstractSurface;
import nu.rydin.minect.data.DataManager;
import nu.rydin.minect.data.ChunkWrapper;

public class MapPanel extends JPanel {
	
	static final int SURFACE = 0;
	
	static final int SLICE = 1;
	
	private int sliceY;
	
	private int xMin;
	
	private int zMin;
	
	private BufferedImage cachedImage;
	
	private int cacheImgX;
	
	private int cacheImgZ;
	
	private double scale;

	private DataManager chunkMgr;
	
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
	
	private int mapMode = SURFACE;

	private boolean showChunkGrid = false;
	
	private final PaintEngine paintEngine;
	
	private final BlockMapper blockMapper;
	
	MapPanel(BlockMapper blockMapper) {
		super();
		this.blockMapper = blockMapper;
		this.paintEngine = new PaintEngine(blockMapper, chunkMgr);
		this.setDoubleBuffered(false);
		this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {				
			}
			
			@Override
			public void componentResized(ComponentEvent event) {
				cachedImage = null;
				MapPanel.this.setView(xMin, zMin, sliceY);
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
				try {
					int x = transform(event.getX()) + xMin;
					int z = transform(event.getY()) + zMin;
					AbstractSurface s = mapMode == SURFACE
							? chunkMgr.getSurface(toChunkIndex(x), toChunkIndex(z), dry)
							: chunkMgr.getSlice(toChunkIndex(x), toChunkIndex(z), sliceY);
					if(s == null) {
						// Mousing over an uninhabited chunk
						return;
					}
					int lx = toChunkLocal(x);
					int lz = toChunkLocal(z);
					int y = s.getHeight(lx, lz);
					for (BlockListener bl : blockListeners) {
						bl.mouseOverBlock(new BlockEvent(x, y, z, s.getBlockId(lx, lz)));
					}
				} catch(IOException e) {
					e.printStackTrace();
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
				boolean repaint = dragging;
				dragging = false;
				if(repaint) {
					repaint();
				}
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

	private static boolean isValidChunk(ChunkWrapper c) {
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

	private int toRegionIndex(int c) {
		return c < 0 ? ((c + 1) / 512) - 1 : c / 512;
	}


	public void addBlockListener(BlockListener bl) {
		blockListeners.add(bl);
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

	public void setShowChunkGrid(boolean b) {
		showChunkGrid = b;
		this.flush();
		this.repaint();
	}

	public void setView(int xMin, int zMin, int sliceY) {
		this.xMin = xMin;
		this.zMin = zMin;
		if(this.sliceY != sliceY || this.scale != scale)
			this.flush();
		this.sliceY = sliceY;
		this.scale = scale;
		this.repaint();
	}
	
	public void setChunkManager(DataManager cm) {
		this.chunkMgr = cm;
		this.paintEngine.setChunkManager(cm);
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
	
	public void setScale(double scale) {
		this.scale = scale;
		this.flush();
		this.repaint();
	}
	
	public void setMode(int mode) {
		this.mapMode = mode;
		this.flush();
		this.repaint();
	}

	public boolean isHighlightTorches() {
		return highlightTorches;
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
		return super.imageUpdate(img, infoflags, x, y, w, h);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// No world open yet? Skip all painting!
		if(chunkMgr == null) {
			return;
		}

		// Compute scaled and offset bounds
		int startReads = chunkMgr.getReads();
		final int xMax = xMin + transform(this.getWidth());
		final int zMax = zMin + transform(this.getHeight());

		// Create scale transform
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);

		// Workaround for Apple Retina Displays
		AffineTransform displayScale = g2.getFontRenderContext ().getTransform ();
		transform.concatenate(displayScale);
		g2.setTransform(transform);

		// Dragging? Just move the cached image until mouse button is released.
		if(dragging && cachedImage != null) {
			g.drawImage(cachedImage, cacheImgX - xMin, cacheImgZ - zMin, null);
			return;
		}

		// Can we reuse the cached image in its entirety?
		if(cachedImage != null && xMin == cacheImgX && zMin == cacheImgZ) {
			g.drawImage(cachedImage, 0, 0, null);
			return;
		}
		
		// Can we reuse part of the cached image?
		final BufferedImage img = new BufferedImage(transform(this.getWidth()), transform(this.getHeight()), BufferedImage.TYPE_INT_RGB);
		if(cachedImage != null && cacheImgX < xMax && cacheImgZ < zMax && cacheImgX + cachedImage.getWidth() > xMin && cacheImgZ + cachedImage.getHeight() > zMin) {
			Graphics2D bg = (Graphics2D) img.getGraphics();
			System.out.println("cachedX: " + cacheImgX + ", width: " + cachedImage.getWidth() + ", xMin: " + xMin + " drawMax: " + (cacheImgX + cachedImage.getWidth()));
			bg.drawImage(cachedImage, cacheImgX - xMin, cacheImgZ - zMin, null);
			g.drawImage(img, 0, 0, this);

			// Need to fill on the left?
			//
			if(xMin < cacheImgX)
				this.paintRect(img, xMin, zMin, cacheImgX + 1, zMax);
			
			// Need to fill on the right?
			//
			if(xMin > cacheImgX)
				this.paintRect(img, cacheImgX + cachedImage.getWidth(), zMin, xMax, zMax);
			
			// Need to fill on top?
			//
			if(zMin < cacheImgZ)
				this.paintRect(img, xMin, zMin, xMax, cacheImgZ + 1);
			
			// Need to fill on the bottom? 
			//
			if(zMin > cacheImgZ)
				this.paintRect(img, xMin, cacheImgZ + cachedImage.getHeight(), xMax, zMax);
		} else {
			// No overlap/no image. Complete repaint.
			//
			g.drawImage(img, 0, 0, this);
			this.paintRect(img, xMin, zMin, xMax, zMax);
		}
		cachedImage = img;
		cacheImgX = xMin;
		cacheImgZ = zMin;
		System.out.println("Rendering required " + (chunkMgr.getReads() - startReads) + " reads");
	}

	private boolean paintRect(BufferedImage img, int x0, int z0, int xMax, int zMax)
	{
		PaintEngine.Context ctx = new PaintEngine.Context(
				img.getGraphics(), img, this, x0, z0, xMax, zMax, this.xMin, this.zMin, this.sliceY,
				this.mapMode, this.dry, this.paintShade, this.paintContour, this.night, this.highlightTorches,
				this.paintLight, this.showChunkGrid);

		// If all four corner chunks can be found in cache, chance are pretty good the image can be rendered
		// quickly, so no need to do it asynchronously.
		if(isProbablyCached(x0, z0, xMax, zMax)) {
			try {
				paintEngine.paintArea(ctx);
				return false;
			} catch(IOException e) {
				e.printStackTrace();
			}
		} else {
			paintEngine.paintAreaAsynch(ctx);
			return true;
		}
	}

	private boolean isProbablyCached(int x0, int z0, int xMax, int zMax) {
		int cx0 = toChunkIndex(x0);
		int cz0 = toChunkIndex(z0);
		int cxMax = toChunkIndex(xMax);
		int czMax = toChunkIndex(zMax);
		if(mapMode == SURFACE) {
			return chunkMgr.isSurfaceCached(cx0, cz0, dry) &&
					chunkMgr.isSurfaceCached(cxMax, cz0, dry) &&
					chunkMgr.isSurfaceCached(cxMax, czMax, dry) &&
					chunkMgr.isSurfaceCached(cx0, czMax, dry);
		} else {
			return chunkMgr.isSliceCached(cx0, cz0, sliceY) &&
					chunkMgr.isSliceCached(cxMax, cz0, sliceY) &&
					chunkMgr.isSliceCached(cxMax, czMax, sliceY) &&
					chunkMgr.isSliceCached(cx0, czMax, sliceY);
		}
	}
}
