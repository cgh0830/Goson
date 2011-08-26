package org.jschema.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.IEnumType;
import gw.lang.reflect.IEnumValue;

import java.util.*;

import gw.lang.reflect.gs.IGosuObject;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JSchemaEnumType extends JSchemaType implements IEnumType {
  private List<IEnumValue> values = new ArrayList<IEnumValue>();

  public JSchemaEnumType(String name, ITypeLoader typeloader, final Object object) {
    super(name, typeloader, object, Collections.EMPTY_MAP);
    List obj = (List)((Map)object).get("enum");
    if (obj == null || !(obj instanceof List)) {
      throw new RuntimeException("An enum must be an array of values.");
    }
    for (int i = 0; i < obj.size(); i++) {
      values.add(new JsonEnumValue((String)obj.get(i)));
    }
  }
  
  @Override
  public List<IEnumValue> getEnumValues() {
    return values;
  }

  @Override
  public IEnumValue getEnumValue( String strName ) {
    for (IEnumValue value : values) {
      if (value.getDisplayName().equals(strName)) {
        return value;
      }
    }
    return null;
  }
  
  public static String enumify(String original) {
    return original.replaceAll("\\s", "_").toUpperCase();
  }
  
  public class JsonEnumValue implements IEnumValue, IGosuObject {
    public String code;
    public String displayName;
    public String originalValue;

    public JsonEnumValue(String value) {
      originalValue = value;
      code = JSchemaEnumType.enumify(value);
      displayName = code;
    }
    
    public String getJsonCode() {
      return originalValue;
    }

    //What is this for?
    @Override
    public Object getValue() {
      throw new NotImplementedException();
    }

    @Override
    public String getCode() {
      return code;
    }

    @Override
    public int getOrdinal() {
      throw new NotImplementedException();
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }
    
    @Override
  	public IType getIntrinsicType() {
      return JSchemaEnumType.this;
  	}
  }
}