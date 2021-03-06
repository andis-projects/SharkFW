package net.sharkfw.asip.engine;

import net.sharkfw.asip.ASIPInterest;
import net.sharkfw.asip.ASIPKnowledge;
import net.sharkfw.asip.engine.serializer.SharkProtocolNotSupportedException;
import net.sharkfw.asip.serialization.ASIPMessageSerializer;
import net.sharkfw.asip.serialization.ASIPSerializationHolder;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.protocols.MessageStub;
import net.sharkfw.protocols.Protocols;
import net.sharkfw.protocols.StreamConnection;
import net.sharkfw.system.L;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Objects of this class are produced by the framework in order
 * to be serialized and transmitted to another peer.
 *
 * @author thsc
 */
public class ASIPOutMessage extends ASIPMessage {

    private Writer osw = null;
    private OutputStream os = null;
    private boolean responseSent = false;
    private String recipientAddress = "";
    private MessageStub outStub;

    public ASIPOutMessage(SharkEngine engine,
                          StreamConnection connection,
                          long ttl,
                          PeerSemanticTag physicalSender,
                          PeerSemanticTag logicalSender,
                          PeerSemanticTag receiverPeer,
                          SpatialSemanticTag receiverLocation,
                          TimeSemanticTag receiverTime,
                          SemanticTag topic,
                          SemanticTag type) throws SharkKBException {

        super(engine, connection, ttl, physicalSender, logicalSender, receiverPeer, receiverLocation, receiverTime, topic, type);
        this.recipientAddress = connection.getReceiverAddressString();
        this.os = connection.getOutputStream();
    }

    public ASIPOutMessage(SharkEngine engine, StreamConnection connection, ASIPInMessage in, SemanticTag topic, SemanticTag type) throws SharkKBException {
        super(engine, connection, (in.getTtl() - 1), engine.getOwner(), in.getLogicalSender(), in.getPhysicalSender(), in.getReceiverSpatial(), in.getReceiverTime(), topic, type);
        this.recipientAddress = connection.getReceiverAddressString();
        this.os = connection.getOutputStream();
    }

    public ASIPOutMessage(SharkEngine engine,
                          MessageStub stub,
                          long ttl,
                          PeerSemanticTag physicalSender,
                          PeerSemanticTag logicalSender,
                          PeerSemanticTag receiverPeer,
                          SpatialSemanticTag receiverLocation,
                          TimeSemanticTag receiverTime,
                          SemanticTag topic,
                          SemanticTag type,
                          String address) throws SharkKBException {

        super(engine, stub, ttl, physicalSender, logicalSender, receiverPeer, receiverLocation, receiverTime, topic, type);
        this.outStub = stub;
        this.recipientAddress = address;
        this.os = new ByteArrayOutputStream();
    }

    public ASIPOutMessage(SharkEngine engine, MessageStub stub, ASIPInMessage in, SemanticTag topic, SemanticTag type) throws SharkKBException {
        super(engine, stub, (in.getTtl() - 1), engine.getOwner(), in.getLogicalSender(), in.getPhysicalSender(), in.getReceiverSpatial(), in.getReceiverTime(), topic, type);
        this.outStub = stub;
        for (String s : in.getPhysicalSender().getAddresses()) {
            try {
                if(!Protocols.isStreamProtocol(Protocols.getValueByAddress(s))){
                    this.recipientAddress = s;
                }
            } catch (SharkProtocolNotSupportedException e) {
                e.printStackTrace();
            }
        }
        this.os = new ByteArrayOutputStream();
    }


    public boolean responseSent() {
        return this.responseSent;
    }

    private void sent() {

        try {
            this.os.flush();
            if (outStub != null) {
                final byte[] msg = ((ByteArrayOutputStream) this.os).toByteArray();
                this.outStub.sendMessage(msg, this.recipientAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.responseSent = true;
    }

    public void expose(ASIPInterest interest) {
        this.setCommand(ASIPMessage.ASIP_EXPOSE);
        try {
            ASIPSerializationHolder holder = ASIPMessageSerializer.serializeExpose(this, interest);
            L.d("Sending an Expose with the complete Size of " + holder.length() + " Bytes", this);
            this.os.write(holder.messageAsUtf8Bytes());
        } catch (SharkKBException | IOException e) {
            e.printStackTrace();
        }
        this.sent();
    }

    public void insert(ASIPKnowledge knowledge) {
        this.setCommand(ASIPMessage.ASIP_INSERT);
        try {
            ASIPSerializationHolder holder = ASIPMessageSerializer.serializeInsert(this, knowledge);
            L.d("Sending an Insert with the complete Size of " + holder.length() + " Bytes", this);
            this.os.write(holder.messageAsUtf8Bytes());
            this.os.write(holder.getContent());
        } catch (SharkKBException | IOException e) {
            e.printStackTrace();
        }
        this.sent();
    }

    public void raw(byte[] raw) {
        this.setCommand(ASIPMessage.ASIP_RAW);
        try {
            ASIPSerializationHolder holder = ASIPMessageSerializer.serializeRaw(this, raw);
            L.d("Sending a Raw with the complete Size of " + holder.length() + " Bytes", this);
            this.os.write(holder.messageAsUtf8Bytes());
            this.os.write(holder.getContent());
        } catch (SharkKBException | IOException e) {
            e.printStackTrace();
        }
        this.sent();
    }

    public void raw(InputStream inputStream) {
        this.setCommand(ASIPMessage.ASIP_RAW);
        try {
            ASIPSerializationHolder holder = ASIPMessageSerializer.serializeRaw(this, inputStream);
            L.d("Sending a Raw with the complete Size of " + holder.length() + " Bytes", this);
            this.os.write(holder.messageAsUtf8Bytes());
            this.os.write(holder.getContent());
        } catch (SharkKBException | IOException e) {
            e.printStackTrace();
        }
        this.sent();
    }

}
