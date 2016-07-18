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
 * 请求数据--客户端请求信息
 */
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-07-18")
public class ClientRequest implements org.apache.thrift.TBase<ClientRequest, ClientRequest._Fields>, java.io.Serializable, Cloneable, Comparable<ClientRequest> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ClientRequest");

  private static final org.apache.thrift.protocol.TField TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("time", org.apache.thrift.protocol.TType.I64, (short)1);
  private static final org.apache.thrift.protocol.TField CLIENT_USER_IP_FIELD_DESC = new org.apache.thrift.protocol.TField("clientUserIp", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField LOCAL_REQ_IP_FIELD_DESC = new org.apache.thrift.protocol.TField("localReqIp", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField OTHER_FIELD_DESC = new org.apache.thrift.protocol.TField("other", org.apache.thrift.protocol.TType.MAP, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ClientRequestStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ClientRequestTupleSchemeFactory());
  }

  public long time; // required
  public String clientUserIp; // required
  public String localReqIp; // required
  public Map<String,String> other; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TIME((short)1, "time"),
    CLIENT_USER_IP((short)2, "clientUserIp"),
    LOCAL_REQ_IP((short)3, "localReqIp"),
    OTHER((short)4, "other");

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
        case 1: // TIME
          return TIME;
        case 2: // CLIENT_USER_IP
          return CLIENT_USER_IP;
        case 3: // LOCAL_REQ_IP
          return LOCAL_REQ_IP;
        case 4: // OTHER
          return OTHER;
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
  private static final int __TIME_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TIME, new org.apache.thrift.meta_data.FieldMetaData("time", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.CLIENT_USER_IP, new org.apache.thrift.meta_data.FieldMetaData("clientUserIp", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.LOCAL_REQ_IP, new org.apache.thrift.meta_data.FieldMetaData("localReqIp", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.OTHER, new org.apache.thrift.meta_data.FieldMetaData("other", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ClientRequest.class, metaDataMap);
  }

  public ClientRequest() {
  }

  public ClientRequest(
    long time,
    String clientUserIp,
    String localReqIp,
    Map<String,String> other)
  {
    this();
    this.time = time;
    setTimeIsSet(true);
    this.clientUserIp = clientUserIp;
    this.localReqIp = localReqIp;
    this.other = other;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ClientRequest(ClientRequest other) {
    __isset_bitfield = other.__isset_bitfield;
    this.time = other.time;
    if (other.isSetClientUserIp()) {
      this.clientUserIp = other.clientUserIp;
    }
    if (other.isSetLocalReqIp()) {
      this.localReqIp = other.localReqIp;
    }
    if (other.isSetOther()) {
      Map<String,String> __this__other = new HashMap<String,String>(other.other);
      this.other = __this__other;
    }
  }

  public ClientRequest deepCopy() {
    return new ClientRequest(this);
  }

  @Override
  public void clear() {
    setTimeIsSet(false);
    this.time = 0;
    this.clientUserIp = null;
    this.localReqIp = null;
    this.other = null;
  }

  public long getTime() {
    return this.time;
  }

  public ClientRequest setTime(long time) {
    this.time = time;
    setTimeIsSet(true);
    return this;
  }

  public void unsetTime() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TIME_ISSET_ID);
  }

  /** Returns true if field time is set (has been assigned a value) and false otherwise */
  public boolean isSetTime() {
    return EncodingUtils.testBit(__isset_bitfield, __TIME_ISSET_ID);
  }

  public void setTimeIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIME_ISSET_ID, value);
  }

  public String getClientUserIp() {
    return this.clientUserIp;
  }

  public ClientRequest setClientUserIp(String clientUserIp) {
    this.clientUserIp = clientUserIp;
    return this;
  }

  public void unsetClientUserIp() {
    this.clientUserIp = null;
  }

  /** Returns true if field clientUserIp is set (has been assigned a value) and false otherwise */
  public boolean isSetClientUserIp() {
    return this.clientUserIp != null;
  }

  public void setClientUserIpIsSet(boolean value) {
    if (!value) {
      this.clientUserIp = null;
    }
  }

  public String getLocalReqIp() {
    return this.localReqIp;
  }

  public ClientRequest setLocalReqIp(String localReqIp) {
    this.localReqIp = localReqIp;
    return this;
  }

  public void unsetLocalReqIp() {
    this.localReqIp = null;
  }

  /** Returns true if field localReqIp is set (has been assigned a value) and false otherwise */
  public boolean isSetLocalReqIp() {
    return this.localReqIp != null;
  }

  public void setLocalReqIpIsSet(boolean value) {
    if (!value) {
      this.localReqIp = null;
    }
  }

  public int getOtherSize() {
    return (this.other == null) ? 0 : this.other.size();
  }

  public void putToOther(String key, String val) {
    if (this.other == null) {
      this.other = new HashMap<String,String>();
    }
    this.other.put(key, val);
  }

  public Map<String,String> getOther() {
    return this.other;
  }

  public ClientRequest setOther(Map<String,String> other) {
    this.other = other;
    return this;
  }

  public void unsetOther() {
    this.other = null;
  }

  /** Returns true if field other is set (has been assigned a value) and false otherwise */
  public boolean isSetOther() {
    return this.other != null;
  }

  public void setOtherIsSet(boolean value) {
    if (!value) {
      this.other = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TIME:
      if (value == null) {
        unsetTime();
      } else {
        setTime((Long)value);
      }
      break;

    case CLIENT_USER_IP:
      if (value == null) {
        unsetClientUserIp();
      } else {
        setClientUserIp((String)value);
      }
      break;

    case LOCAL_REQ_IP:
      if (value == null) {
        unsetLocalReqIp();
      } else {
        setLocalReqIp((String)value);
      }
      break;

    case OTHER:
      if (value == null) {
        unsetOther();
      } else {
        setOther((Map<String,String>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TIME:
      return getTime();

    case CLIENT_USER_IP:
      return getClientUserIp();

    case LOCAL_REQ_IP:
      return getLocalReqIp();

    case OTHER:
      return getOther();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TIME:
      return isSetTime();
    case CLIENT_USER_IP:
      return isSetClientUserIp();
    case LOCAL_REQ_IP:
      return isSetLocalReqIp();
    case OTHER:
      return isSetOther();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ClientRequest)
      return this.equals((ClientRequest)that);
    return false;
  }

  public boolean equals(ClientRequest that) {
    if (that == null)
      return false;

    boolean this_present_time = true;
    boolean that_present_time = true;
    if (this_present_time || that_present_time) {
      if (!(this_present_time && that_present_time))
        return false;
      if (this.time != that.time)
        return false;
    }

    boolean this_present_clientUserIp = true && this.isSetClientUserIp();
    boolean that_present_clientUserIp = true && that.isSetClientUserIp();
    if (this_present_clientUserIp || that_present_clientUserIp) {
      if (!(this_present_clientUserIp && that_present_clientUserIp))
        return false;
      if (!this.clientUserIp.equals(that.clientUserIp))
        return false;
    }

    boolean this_present_localReqIp = true && this.isSetLocalReqIp();
    boolean that_present_localReqIp = true && that.isSetLocalReqIp();
    if (this_present_localReqIp || that_present_localReqIp) {
      if (!(this_present_localReqIp && that_present_localReqIp))
        return false;
      if (!this.localReqIp.equals(that.localReqIp))
        return false;
    }

    boolean this_present_other = true && this.isSetOther();
    boolean that_present_other = true && that.isSetOther();
    if (this_present_other || that_present_other) {
      if (!(this_present_other && that_present_other))
        return false;
      if (!this.other.equals(that.other))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_time = true;
    list.add(present_time);
    if (present_time)
      list.add(time);

    boolean present_clientUserIp = true && (isSetClientUserIp());
    list.add(present_clientUserIp);
    if (present_clientUserIp)
      list.add(clientUserIp);

    boolean present_localReqIp = true && (isSetLocalReqIp());
    list.add(present_localReqIp);
    if (present_localReqIp)
      list.add(localReqIp);

    boolean present_other = true && (isSetOther());
    list.add(present_other);
    if (present_other)
      list.add(other);

    return list.hashCode();
  }

  @Override
  public int compareTo(ClientRequest other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetTime()).compareTo(other.isSetTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.time, other.time);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetClientUserIp()).compareTo(other.isSetClientUserIp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClientUserIp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clientUserIp, other.clientUserIp);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLocalReqIp()).compareTo(other.isSetLocalReqIp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLocalReqIp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.localReqIp, other.localReqIp);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOther()).compareTo(other.isSetOther());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOther()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.other, other.other);
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
    StringBuilder sb = new StringBuilder("ClientRequest(");
    boolean first = true;

    sb.append("time:");
    sb.append(this.time);
    first = false;
    if (!first) sb.append(", ");
    sb.append("clientUserIp:");
    if (this.clientUserIp == null) {
      sb.append("null");
    } else {
      sb.append(this.clientUserIp);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("localReqIp:");
    if (this.localReqIp == null) {
      sb.append("null");
    } else {
      sb.append(this.localReqIp);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("other:");
    if (this.other == null) {
      sb.append("null");
    } else {
      sb.append(this.other);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'time' because it's a primitive and you chose the non-beans generator.
    if (clientUserIp == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'clientUserIp' was not present! Struct: " + toString());
    }
    if (localReqIp == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'localReqIp' was not present! Struct: " + toString());
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ClientRequestStandardSchemeFactory implements SchemeFactory {
    public ClientRequestStandardScheme getScheme() {
      return new ClientRequestStandardScheme();
    }
  }

  private static class ClientRequestStandardScheme extends StandardScheme<ClientRequest> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ClientRequest struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.time = iprot.readI64();
              struct.setTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CLIENT_USER_IP
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.clientUserIp = iprot.readString();
              struct.setClientUserIpIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // LOCAL_REQ_IP
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.localReqIp = iprot.readString();
              struct.setLocalReqIpIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // OTHER
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map10 = iprot.readMapBegin();
                struct.other = new HashMap<String,String>(2*_map10.size);
                String _key11;
                String _val12;
                for (int _i13 = 0; _i13 < _map10.size; ++_i13)
                {
                  _key11 = iprot.readString();
                  _val12 = iprot.readString();
                  struct.other.put(_key11, _val12);
                }
                iprot.readMapEnd();
              }
              struct.setOtherIsSet(true);
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
      if (!struct.isSetTime()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'time' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ClientRequest struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TIME_FIELD_DESC);
      oprot.writeI64(struct.time);
      oprot.writeFieldEnd();
      if (struct.clientUserIp != null) {
        oprot.writeFieldBegin(CLIENT_USER_IP_FIELD_DESC);
        oprot.writeString(struct.clientUserIp);
        oprot.writeFieldEnd();
      }
      if (struct.localReqIp != null) {
        oprot.writeFieldBegin(LOCAL_REQ_IP_FIELD_DESC);
        oprot.writeString(struct.localReqIp);
        oprot.writeFieldEnd();
      }
      if (struct.other != null) {
        oprot.writeFieldBegin(OTHER_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.other.size()));
          for (Map.Entry<String, String> _iter14 : struct.other.entrySet())
          {
            oprot.writeString(_iter14.getKey());
            oprot.writeString(_iter14.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ClientRequestTupleSchemeFactory implements SchemeFactory {
    public ClientRequestTupleScheme getScheme() {
      return new ClientRequestTupleScheme();
    }
  }

  private static class ClientRequestTupleScheme extends TupleScheme<ClientRequest> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ClientRequest struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI64(struct.time);
      oprot.writeString(struct.clientUserIp);
      oprot.writeString(struct.localReqIp);
      BitSet optionals = new BitSet();
      if (struct.isSetOther()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetOther()) {
        {
          oprot.writeI32(struct.other.size());
          for (Map.Entry<String, String> _iter15 : struct.other.entrySet())
          {
            oprot.writeString(_iter15.getKey());
            oprot.writeString(_iter15.getValue());
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ClientRequest struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.time = iprot.readI64();
      struct.setTimeIsSet(true);
      struct.clientUserIp = iprot.readString();
      struct.setClientUserIpIsSet(true);
      struct.localReqIp = iprot.readString();
      struct.setLocalReqIpIsSet(true);
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TMap _map16 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.other = new HashMap<String,String>(2*_map16.size);
          String _key17;
          String _val18;
          for (int _i19 = 0; _i19 < _map16.size; ++_i19)
          {
            _key17 = iprot.readString();
            _val18 = iprot.readString();
            struct.other.put(_key17, _val18);
          }
        }
        struct.setOtherIsSet(true);
      }
    }
  }

}
