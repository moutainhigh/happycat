/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.woniu.sncp.cbss.api.core.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
/**
 * 服务端状态
 */
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-07-14")
public class State implements org.apache.thrift.TBase<State, State._Fields>, java.io.Serializable, Cloneable, Comparable<State> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("State");

  private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField FUTURETIME_FIELD_DESC = new org.apache.thrift.protocol.TField("futuretime", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField DOMANENAME_FIELD_DESC = new org.apache.thrift.protocol.TField("domanename", org.apache.thrift.protocol.TType.STRING, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new StateStandardSchemeFactory());
    schemes.put(TupleScheme.class, new StateTupleSchemeFactory());
  }

  /**
   * 
   * @see Status
   */
  public Status status; // required
  /**
   * 当status为SERVER_FUTURE_STOPED或SERVER_FUTURE_MAINTAIN时，此值会出现一个时间点格式:yyyy-MM-dd HH:mm:ss,表示在此时间点会进行维护或停服务
   */
  public String futuretime; // required
  /**
   * 当status为DOMAINNAME_CHANGE时，此值一个新域名或逗号分隔的多个域名，使用人按照顺序逐个调用直到调用成功或每个都使用过，如域名:a.b.c,a1.b.c,a2.b.c,表示3个域名轮询调用
   */
  public String domanename; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * 
     * @see Status
     */
    STATUS((short)1, "status"),
    /**
     * 当status为SERVER_FUTURE_STOPED或SERVER_FUTURE_MAINTAIN时，此值会出现一个时间点格式:yyyy-MM-dd HH:mm:ss,表示在此时间点会进行维护或停服务
     */
    FUTURETIME((short)2, "futuretime"),
    /**
     * 当status为DOMAINNAME_CHANGE时，此值一个新域名或逗号分隔的多个域名，使用人按照顺序逐个调用直到调用成功或每个都使用过，如域名:a.b.c,a1.b.c,a2.b.c,表示3个域名轮询调用
     */
    DOMANENAME((short)3, "domanename");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // STATUS
          return STATUS;
        case 2: // FUTURETIME
          return FUTURETIME;
        case 3: // DOMANENAME
          return DOMANENAME;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, Status.class)));
    tmpMap.put(_Fields.FUTURETIME, new org.apache.thrift.meta_data.FieldMetaData("futuretime", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.DOMANENAME, new org.apache.thrift.meta_data.FieldMetaData("domanename", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(State.class, metaDataMap);
  }

  public State() {
  }

  public State(
    Status status,
    String futuretime,
    String domanename)
  {
    this();
    this.status = status;
    this.futuretime = futuretime;
    this.domanename = domanename;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public State(State other) {
    if (other.isSetStatus()) {
      this.status = other.status;
    }
    if (other.isSetFuturetime()) {
      this.futuretime = other.futuretime;
    }
    if (other.isSetDomanename()) {
      this.domanename = other.domanename;
    }
  }

  public State deepCopy() {
    return new State(this);
  }

  @Override
  public void clear() {
    this.status = null;
    this.futuretime = null;
    this.domanename = null;
  }

  /**
   * 
   * @see Status
   */
  public Status getStatus() {
    return this.status;
  }

  /**
   * 
   * @see Status
   */
  public State setStatus(Status status) {
    this.status = status;
    return this;
  }

  public void unsetStatus() {
    this.status = null;
  }

  /** Returns true if field status is set (has been assigned a value) and false otherwise */
  public boolean isSetStatus() {
    return this.status != null;
  }

  public void setStatusIsSet(boolean value) {
    if (!value) {
      this.status = null;
    }
  }

  /**
   * 当status为SERVER_FUTURE_STOPED或SERVER_FUTURE_MAINTAIN时，此值会出现一个时间点格式:yyyy-MM-dd HH:mm:ss,表示在此时间点会进行维护或停服务
   */
  public String getFuturetime() {
    return this.futuretime;
  }

  /**
   * 当status为SERVER_FUTURE_STOPED或SERVER_FUTURE_MAINTAIN时，此值会出现一个时间点格式:yyyy-MM-dd HH:mm:ss,表示在此时间点会进行维护或停服务
   */
  public State setFuturetime(String futuretime) {
    this.futuretime = futuretime;
    return this;
  }

  public void unsetFuturetime() {
    this.futuretime = null;
  }

  /** Returns true if field futuretime is set (has been assigned a value) and false otherwise */
  public boolean isSetFuturetime() {
    return this.futuretime != null;
  }

  public void setFuturetimeIsSet(boolean value) {
    if (!value) {
      this.futuretime = null;
    }
  }

  /**
   * 当status为DOMAINNAME_CHANGE时，此值一个新域名或逗号分隔的多个域名，使用人按照顺序逐个调用直到调用成功或每个都使用过，如域名:a.b.c,a1.b.c,a2.b.c,表示3个域名轮询调用
   */
  public String getDomanename() {
    return this.domanename;
  }

  /**
   * 当status为DOMAINNAME_CHANGE时，此值一个新域名或逗号分隔的多个域名，使用人按照顺序逐个调用直到调用成功或每个都使用过，如域名:a.b.c,a1.b.c,a2.b.c,表示3个域名轮询调用
   */
  public State setDomanename(String domanename) {
    this.domanename = domanename;
    return this;
  }

  public void unsetDomanename() {
    this.domanename = null;
  }

  /** Returns true if field domanename is set (has been assigned a value) and false otherwise */
  public boolean isSetDomanename() {
    return this.domanename != null;
  }

  public void setDomanenameIsSet(boolean value) {
    if (!value) {
      this.domanename = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case STATUS:
      if (value == null) {
        unsetStatus();
      } else {
        setStatus((Status)value);
      }
      break;

    case FUTURETIME:
      if (value == null) {
        unsetFuturetime();
      } else {
        setFuturetime((String)value);
      }
      break;

    case DOMANENAME:
      if (value == null) {
        unsetDomanename();
      } else {
        setDomanename((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case STATUS:
      return getStatus();

    case FUTURETIME:
      return getFuturetime();

    case DOMANENAME:
      return getDomanename();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case STATUS:
      return isSetStatus();
    case FUTURETIME:
      return isSetFuturetime();
    case DOMANENAME:
      return isSetDomanename();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof State)
      return this.equals((State)that);
    return false;
  }

  public boolean equals(State that) {
    if (that == null)
      return false;

    boolean this_present_status = true && this.isSetStatus();
    boolean that_present_status = true && that.isSetStatus();
    if (this_present_status || that_present_status) {
      if (!(this_present_status && that_present_status))
        return false;
      if (!this.status.equals(that.status))
        return false;
    }

    boolean this_present_futuretime = true && this.isSetFuturetime();
    boolean that_present_futuretime = true && that.isSetFuturetime();
    if (this_present_futuretime || that_present_futuretime) {
      if (!(this_present_futuretime && that_present_futuretime))
        return false;
      if (!this.futuretime.equals(that.futuretime))
        return false;
    }

    boolean this_present_domanename = true && this.isSetDomanename();
    boolean that_present_domanename = true && that.isSetDomanename();
    if (this_present_domanename || that_present_domanename) {
      if (!(this_present_domanename && that_present_domanename))
        return false;
      if (!this.domanename.equals(that.domanename))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_status = true && (isSetStatus());
    list.add(present_status);
    if (present_status)
      list.add(status.getValue());

    boolean present_futuretime = true && (isSetFuturetime());
    list.add(present_futuretime);
    if (present_futuretime)
      list.add(futuretime);

    boolean present_domanename = true && (isSetDomanename());
    list.add(present_domanename);
    if (present_domanename)
      list.add(domanename);

    return list.hashCode();
  }

  @Override
  public int compareTo(State other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetStatus()).compareTo(other.isSetStatus());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStatus()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, other.status);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFuturetime()).compareTo(other.isSetFuturetime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFuturetime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.futuretime, other.futuretime);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDomanename()).compareTo(other.isSetDomanename());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDomanename()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.domanename, other.domanename);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("State(");
    boolean first = true;

    sb.append("status:");
    if (this.status == null) {
      sb.append("null");
    } else {
      sb.append(this.status);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("futuretime:");
    if (this.futuretime == null) {
      sb.append("null");
    } else {
      sb.append(this.futuretime);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("domanename:");
    if (this.domanename == null) {
      sb.append("null");
    } else {
      sb.append(this.domanename);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (status == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'status' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class StateStandardSchemeFactory implements SchemeFactory {
    public StateStandardScheme getScheme() {
      return new StateStandardScheme();
    }
  }

  private static class StateStandardScheme extends StandardScheme<State> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, State struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // STATUS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.status = com.woniu.sncp.cbss.api.core.thrift.Status.findByValue(iprot.readI32());
              struct.setStatusIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // FUTURETIME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.futuretime = iprot.readString();
              struct.setFuturetimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // DOMANENAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.domanename = iprot.readString();
              struct.setDomanenameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, State struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.status != null) {
        oprot.writeFieldBegin(STATUS_FIELD_DESC);
        oprot.writeI32(struct.status.getValue());
        oprot.writeFieldEnd();
      }
      if (struct.futuretime != null) {
        oprot.writeFieldBegin(FUTURETIME_FIELD_DESC);
        oprot.writeString(struct.futuretime);
        oprot.writeFieldEnd();
      }
      if (struct.domanename != null) {
        oprot.writeFieldBegin(DOMANENAME_FIELD_DESC);
        oprot.writeString(struct.domanename);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class StateTupleSchemeFactory implements SchemeFactory {
    public StateTupleScheme getScheme() {
      return new StateTupleScheme();
    }
  }

  private static class StateTupleScheme extends TupleScheme<State> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, State struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.status.getValue());
      BitSet optionals = new BitSet();
      if (struct.isSetFuturetime()) {
        optionals.set(0);
      }
      if (struct.isSetDomanename()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetFuturetime()) {
        oprot.writeString(struct.futuretime);
      }
      if (struct.isSetDomanename()) {
        oprot.writeString(struct.domanename);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, State struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.status = com.woniu.sncp.cbss.api.core.thrift.Status.findByValue(iprot.readI32());
      struct.setStatusIsSet(true);
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.futuretime = iprot.readString();
        struct.setFuturetimeIsSet(true);
      }
      if (incoming.get(1)) {
        struct.domanename = iprot.readString();
        struct.setDomanenameIsSet(true);
      }
    }
  }

}

