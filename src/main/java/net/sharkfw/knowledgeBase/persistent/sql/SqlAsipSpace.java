package net.sharkfw.knowledgeBase.persistent.sql;

import net.sharkfw.asip.ASIPSpace;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

public class SqlAsipSpace implements ASIPSpace {

    private STSet topics;
    private STSet types;
    private PeerSTSet approvers;
    private PeerSTSet receivers;
    private PeerSemanticTag sender;
    private SpatialSTSet locations;
    private TimeSTSet times;
    private int direction = DIRECTION_INOUT;
    private SqlAsipInformationSpace sqlAsipInformationSpace;
    private SqlSharkKB sqlSharkKB;

    protected SqlAsipSpace() {
//        try {
//            topics = new SqlSTSet(sqlSharkKB, "TOPIC", sqlAsipInformationSpace);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        topics = InMemoSharkKB.createInMemoSTSet();
        types= InMemoSharkKB.createInMemoSTSet();
        approvers = InMemoSharkKB.createInMemoPeerSTSet();
        receivers = InMemoSharkKB.createInMemoPeerSTSet();
        locations = InMemoSharkKB.createInMemoSpatialSTSet();
        times = InMemoSharkKB.createInMemoTimeSTSet();
    }

    protected void addInformationSpace(SqlAsipInformationSpace sqlAsipInformationSpace){
        this.sqlAsipInformationSpace = sqlAsipInformationSpace;
    }

    protected void addSharkKb(SqlSharkKB sqlSharkKB){
        this.sqlSharkKB = sqlSharkKB;
    }

    protected void addTag(SemanticTag tag, int type) throws SharkKBException {
        switch (type){
            case DIM_TOPIC:
                topics.merge(tag);
                break;
            case DIM_TYPE:
                types.merge(tag);
                break;
            case DIM_APPROVERS:
                approvers.merge(tag);
                break;
            case DIM_SENDER:
                sender = (PeerSemanticTag) tag;
                break;
            case DIM_RECEIVER:
                receivers.merge(tag);
                break;
            case DIM_TIME:
                times.merge(tag);
                break;
            case DIM_LOCATION:
                locations.merge(tag);
                break;
        }
    }

    @Override
    public STSet getTopics() {
        return topics;
    }

    @Override
    public STSet getTypes() {
        return types;
    }

    @Override
    public int getDirection() {
        return direction;
    }

    @Override
    public PeerSemanticTag getSender() {
        return sender;
    }

    @Override
    public PeerSTSet getReceivers() {
        return receivers;
    }

    @Override
    public PeerSTSet getApprovers() {
        return approvers;
    }

    @Override
    public TimeSTSet getTimes() {
        return times;
    }

    @Override
    public SpatialSTSet getLocations() {
        return locations;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
