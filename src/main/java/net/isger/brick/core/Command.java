/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package net.isger.brick.core;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Command extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Command\",\"namespace\":\"net.isger.brick.core\",\"fields\":[{\"name\":\"headers\",\"type\":{\"type\":\"map\",\"values\":\"bytes\"}},{\"name\":\"parameters\",\"type\":{\"type\":\"map\",\"values\":\"bytes\"}},{\"name\":\"footers\",\"type\":{\"type\":\"map\",\"values\":\"bytes\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> headers;
  @Deprecated public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> parameters;
  @Deprecated public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> footers;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public Command() {}

  /**
   * All-args constructor.
   */
  public Command(java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> headers, java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> parameters, java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> footers) {
    this.headers = headers;
    this.parameters = parameters;
    this.footers = footers;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return headers;
    case 1: return parameters;
    case 2: return footers;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: headers = (java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer>)value$; break;
    case 1: parameters = (java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer>)value$; break;
    case 2: footers = (java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'headers' field.
   */
  public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> getHeaders() {
    return headers;
  }

  /**
   * Sets the value of the 'headers' field.
   * @param value the value to set.
   */
  public void setHeaders(java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> value) {
    this.headers = value;
  }

  /**
   * Gets the value of the 'parameters' field.
   */
  public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> getParameters() {
    return parameters;
  }

  /**
   * Sets the value of the 'parameters' field.
   * @param value the value to set.
   */
  public void setParameters(java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> value) {
    this.parameters = value;
  }

  /**
   * Gets the value of the 'footers' field.
   */
  public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> getFooters() {
    return footers;
  }

  /**
   * Sets the value of the 'footers' field.
   * @param value the value to set.
   */
  public void setFooters(java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> value) {
    this.footers = value;
  }

  /** Creates a new Command RecordBuilder */
  public static net.isger.brick.core.Command.Builder newBuilder() {
    return new net.isger.brick.core.Command.Builder();
  }
  
  /** Creates a new Command RecordBuilder by copying an existing Builder */
  public static net.isger.brick.core.Command.Builder newBuilder(net.isger.brick.core.Command.Builder other) {
    return new net.isger.brick.core.Command.Builder(other);
  }
  
  /** Creates a new Command RecordBuilder by copying an existing Command instance */
  public static net.isger.brick.core.Command.Builder newBuilder(net.isger.brick.core.Command other) {
    return new net.isger.brick.core.Command.Builder(other);
  }
  
  /**
   * RecordBuilder for Command instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Command>
    implements org.apache.avro.data.RecordBuilder<Command> {

    private java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> headers;
    private java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> parameters;
    private java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> footers;

    /** Creates a new Builder */
    private Builder() {
      super(net.isger.brick.core.Command.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(net.isger.brick.core.Command.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.headers)) {
        this.headers = data().deepCopy(fields()[0].schema(), other.headers);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.parameters)) {
        this.parameters = data().deepCopy(fields()[1].schema(), other.parameters);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.footers)) {
        this.footers = data().deepCopy(fields()[2].schema(), other.footers);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing Command instance */
    private Builder(net.isger.brick.core.Command other) {
            super(net.isger.brick.core.Command.SCHEMA$);
      if (isValidValue(fields()[0], other.headers)) {
        this.headers = data().deepCopy(fields()[0].schema(), other.headers);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.parameters)) {
        this.parameters = data().deepCopy(fields()[1].schema(), other.parameters);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.footers)) {
        this.footers = data().deepCopy(fields()[2].schema(), other.footers);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'headers' field */
    public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> getHeaders() {
      return headers;
    }
    
    /** Sets the value of the 'headers' field */
    public net.isger.brick.core.Command.Builder setHeaders(java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> value) {
      validate(fields()[0], value);
      this.headers = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'headers' field has been set */
    public boolean hasHeaders() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'headers' field */
    public net.isger.brick.core.Command.Builder clearHeaders() {
      headers = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'parameters' field */
    public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> getParameters() {
      return parameters;
    }
    
    /** Sets the value of the 'parameters' field */
    public net.isger.brick.core.Command.Builder setParameters(java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> value) {
      validate(fields()[1], value);
      this.parameters = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'parameters' field has been set */
    public boolean hasParameters() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'parameters' field */
    public net.isger.brick.core.Command.Builder clearParameters() {
      parameters = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'footers' field */
    public java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> getFooters() {
      return footers;
    }
    
    /** Sets the value of the 'footers' field */
    public net.isger.brick.core.Command.Builder setFooters(java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer> value) {
      validate(fields()[2], value);
      this.footers = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'footers' field has been set */
    public boolean hasFooters() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'footers' field */
    public net.isger.brick.core.Command.Builder clearFooters() {
      footers = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public Command build() {
      try {
        Command record = new Command();
        record.headers = fieldSetFlags()[0] ? this.headers : (java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer>) defaultValue(fields()[0]);
        record.parameters = fieldSetFlags()[1] ? this.parameters : (java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer>) defaultValue(fields()[1]);
        record.footers = fieldSetFlags()[2] ? this.footers : (java.util.Map<java.lang.CharSequence,java.nio.ByteBuffer>) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
