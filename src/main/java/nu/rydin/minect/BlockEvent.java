package nu.rydin.minect;

public class BlockEvent {
	private final int x;
	
	private final int y;
	
	private final int z;
	
	private final int type;

	public BlockEvent(int x, int y, int z, int type) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y; 
	}

	public int getZ() {
		return z;
	}

	public int getType() {
		return type;
	}
	
	
}
