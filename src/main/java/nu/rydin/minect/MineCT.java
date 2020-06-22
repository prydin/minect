package nu.rydin.minect;

import org.apache.commons.cli.*;

import java.io.File;

public class MineCT {
  public static final void main(String[] args) throws Exception {

    Options options = new Options();

    Option input = new Option("d", "directory", true, "region file directory");
    input.setRequired(true);
    options.addOption(input);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("minect", options);
      System.exit(1);
    }
    String worldDir = cmd.getOptionValue("directory");

    String workingDirectory;
    String os = (System.getProperty("os.name")).toUpperCase();
    if (os.contains("WIN")) {
      workingDirectory = System.getenv("AppData");
    } else {
      workingDirectory = System.getProperty("user.home");
    }
    File mcRoot = new File(new File(workingDirectory, ".minecraft"), "saves");
    MineCTApplication app = new MineCTApplication(mcRoot);
    if (worldDir != null) {
      app.openDirectory(new File(worldDir));
    }
    app.setVisible(true);
  }
}
