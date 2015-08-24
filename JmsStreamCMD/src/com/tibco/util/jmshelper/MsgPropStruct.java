/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

package com.tibco.util.jmshelper;

import java.util.ArrayList;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "UnusedDeclaration", "unused"})
final public class MsgPropStruct
{
    @SuppressWarnings("unused")
    public enum ValueType { Boolean, Byte, Bytes, Char, Double, Float, Integer, Short, Long, Number, String, ArrayList }
    private String name = null;
    private String value = null;
    private ValueType type = null;
    private ArrayList array = null;

    public MsgPropStruct(String name, ValueType type, String value) {
        this.name = name;
        this.value = value;
        this.type = type;
    }
    public MsgPropStruct(String name, String type, String value) {
        this.name = name;
        this.value = value;
        this.type = ValueType.valueOf(type);
    }
    public MsgPropStruct(String name, ArrayList array) {
        this.name = name;
        this.array = array;
        this.type = ValueType.ArrayList;
    }
    public String getName() { return name; }
    public String getValue() { return value; }
    public ValueType getType() { return type; }
    public ArrayList getArray() { return array; }
}
