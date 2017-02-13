package net.sharkfw.security;

import net.sharkfw.asip.ASIPInformation;
import net.sharkfw.asip.ASIPInformationSpace;
import net.sharkfw.asip.ASIPInterest;
import net.sharkfw.asip.ASIPSpace;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.security.utilities.SharkSign;
import net.sharkfw.system.KnowledgeUtils;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by j4rvis on 2/7/17.
 */
public class SharkPkiStorage implements PkiStorage {

    public static final String INFO_OWNER_PRIVATE_KEY = "INFO_OWNER_PRIVATE_KEY";
    public static final String INFO_OWNER_PUBLIC_KEY = "INFO_OWNER_PUBLIC_KEY";
    public static final String INFO_OLD_OWNER_PRIVATE_KEY = "INFO_OLD_OWNER_PRIVATE_KEY";
    public static final String INFO_OLD_OWNER_PUBLIC_KEY = "INFO_OLD_OWNER_PUBLIC_KEY";

    private final SharkKB kb;
    private PeerSemanticTag owner;
    private KeyFactory keyFactory;

    private ASIPSpace certificateSpace = null;
    private ASIPSpace ownerSpace = null;

    public SharkPkiStorage(SharkKB kb) {
        this.kb = kb;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Generate the basic certificate space
        STSet typeSet = InMemoSharkKB.createInMemoSTSet();
        try {
            typeSet.merge(SharkCertificate.CERTIFICATE_TAG);
            certificateSpace = InMemoSharkKB.createInMemoASIPInterest(
                    null,
                    typeSet,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ASIPSpace.DIRECTION_NOTHING);
        } catch (SharkKBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPkiStorageOwner(PeerSemanticTag owner) {
        this.owner = owner;
        // generate the owner space
        STSet set = InMemoSharkKB.createInMemoSTSet();
        try {
            set.merge(PkiStorage.OWNERSPACE_TAG);
            ownerSpace = InMemoSharkKB.createInMemoASIPInterest(
                    null,
                    set,
                    owner,
                    null,
                    null,
                    null,
                    null,
                    ASIPSpace.DIRECTION_NOTHING
            );
        } catch (SharkKBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SharkPublicKey> getUnsignedPublicKeys() throws SharkKBException {
        return this.getSharkPublicKeysBySpace(InMemoSharkKB.createInMemoASIPInterest());
    }

    @Override
    public SharkCertificate sign(SharkPublicKey sharkPublicKey) throws SharkKBException {
        byte[] sign = SharkSign.sign(
                sharkPublicKey.getOwnerPublicKey().getEncoded(),
                this.getOwnerPrivateKey(),
                SharkSign.SharkSignatureAlgorithm.SHA1withRSA);

        long signingDate = System.currentTimeMillis();

        // TODO wird der alter Space genutzt und somit die Änderungen überschrieben? Oder bleibt der alte Key erhalten?
        return new ASIPSpaceSharkCertificate(
                this.kb,
                sharkPublicKey.getOwner(),
                sharkPublicKey.getOwnerPublicKey(),
                sharkPublicKey.getValidity(),
                this.owner,
                sign,
                signingDate);
    }

    @Override
    public PrivateKey getOwnerPrivateKey() throws SharkKBException {
        ASIPInformation information = KnowledgeUtils.getInfoByName(this.kb, this.ownerSpace, INFO_OWNER_PRIVATE_KEY);
        if (information != null) {
            try {
                return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(information.getContentAsByte()));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * TODO necessary?
     *
     * @param newPrivateKey {@link PrivateKey}
     * @throws SharkKBException
     */
    @Override
    @Deprecated
    public void setOwnerPrivateKey(PrivateKey newPrivateKey) throws SharkKBException {

    }

    @Override
    public PrivateKey getOldOwnerPrivateKey() throws SharkKBException {
        ASIPInformation information = KnowledgeUtils.getInfoByName(this.kb, this.ownerSpace, INFO_OLD_OWNER_PRIVATE_KEY);
        if (information != null) {
            try {
                return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(information.getContentAsByte()));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public PublicKey getOwnerPublicKey() throws SharkKBException {
        ASIPInformation information = KnowledgeUtils.getInfoByName(this.kb, this.ownerSpace, INFO_OWNER_PUBLIC_KEY);
        if (information != null) {
            try {
                return keyFactory.generatePublic(new X509EncodedKeySpec(information.getContentAsByte()));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public PublicKey getOldOwnerPublicKey() throws SharkKBException {
        ASIPInformation information = KnowledgeUtils.getInfoByName(this.kb, this.ownerSpace, INFO_OLD_OWNER_PUBLIC_KEY);
        if (information != null) {
            try {
                return keyFactory.generatePublic(new X509EncodedKeySpec(information.getContentAsByte()));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void generateNewKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA/ECB/PKCS1Padding");
            keyPairGenerator.initialize(4096);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // replace the old keys with the current keys
            KnowledgeUtils.setInfoWithName(this.kb, this.ownerSpace, INFO_OLD_OWNER_PRIVATE_KEY, getOwnerPrivateKey().getEncoded());
            KnowledgeUtils.setInfoWithName(this.kb, this.ownerSpace, INFO_OLD_OWNER_PUBLIC_KEY, getOwnerPublicKey().getEncoded());

            // now replace the current keys with the new created keys
            KnowledgeUtils.setInfoWithName(this.kb, this.ownerSpace, INFO_OWNER_PRIVATE_KEY, keyPair.getPrivate().getEncoded());
            KnowledgeUtils.setInfoWithName(this.kb, this.ownerSpace, INFO_OWNER_PUBLIC_KEY, keyPair.getPublic().getEncoded());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SharkKBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SharkCertificate> getSharkCertificates(PeerSemanticTag owner) throws SharkKBException {
        ASIPInterest interest = InMemoSharkKB.createInMemoASIPInterest(
                (STSet) null,
                null,
                owner,
                null,
                null,
                null,
                null,
                ASIPSpace.DIRECTION_INOUT
        );
        return this.getSharkCertificatesBySpace(interest);
    }

    @Override
    public SharkCertificate getSharkCertificate(PeerSemanticTag owner, PeerSemanticTag signer) throws SharkKBException {
        PeerSTSet set = InMemoSharkKB.createInMemoPeerSTSet();
        set.merge(signer);
        ASIPInterest interest = InMemoSharkKB.createInMemoASIPInterest(
                (STSet) null,
                null,
                owner,
                set,
                null,
                null,
                null,
                ASIPSpace.DIRECTION_INOUT
        );
        List<SharkCertificate> certificatesBySpace = this.getSharkCertificatesBySpace(interest);
        if (certificatesBySpace.size() > 0) {
            return certificatesBySpace.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<SharkCertificate> getSignedSharkCertificates(PeerSemanticTag signer) throws SharkKBException {
        PeerSTSet set = InMemoSharkKB.createInMemoPeerSTSet();
        set.merge(signer);
        ASIPInterest interest = InMemoSharkKB.createInMemoASIPInterest(
                (STSet) null,
                null,
                null,
                set,
                null,
                null,
                null,
                ASIPSpace.DIRECTION_INOUT
        );
        return this.getSharkCertificatesBySpace(interest);
    }

    @Override
    public boolean addSharkCertificate(SharkCertificate certificate) throws SharkKBException {
        return false;
    }

    @Override
    public SharkCertificate addSharkCertificate(PeerSemanticTag owner, PublicKey ownerKey, long validity,
                                                PeerSemanticTag signer, byte[] signature, long signingDate) {

        return new ASIPSpaceSharkCertificate(this.kb, owner, ownerKey, validity, signer, signature, signingDate);
    }

    @Override
    public List<SharkCertificate> getAllSharkCertificates() throws SharkKBException {
        return this.getSharkCertificatesBySpace(certificateSpace);
    }

    @Override
    public boolean deleteSharkCertificate(SharkCertificate certificate) throws SharkKBException {

        return false;
    }

    // TODO ??? whats the data?? used PubKey
    @Override
    public boolean verifySharkCertificate(SharkCertificate certificate, PeerSemanticTag signer) {

        try {
            // Iterate through all possible certificates to retrieve a valid pubkey from the signer
            Iterator<SharkCertificate> iterator = getSharkCertificates(signer).iterator();
            while (iterator.hasNext()) {
                SharkCertificate next = iterator.next();

                boolean verify = SharkSign.verify(certificate.getOwnerPublicKey().getEncoded(), certificate.getSignature(), next.getOwnerPublicKey(), SharkSign.SharkSignatureAlgorithm.SHA1withRSA);
                if (verify) return true;
            }
        } catch (SharkKBException e) {
            e.printStackTrace();
        }

        return false;
    }

    private List<SharkCertificate> getSharkCertificatesBySpace(ASIPSpace space) throws SharkKBException {
        List<SharkCertificate> resultSet = new ArrayList<>();

        Iterator<ASIPInformation> informationIterator = this.kb.getInformation(space, true, false);

        resultSet.add(new ASIPSpaceSharkCertificate(this.kb, informationIterator));



//        Iterator<ASIPInformationSpace> informationSpaces = this.kb.getInformationSpaces(space);
//        while (informationSpaces.hasNext()) {
//            ASIPInformationSpace next = informationSpaces.next();
//            // now we need to differentiate the certificates from the simple unsigned pub keys.
//            // the difference between these two is the existence of a set of approvers.
//            // certificates has a list off approvers/signers
//            ASIPSpace nextASIPSpace = next.getASIPSpace();
//            PeerSTSet approvers = nextASIPSpace.getApprovers();
//            if (approvers != null) {
//                // Now create a certificate for each approver
//                Enumeration<PeerSemanticTag> peerSemanticTagEnumeration = approvers.peerTags();
//                while (peerSemanticTagEnumeration.hasMoreElements()) {
//                    PeerSemanticTag peerSemanticTag = peerSemanticTagEnumeration.nextElement();
//                    resultSet.add(new ASIPSpaceSharkCertificate(this.kb, nextASIPSpace, peerSemanticTag));
//                }
//            }
//        }
        return resultSet;
    }

    private List<SharkPublicKey> getSharkPublicKeysBySpace(ASIPSpace space) throws SharkKBException {
        List<SharkPublicKey> resultSet = new ArrayList<>();

        Iterator<ASIPInformationSpace> informationSpaces = this.kb.getInformationSpaces(space);
        while (informationSpaces.hasNext()) {
            ASIPInformationSpace next = informationSpaces.next();
            // now we need to differentiate the certificates from the simple unsigned pub keys.
            // the difference between these two is the existence of a set of approvers.
            // certificates has a list off approvers/signers
            ASIPSpace nextASIPSpace = next.getASIPSpace();
            PeerSTSet approvers = nextASIPSpace.getApprovers();
            if (approvers == null) {
                resultSet.add(new ASIPSpaceSharkPublicKey(this.kb, nextASIPSpace));
            }
        }
        return resultSet;
    }
}