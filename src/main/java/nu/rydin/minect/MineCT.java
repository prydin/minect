package nu.rydin.minect;

import java.io.File;

public class MineCT {
  public static final void main(String[] args) throws Exception {
    String workingDirectory;
    String os = (System.getProperty("os.name")).toUpperCase();
    if (os.contains("WIN")) {
      workingDirectory = System.getenv("AppData");
    } else {
      workingDirectory = System.getProperty("user.home");
    }
    File mcRoot = new File(new File(workingDirectory, ".minecraft"), "saves");
    MineCTApplication app = new MineCTApplication(mcRoot);
    app.setVisible(true);

    /*cm.getChunk(-300, -300);

    RegionFile rf = new RegionFile(new File("C:\\Users\\prydin\\Documents\\Survival 2\\region\\r.0.0.mca"));
    InputStream in = rf.getChunkDataInputStream(0, 0);
    NBTInputStream nbtIn = new NBTInputStream(in);
    Chunk chunk = new Chunk(in);*/

    /*
    MapPanel p = new MapPanel(cm);
    p.setView(0,  0, 1000, 1000, 10, 1);
    JFrame f = new JFrame();
    f.setBounds(0, 0, 1000, 1000);
    f.add(p);
    f.setVisible(true);

    for(;;) {
    	for(int idx = 0; idx < 100; ++idx) {
    		Thread.sleep(200);
    		p.setView(0,  0, 1000, 1000, idx, 1);
    	}
    }
    */
  }
}
