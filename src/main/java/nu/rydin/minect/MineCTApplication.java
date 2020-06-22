package nu.rydin.minect;

import nu.rydin.minect.data.DataManager;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MineCTApplication extends JFrame {
  private static final long serialVersionUID = -2054778334977399472L;
  private final int initialZoom = 5;

  private DataManager chunkMgr;

  private final MapPanel mapPanel;

  private final JSlider layerSlider;

  private final JSlider scaleSlider;

  private final JPanel statusPanel;

  private final JLabel coordinateLabel;

  private final File workingDirectory;

  private final JToggleButton ctButton;

  private final JToggleButton torchButton;

  private final JToggleButton lightButton;

  private final JToggleButton contourButton;

  private final JToggleButton dryButton;

  private final JToggleButton nightButton;

  private final JToggleButton chunkButton;

  private final BlockMapper blockMapper;

  private final ToolbarToggleListener toolbarListener = new ToolbarToggleListener();

  public final class ToolbarToggleListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();
      boolean checked = ((JToggleButton) e.getSource()).isSelected();
      if ("CT".equals(command)) {
        if (checked) {
          mapPanel.setMode(MapPanel.SLICE);
        } else {
          mapPanel.setMode(MapPanel.SURFACE);
        }
      } else if ("TORCH".equals(command)) {
        mapPanel.setHighlightTorches(checked);
      } else if ("LIGHT".equals(command)) {
        mapPanel.setPaintLight(checked);
      } else if ("CONTOUR".equals(command)) {
        mapPanel.setPaintContour(checked);
      } else if ("DRY".equals(command)) {
        mapPanel.setDry(checked);
      } else if ("NIGHT".equals(command)) {
        mapPanel.setNight(checked);
      } else if ("CHUNK".equals(command)) {
        mapPanel.setShowChunkGrid(checked);
      }
    }
  }

  public MineCTApplication(File workingDirectory) throws IOException {
    // Create the menu
    //
    this.workingDirectory = workingDirectory;
    JMenuBar menuBar = new JMenuBar();
    JMenu file = new JMenu("File");
    menuBar.add(file);

    JMenuItem openFile = new JMenuItem("Open Directory", KeyEvent.VK_O);
    openFile.addActionListener((ActionEvent e) -> openFileDialog());
    file.add(openFile);

    JMenuItem openWorld = new JMenuItem("Open World", KeyEvent.VK_W);
    file.add(openWorld);
    file.addActionListener((ActionEvent e) -> openWorld());
    setJMenuBar(menuBar);

    // Create Window Contents
    //
    JToolBar toolbar = new JToolBar("Main toolbar");
    toolbar.add(ctButton = makeNavigationButton("ct.gif", "CT", "CT mode", "CT"));
    toolbar.add(
        lightButton = makeNavigationButton("light.gif", "LIGHT", "Show block light", "Light"));
    toolbar.add(
        torchButton = makeNavigationButton("torch.gif", "TORCH", "Highlight torches", "Torch"));
    toolbar.add(
        contourButton = makeNavigationButton("contour.gif", "CONTOUR", "Contour lines", "Contour"));
    toolbar.add(dryButton = makeNavigationButton("water.gif", "DRY", "Show/hide water", "Water"));
    toolbar.add(nightButton = makeNavigationButton("night.gif", "NIGHT", "Night mode", "Night"));
    toolbar.add(
        chunkButton = makeNavigationButton("chunk.png", "CHUNK", "Show chunk grid", "Chunk"));
    add(toolbar);

    blockMapper =
        new BlockMapper(
            getClass().getResource("/block-colors.dat"),
            getClass().getResource("/biome-colors.dat"));
    mapPanel = new MapPanel(blockMapper);
    add(mapPanel);
    GroupLayout layout = new GroupLayout(getContentPane());
    setLayout(layout);
    mapPanel.setVisible(true);
    mapPanel.setView(0, 0, 25);
    mapPanel.setMode(MapPanel.SURFACE);
    mapPanel.setPreferredSize(new Dimension(1000, 1000));
    setBounds(0, 0, 400, 400);

    layerSlider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
    layerSlider.setPaintTicks(true);
    add(layerSlider);
    layerSlider.addChangeListener((ChangeEvent e) -> mapPanel.setLayer(layerSlider.getValue()));

    scaleSlider = new JSlider(JSlider.VERTICAL, 1, 10, initialZoom);
    scaleSlider.setPaintTicks(true);
    add(scaleSlider);
    scaleSlider.addChangeListener(
        (ChangeEvent e) -> mapPanel.setScale((double) scaleSlider.getValue() / 5.0D));
    mapPanel.setScale(initialZoom / 5.0D);

    statusPanel = new JPanel();
    statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
    coordinateLabel = new JLabel();
    statusPanel.add(coordinateLabel);
    statusPanel.setVisible(true);
    mapPanel.addBlockListener(
        (BlockEvent e) ->
            coordinateLabel.setText(
                e.getX()
                    + ","
                    + e.getY()
                    + ","
                    + e.getZ()
                    + " - "
                    + blockMapper.getBlockName(e.getType())));

    // Define layout
    //
    layout.setHorizontalGroup(
        layout
            .createParallelGroup()
            .addGroup(
                layout
                    .createParallelGroup()
                    .addComponent(toolbar)
                    .addGroup(
                        layout
                            .createSequentialGroup()
                            .addComponent(mapPanel)
                            .addGroup(layout.createParallelGroup())
                            .addComponent(layerSlider)
                            .addComponent(scaleSlider)))
            .addComponent(statusPanel));
    layout.setVerticalGroup(
        layout
            .createSequentialGroup()
            .addGroup(
                layout
                    .createSequentialGroup()
                    .addComponent(toolbar)
                    .addGroup(
                        layout
                            .createParallelGroup()
                            .addComponent(mapPanel)
                            .addComponent(layerSlider)
                            .addComponent(scaleSlider)))
            .addComponent(statusPanel));

    layerSlider.setVisible(true);
    // mapPanel.setHighlight(58);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  protected JToggleButton makeNavigationButton(
      String imageName, String actionCommand, String toolTipText, String altText) {

    String imgLocation = "/" + imageName;
    URL imageURL = getClass().getResource(imgLocation);
    JToggleButton button = new JToggleButton();
    button.setActionCommand(actionCommand);
    button.setToolTipText(toolTipText);
    button.addActionListener(toolbarListener);

    if (imgLocation != null) { // image found
      button.setIcon(new ImageIcon(imageURL, altText));
    } else { // no image found
      button.setText(altText);
      System.err.println("Resource not found: " + imgLocation);
    }

    return button;
  }

  protected void openFileDialog() {
    JFileChooser fc = new JFileChooser(workingDirectory);
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int status = fc.showOpenDialog(this);
    if (status == JFileChooser.APPROVE_OPTION) {
      openDirectory(fc.getSelectedFile());
    }
  }

  public void openDirectory(File dir) {
    chunkMgr = new DataManager(dir, blockMapper);
    mapPanel.setChunkManager(chunkMgr);
    mapPanel.setView(0, 0, layerSlider.getValue());
  }

  protected void openWorld() {}
}
