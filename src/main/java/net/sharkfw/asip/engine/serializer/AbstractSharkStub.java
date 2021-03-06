package net.sharkfw.asip.engine.serializer;

import net.sharkfw.asip.SharkStub;
import net.sharkfw.knowledgeBase.Interest;
import net.sharkfw.peer.ASIPPort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.ports.KnowledgePort;
import net.sharkfw.protocols.StreamConnection;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author thsc
 */
public abstract class AbstractSharkStub implements SharkStub {
    private final List<ASIPPort> ports = new ArrayList<>();
    protected KnowledgePort notHandledRequestsHandler;
    protected SharkEngine se;
    private final HashMap<String, StreamConnection> table = new HashMap<>();

    public AbstractSharkStub(SharkEngine se) {
        this.se = se;
    }

    @Override
    public final void addListener(ASIPPort newListener) {
        // already in there?
        Iterator<ASIPPort> kpIter = ports.iterator();
        while (kpIter.hasNext()) {
            if (newListener == kpIter.next()) {
                return; // already in - do nothing
            }
        }

        // not found - add
        this.ports.add(newListener);
    }

    ;

    @Override
    public final void withdrawListener(ASIPPort listener) {
        this.ports.remove(listener);
    }

    ;

    @Override
    public Iterator<ASIPPort> getListener() {
        return this.ports.iterator();
    }

    // security_deprecated stuff
    //protected SharkPublicKeyStorage publicKeyStorage;
//    protected SharkPkiStorage sharkPkiStorage;
    protected SharkEngine.SecurityReplyPolicy replyPolicy;
    protected boolean refuseUnverifiably;
    protected SharkEngine.SecurityLevel signatureLevel = SharkEngine.SecurityLevel.IF_POSSIBLE;
    protected SharkEngine.SecurityLevel encryptionLevel = SharkEngine.SecurityLevel.IF_POSSIBLE;
    protected PrivateKey privateKey;

    @Override
    public void initSecurity(PrivateKey privateKey, /*SharkPublicKeyStorage publicKeyStorage,*/ /*SharkPkiStorage sharkPkiStorage,*/
                             SharkEngine.SecurityLevel encryptionLevel, SharkEngine.SecurityLevel signatureLevel,
                             SharkEngine.SecurityReplyPolicy replyPolicy, boolean refuseUnverifiably) {

        this.privateKey = privateKey;
        //this.publicKeyStorage = publicKeyStorage;
//        this.sharkPkiStorage = sharkPkiStorage;
        this.signatureLevel = signatureLevel;
        this.encryptionLevel = encryptionLevel;
        this.replyPolicy = replyPolicy;
        this.refuseUnverifiably = refuseUnverifiably;
    }

    @Override
    public void handleInterest(Interest interest) {
        // TODO or remove: default implementation
    }
}
