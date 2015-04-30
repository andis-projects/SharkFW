package net.sharkfw.security.keystorage.filesystem;

import net.sharkfw.security.SharkKeyPairAlgorithm;
import net.sharkfw.security.keystorage.SharkKeyStorage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author ac
 */
public class FSSharkKeyStorageTest {

    private static String filePath;
    private static FSSharkKeyStorage fsSharkKeyStorage;
    private static SharkKeyStorage sharkKeyStorage;

    @BeforeClass
    public static void setUp() throws Exception {
        filePath = "KeyStorage.shark";
        fsSharkKeyStorage = new FSSharkKeyStorage(filePath);
        sharkKeyStorage = new SharkKeyStorage();
        sharkKeyStorage.setPrivateKey(null);
        sharkKeyStorage.setPublicKey(null);
        sharkKeyStorage.setSharkKeyPairAlgorithm(SharkKeyPairAlgorithm.RSA);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        new File(filePath).delete();
    }

    @Test
    public void testSave() throws Exception {
        assertTrue(fsSharkKeyStorage.save(sharkKeyStorage));
    }

    @Test
    public void testLoad() throws Exception {

        boolean isEqual = false;
        SharkKeyStorage sks = fsSharkKeyStorage.load();

        if(sks == null) {
            testSave();
            sks = fsSharkKeyStorage.load();
        }

        if (    sks.getPublicKey() == sharkKeyStorage.getPublicKey() &&
                sks.getPrivateKey() == sharkKeyStorage.getPrivateKey() &&
                sks.getSharkKeyPairAlgorithm() == sharkKeyStorage.getSharkKeyPairAlgorithm())
        {
            isEqual = true;
        }

        assertEquals(true, isEqual);
    }
}