package nu.rydin.minect;

public interface JobGenerationKeeper {
  int getJobGeneration();

  int incrementJobGeneration();
}
