/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.woniu.sncp.cbss.api.core.thrift;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
/**
 * 1 请求数据
 * 2 客户端发起接收响应,将响应中的UUID回传
 */
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-07-14")
public class Data implements org.apache.thrift.TBase<Data, Data._Fields>, java.io.Serializable, Cloneable, Comparable<Data> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Data");

  private static final org.apache.thrift.protocol.TField VERSION_FIELD_DESC = new org.apache.thrift.protocol.TField("version", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField CLIENT_REQUEST_FIELD_DESC = new org.apache.thrift.protocol.TField("clientRequest", org.apache.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.thrift.protocol.TField PARAM_FIELD_DESC = new org.apache.thrift.protocol.TField("param", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField TRACE_STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("traceState", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField SESSION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("sessionId", org.apache.thrift.protocol.TType.STRING, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new DataStandardSchemeFactory());
    schemes.put(TupleScheme.class, new DataTupleSchemeFactory());
  }

  public String version; // required
  public ClientRequest clientRequest; // required
  public Param param; // required
  public String traceState; // required
  public String sessionId; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    VERSION((short)1, "version"),
    CLIENT_REQUEST((short)2, "clientRequest"),
    PARAM((short)3, "param"),
    TRACE_STATE((short)4, "traceState"),
    SESSION_ID((short)5, "sessionId");

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
        case 1: // VERSION
          return VERSION;
        case 2: // CLIENT_REQUEST
          return CLIENT_REQUEST;
        case 3: // PARAM
          return PARAM;
        case 4: // TRACE_STATE
          return TRACE_STATE;
        case 5: // SESSION_ID
          return SESSION_ID;
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
    tmpMap.put(_Fields.VERSION, new org.apache.thrift.meta_data.FieldMetaData("version", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CLIENT_REQUEST, new org.apache.thrift.meta_data.FieldMetaData("clientRequest", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT        , "ClientRequest")));
    tmpMap.put(_Fields.PARAM, new org.apache.thrift.meta_data.FieldMetaData("param", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT        , "Param")));
    tmpMap.put(_Fields.TRACE_STATE, new org.apache.thrift.meta_data.FieldMetaData("traceState", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.SESSION_ID, new org.apache.thrift.meta_data.FieldMetaData("sessionId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Data.class, metaDataMap);
  }

  public Data() {
    this.version = "0.0.1";

    this.traceState = "1";

  }

  public Data(
    String version,
    ClientRequest clientRequest,
    Param param,
    String traceState,
    String sessionId)
  {
    this();
    this.version = version;
    this.clientRequest = clientRequest;
    this.param = param;
    this.traceState = traceState;
    this.sessionId = sessionId;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Data(Data other) {
    if (other.isSetVersion()) {
      this.version = other.version;
    }
    if (other.isSetClientRequest()) {
      this.clientRequest = other.clientRequest;
    }
    if (other.isSetParam()) {
      this.param = other.param;
    }
    if (other.isSetTraceState()) {
      this.traceState = other.traceState;
    }
    if (other.isSetSessionId()) {
      this.sessionId = other.sessionId;
    }
  }

  public Data deepCopy() {
    return new Data(this);
  }

  @Override
  public void clear() {
    this.version = "0.0.1";

    this.clientRequest = null;
    this.param = null;
    this.traceState = "1";

    this.sessionId = null;
  }

  public String getVersion() {
    return this.version;
  }

  public Data setVersion(String version) {
    this.version = version;
    return this;
  }

  public void unsetVersion() {
    this.version = null;
  }

  /** Returns true if field version is set (has been assigned a value) and false otherwise */
  public boolean isSetVersion() {
    return this.version != null;
  }

  public void setVersionIsSet(boolean value) {
    if (!value) {
      this.version = null;
    }
  }

  public ClientRequest getClientRequest() {
    return this.clientRequest;
  }

  public Data setClientRequest(ClientRequest clientRequest) {
    this.clientRequest = clientRequest;
    return this;
  }

  public void unsetClientRequest() {
    this.clientRequest = null;
  }

  /** Returns true if field clientRequest is set (has been assigned a value) and false otherwise */
  public boolean isSetClientRequest() {
    return this.clientRequest != null;
  }

  public void setClientRequestIsSet(boolean value) {
    if (!value) {
      this.clientRequest = null;
    }
  }

  public Param getParam() {
    return this.param;
  }

  public Data setParam(Param param) {
    this.param = param;
    return this;
  }

  public void unsetParam() {
    this.param = null;
  }

  /** Returns true if field param is set (has been assigned a value) and false otherwise */
  public boolean isSetParam() {
    return this.param != null;
  }

  public void setParamIsSet(boolean value) {
    if (!value) {
      this.param = null;
    }
  }

  public String getTraceState() {
    return this.traceState;
  }

  public Data setTraceState(String traceState) {
    this.traceState = traceState;
    return this;
  }

  public void unsetTraceState() {
    this.traceState = null;
  }

  /** Returns true if field traceState is set (has been assigned a value) and false otherwise */
  public boolean isSetTraceState() {
    return this.traceState != null;
  }

  public void setTraceStateIsSet(boolean value) {
    if (!value) {
      this.traceState = null;
    }
  }

  public String getSessionId() {
    return this.sessionId;
  }

  public Data setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public void unsetSessionId() {
    this.sessionId = null;
  }

  /** Returns true if field sessionId is set (has been assigned a value) and false otherwise */
  public boolean isSetSessionId() {
    return this.sessionId != null;
  }

  public void setSessionIdIsSet(boolean value) {
    if (!value) {
      this.sessionId = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case VERSION:
      if (value == null) {
        unsetVersion();
      } else {
        setVersion((String)value);
      }
      break;

    case CLIENT_REQUEST:
      if (value == null) {
        unsetClientRequest();
      } else {
        setClientRequest((ClientRequest)value);
      }
      break;

    case PARAM:
      if (value == null) {
        unsetParam();
      } else {
        setParam((Param)value);
      }
      break;

    case TRACE_STATE:
      if (value == null) {
        unsetTraceState();
      } else {
        setTraceState((String)value);
      }
      break;

    case SESSION_ID:
      if (value == null) {
        unsetSessionId();
      } else {
        setSessionId((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case VERSION:
      return getVersion();

    case CLIENT_REQUEST:
      return getClientRequest();

    case PARAM:
      return getParam();

    case TRACE_STATE:
      return getTraceState();

    case SESSION_ID:
      return getSessionId();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case VERSION:
      return isSetVersion();
    case CLIENT_REQUEST:
      return isSetClientRequest();
    case PARAM:
      return isSetParam();
    case TRACE_STATE:
      return isSetTraceState();
    case SESSION_ID:
      return isSetSessionId();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Data)
      return this.equals((Data)that);
    return false;
  }

  public boolean equals(Data that) {
    if (that == null)
      return false;

    boolean this_present_version = true && this.isSetVersion();
    boolean that_present_version = true && that.isSetVersion();
    if (this_present_version || that_present_version) {
      if (!(this_present_version && that_present_version))
        return false;
      if (!this.version.equals(that.version))
        return false;
    }

    boolean this_present_clientRequest = true && this.isSetClientRequest();
    boolean that_present_clientRequest = true && that.isSetClientRequest();
    if (this_present_clientRequest || that_present_clientRequest) {
      if (!(this_present_clientRequest && that_present_clientRequest))
        return false;
      if (!this.clientRequest.equals(that.clientRequest))
        return false;
    }

    boolean this_present_param = true && this.isSetParam();
    boolean that_present_param = true && that.isSetParam();
    if (this_present_param || that_present_param) {
      if (!(this_present_param && that_present_param))
        return false;
      if (!this.param.equals(that.param))
        return false;
    }

    boolean this_present_traceState = true && this.isSetTraceState();
    boolean that_present_traceState = true && that.isSetTraceState();
    if (this_present_traceState || that_present_traceState) {
      if (!(this_present_traceState && that_present_traceState))
        return false;
      if (!this.traceState.equals(that.traceState))
        return false;
    }

    boolean this_present_sessionId = true && this.isSetSessionId();
    boolean that_present_sessionId = true && that.isSetSessionId();
    if (this_present_sessionId || that_present_sessionId) {
      if (!(this_present_sessionId && that_present_sessionId))
        return false;
      if (!this.sessionId.equals(that.sessionId))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_version = true && (isSetVersion());
    list.add(present_version);
    if (present_version)
      list.add(version);

    boolean present_clientRequest = true && (isSetClientRequest());
    list.add(present_clientRequest);
    if (present_clientRequest)
      list.add(clientRequest);

    boolean present_param = true && (isSetParam());
    list.add(present_param);
    if (present_param)
      list.add(param);

    boolean present_traceState = true && (isSetTraceState());
    list.add(present_traceState);
    if (present_traceState)
      list.add(traceState);

    boolean present_sessionId = true && (isSetSessionId());
    list.add(present_sessionId);
    if (present_sessionId)
      list.add(sessionId);

    return list.hashCode();
  }

  @Override
  public int compareTo(Data other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetVersion()).compareTo(other.isSetVersion());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetVersion()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.version, other.version);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetClientRequest()).compareTo(other.isSetClientRequest());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClientRequest()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clientRequest, other.clientRequest);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetParam()).compareTo(other.isSetParam());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetParam()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.param, other.param);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTraceState()).compareTo(other.isSetTraceState());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTraceState()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.traceState, other.traceState);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSessionId()).compareTo(other.isSetSessionId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSessionId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sessionId, other.sessionId);
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
    StringBuilder sb = new StringBuilder("Data(");
    boolean first = true;

    sb.append("version:");
    if (this.version == null) {
      sb.append("null");
    } else {
      sb.append(this.version);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("clientRequest:");
    if (this.clientRequest == null) {
      sb.append("null");
    } else {
      sb.append(this.clientRequest);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("param:");
    if (this.param == null) {
      sb.append("null");
    } else {
      sb.append(this.param);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("traceState:");
    if (this.traceState == null) {
      sb.append("null");
    } else {
      sb.append(this.traceState);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("sessionId:");
    if (this.sessionId == null) {
      sb.append("null");
    } else {
      sb.append(this.sessionId);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (version == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'version' was not present! Struct: " + toString());
    }
    if (clientRequest == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'clientRequest' was not present! Struct: " + toString());
    }
    if (param == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'param' was not present! Struct: " + toString());
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

  private static class DataStandardSchemeFactory implements SchemeFactory {
    public DataStandardScheme getScheme() {
      return new DataStandardScheme();
    }
  }

  private static class DataStandardScheme extends StandardScheme<Data> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Data struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // VERSION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.version = iprot.readString();
              struct.setVersionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CLIENT_REQUEST
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.clientRequest = new ClientRequest();
              struct.clientRequest.read(iprot);
              struct.setClientRequestIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PARAM
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.param = new Param();
              struct.param.read(iprot);
              struct.setParamIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TRACE_STATE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.traceState = iprot.readString();
              struct.setTraceStateIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // SESSION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.sessionId = iprot.readString();
              struct.setSessionIdIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, Data struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.version != null) {
        oprot.writeFieldBegin(VERSION_FIELD_DESC);
        oprot.writeString(struct.version);
        oprot.writeFieldEnd();
      }
      if (struct.clientRequest != null) {
        oprot.writeFieldBegin(CLIENT_REQUEST_FIELD_DESC);
        struct.clientRequest.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.param != null) {
        oprot.writeFieldBegin(PARAM_FIELD_DESC);
        struct.param.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.traceState != null) {
        oprot.writeFieldBegin(TRACE_STATE_FIELD_DESC);
        oprot.writeString(struct.traceState);
        oprot.writeFieldEnd();
      }
      if (struct.sessionId != null) {
        oprot.writeFieldBegin(SESSION_ID_FIELD_DESC);
        oprot.writeString(struct.sessionId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class DataTupleSchemeFactory implements SchemeFactory {
    public DataTupleScheme getScheme() {
      return new DataTupleScheme();
    }
  }

  private static class DataTupleScheme extends TupleScheme<Data> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Data struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.version);
      struct.clientRequest.write(oprot);
      struct.param.write(oprot);
      BitSet optionals = new BitSet();
      if (struct.isSetTraceState()) {
        optionals.set(0);
      }
      if (struct.isSetSessionId()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetTraceState()) {
        oprot.writeString(struct.traceState);
      }
      if (struct.isSetSessionId()) {
        oprot.writeString(struct.sessionId);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Data struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.version = iprot.readString();
      struct.setVersionIsSet(true);
      struct.clientRequest = new ClientRequest();
      struct.clientRequest.read(iprot);
      struct.setClientRequestIsSet(true);
      struct.param = new Param();
      struct.param.read(iprot);
      struct.setParamIsSet(true);
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.traceState = iprot.readString();
        struct.setTraceStateIsSet(true);
      }
      if (incoming.get(1)) {
        struct.sessionId = iprot.readString();
        struct.setSessionIdIsSet(true);
      }
    }
  }

}

