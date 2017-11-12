/*
 * This file is generated by jOOQ.
*/
package rr.industries.jooq.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.TableRecordImpl;

import rr.industries.jooq.tables.Greetingtable;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GreetingtableRecord extends TableRecordImpl<GreetingtableRecord> implements Record3<String, String, String> {

    private static final long serialVersionUID = 33110167;

    /**
     * Setter for <code>greetingtable.guildid</code>.
     */
    public void setGuildid(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>greetingtable.guildid</code>.
     */
    public String getGuildid() {
        return (String) get(0);
    }

    /**
     * Setter for <code>greetingtable.joinmessage</code>.
     */
    public void setJoinmessage(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>greetingtable.joinmessage</code>.
     */
    public String getJoinmessage() {
        return (String) get(1);
    }

    /**
     * Setter for <code>greetingtable.leavemessage</code>.
     */
    public void setLeavemessage(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>greetingtable.leavemessage</code>.
     */
    public String getLeavemessage() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<String, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<String, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return Greetingtable.GREETINGTABLE.GUILDID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Greetingtable.GREETINGTABLE.JOINMESSAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Greetingtable.GREETINGTABLE.LEAVEMESSAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component1() {
        return getGuildid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getJoinmessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getLeavemessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getGuildid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getJoinmessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getLeavemessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GreetingtableRecord value1(String value) {
        setGuildid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GreetingtableRecord value2(String value) {
        setJoinmessage(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GreetingtableRecord value3(String value) {
        setLeavemessage(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GreetingtableRecord values(String value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached GreetingtableRecord
     */
    public GreetingtableRecord() {
        super(Greetingtable.GREETINGTABLE);
    }

    /**
     * Create a detached, initialised GreetingtableRecord
     */
    public GreetingtableRecord(String guildid, String joinmessage, String leavemessage) {
        super(Greetingtable.GREETINGTABLE);

        set(0, guildid);
        set(1, joinmessage);
        set(2, leavemessage);
    }
}