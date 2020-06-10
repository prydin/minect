package nu.rydin.minect;

import nu.rydin.minect.data.ArbitraryWordArray;
import org.junit.Assert;
import org.junit.Test;

public class TestArbitraryWordArray {

  @Test
  public void testPutAndGet() {
    ArbitraryWordArray nba = new ArbitraryWordArray(512, 9);
    for (int idx = 0; idx < 512; ++idx) {
      nba.put(idx, idx);
      Assert.assertEquals("TestArbitraryWordArray get (9 bits)", idx, nba.get(idx));
    }

    nba = new ArbitraryWordArray(131072, 17);
    for (int idx = 0; idx < 131071; ++idx) {
      nba.put(idx, idx);
      Assert.assertEquals("NineBitArray get (17 bits)", idx, nba.get(idx));
    }
  }
}
