package net.sharkfw.knowledgeBase.persistent.dump;

import net.sharkfw.knowledgeBase.SystemPropertyHolder;

/**
 * Created by j4rvis on 2/28/17.
 */
public class DumpSystemPropertyHolder extends DumpPropertyHolder implements SystemPropertyHolder{
    private final SystemPropertyHolder systemPropertyHolder;

    public DumpSystemPropertyHolder(DumpSharkKB dumpSharkKB, SystemPropertyHolder systemPropertyHolder) {
        super(dumpSharkKB, systemPropertyHolder);
        this.systemPropertyHolder = systemPropertyHolder;
    }

    @Override
    public void setSystemProperty(String name, String value) {
        this.systemPropertyHolder.setSystemProperty(name, value);
        this.kb.persist();
    }

    @Override
    public String getSystemProperty(String name) {
        return this.systemPropertyHolder.getSystemProperty(name);
    }
}
