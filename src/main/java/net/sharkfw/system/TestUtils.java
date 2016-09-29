package net.sharkfw.system;

import net.sharkfw.asip.ASIPSpace;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by j4rvis on 9/29/16.
 */
public class TestUtils {

    private static SecureRandom random;

    static {
        random = new SecureRandom();
    }

    /**
     * Checks if the given Integer is 0 than sets it to 1 as the minimum.
     * This should ensure that the minimum size of an array or a Set is 1
     * @param length
     * @return the size of length or 1 if length was 0
     */
    private static int checkLength(int length){
        return length == 0 ? 1 : length;
    }

    /**
     * Generates a random String with a given size
     * @param length
     * @return random String
     */
    private static String getRandomString(int length) {
        return new BigInteger(130, random).toString(32).substring(0, checkLength(length));
    }

    /**
     * Create a String array with a give size. Strings created withing this array will be of a size between 1 and 10.
     * @param length of the array
     * @return String array filled with random strings
     */
    private static String[] createStringArray(int length) {
        // If length equals 0, set it to 1
        int numberOfSI = checkLength(length);
        String[] sis = new String[numberOfSI];
        for (int i = 0; i < numberOfSI; i++) {
            sis[i] = getRandomString(random.nextInt(11));
        }
        return sis;
    }

    /**
     * Creates a SemanticTag with random name and String array of random sis.
     * @return SemanticTag
     */
    public static SemanticTag createRandomSemanticTag() {
        String name = getRandomString(random.nextInt(11));
        String[] sis = createStringArray(random.nextInt(6));
        return InMemoSharkKB.createInMemoSemanticTag(name, sis);
    }

    /**
     * Creates a PeerSemanticTag with random name and String array of random sis and also a String array of
     * random addresses with a size between 1 and 5.
     * @return SemanticTag
     */
    public static PeerSemanticTag createRandomPeerSemanticTag() {
        PeerSemanticTag tag = (PeerSemanticTag) createRandomSemanticTag();
        tag.setAddresses(createStringArray(random.nextInt(5)));
        return tag;
    }

    /**
     * Create a STSet filled with random SemanticTags.
     * @param length defines the length/size of the STSet
     * @return STSet
     */
    public static STSet createRandomSTSet(int length) {
        STSet set = InMemoSharkKB.createInMemoSTSet();
        length = checkLength(length);
        for (int i = 0; i < length; i++) {
            try {
                set.merge(createRandomSemanticTag());
            } catch (SharkKBException e) {
                e.printStackTrace();
            }
        }
        return set;
    }

    /**
     * Create a STSet filled random SemanticTags.
     * The size will be between 1 and 10;
     * @return STSet
     */
    public static STSet createRandomSTSet(){
        return createRandomSTSet(random.nextInt(10));
    }

    /**
     * Create a PeerSTSet filled with random PeerSemanticTags.
     * @param length defines the length/size of the PeerSTSet
     * @return PeerSTSet
     */
    public static PeerSTSet createRandomPeerSTSet(int length) {
        PeerSTSet set = InMemoSharkKB.createInMemoPeerSTSet();
        length = checkLength(length);
        for (int i = 0; i < length; i++) {
            try {
                set.merge(createRandomPeerSemanticTag());
            } catch (SharkKBException e) {
                e.printStackTrace();
            }
        }
        return set;
    }

    /**
     * Create a PeerSTSet filled random PeerSemanticTags.
     * The size will be between 1 and 10;
     * @return PeerSTSet
     */
    public static PeerSTSet createRandomPeerSTSet(){
        return createRandomPeerSTSet(random.nextInt(11));
    }

    /**
     * This method will return an ASIPSpace with random Tags and Sets
     * TODO Add Spatial and TimeTag support
     * @return ASIPSpace
     */
    public static ASIPSpace createRandomASIPSpace() throws SharkKBException {
        return InMemoSharkKB.createInMemoASIPInterest(
                createRandomSTSet(),
                createRandomSTSet(),
                createRandomPeerSemanticTag(),
                createRandomPeerSTSet(),
                createRandomPeerSTSet(),
                null,
                null,
                random.nextInt(4)
        );
    }
}
