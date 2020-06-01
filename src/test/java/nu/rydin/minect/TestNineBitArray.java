package nu.rydin.minect;

import nu.rydin.minect.data.NineBitArray;
import org.junit.Assert;
import org.junit.Test;

public class TestNineBitArray {

    @Test
    public void testPutAndGet() {
        NineBitArray nba = new NineBitArray(512);
        for(int idx = 0; idx < 512; ++idx) {
            nba.put(idx, idx);
            Assert.assertEquals("NineBitArray get", idx, nba.get(idx));
        }
    }
}
