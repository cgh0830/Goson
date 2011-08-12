package com.jpcamara.goson;

import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IConstructorInfo;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.PropertyInfoBuilder;
import gw.lang.reflect.TypeInfoBase;
import gw.lang.reflect.IRelativeTypeInfo.Accessibility;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.gs.IGosuObject;
import gw.util.concurrent.LazyVar;
import gw.lang.reflect.IEnumValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.logging.Logger;
import java.util.logging.Level;

public class JsonTypeInfo extends TypeInfoBase {
  private static final String ENUM_KEY = "enum";
  
  private static final Map<String, IJavaType> TYPES = new HashMap<String, IJavaType>();
  static {
    TYPES.put("bigdecimal", IJavaType.BIGDECIMAL);
    TYPES.put("biginteger", IJavaType.BIGINTEGER);
    TYPES.put("decimal", IJavaType.DOUBLE);
    TYPES.put("integer", IJavaType.INTEGER);
    TYPES.put("string", IJavaType.STRING);
    TYPES.put("date", IJavaType.DATE);
    TYPES.put("boolean", IJavaType.BOOLEAN);
    TYPES.put("enum", IJavaType.ENUM);
    TYPES.put("map_of", IJavaType.MAP);
  }

  private JsonType owner;
  private JsonParser json;
  private Map<String, String> propertyNames = new HashMap<String, String>();
  private List<IPropertyInfo> properties;
  private LazyVar<List<IMethodInfo>> methods = new LazyVar<List<IMethodInfo>>() {
    private List<IMethodInfo> typeMethods = new ArrayList<IMethodInfo>();		
    @Override
    protected List<IMethodInfo> init() {
    	typeMethods.add(new MethodInfoBuilder()
    		.withName("write")
    		.withReturnType(IJavaType.STRING)
    		.withStatic(true)
    		.withCallHandler(new IMethodCallHandler() {
    			@Override
    			public Object handleCall(Object ctx, Object... args) {
    				Json me = (Json)ctx;
    				return me.serialize();
    			}
    		})
    		.build(JsonTypeInfo.this));
    	typeMethods.add(new MethodInfoBuilder()
    		.withName("parse")
    		.withReturnType(getOwnersType())
    		.withStatic(true)
    		.withParameters(new ParameterInfoBuilder()
    			.withType(IJavaType.STRING))
    		.withCallHandler(new IMethodCallHandler() {
    			@Override
    			public Object handleCall(Object ctx, Object... args) {
    				String content = (String)args[0];
    				try {
    					return new Json(content, JsonTypeInfo.this);
    				} catch (Exception e) {
    					throw new RuntimeException(e);
    				}
    			}
    		})
    		.build(JsonTypeInfo.this));
    	return typeMethods;
    }
  };
    
  public JsonTypeInfo(JsonType owner, JsonParser object) {
    this.owner = owner;
    this.json = object;
    createProperties();
  }
  
  private PropertyInfoBuilder create(final String name) {
    JsonName propertyName = new JsonName(name);
	  propertyNames.put(propertyName.getName(), name);
    return new PropertyInfoBuilder()
      .withName(propertyName.getName()).withWritable(true)
      .withAccessor(new IPropertyAccessor() {
      	@Override
      	public void setValue(Object ctx, Object value) {
      		Json json = (Json) ctx;
      		try {
      			json.put(name, value);
      		} catch (Exception e) {
      			throw new RuntimeException(e);
      		}
      	}

      	@Override
      	public Object getValue(Object ctx) {
      		try {
      		  Object o = ((Json) ctx).get(name);
      		  if (o instanceof List) {
      		  }
      			return ((Json) ctx).get(name);
      		} catch (Exception e) {
      			throw new RuntimeException(e);
      		}
      	}
      });
  }

	private IPropertyInfo createWithType(final String name, IType type) {
	  PropertyInfoBuilder property = create(name);
	  return property.withType(type).build(this);
	}

	private IPropertyInfo createWithListType(final String name, IType type) {
	  PropertyInfoBuilder property = create(name);
	  return property.withType(IJavaType.ARRAYLIST.getParameterizedType(new IType[] { type })).build(this);
	}
	
	private IPropertyInfo createWithMapType(final String name, IType key, IType value) {
	  PropertyInfoBuilder property = create(name);
	  return property.withType(IJavaType.MAP.getParameterizedType(key, value)).build(this);
	}
	
	private IJavaType findJavaType(String typeName) {
	  IJavaType t = TYPES.get(typeName);
	  if (t == null) {
	    return null;
	  }
	  return t;
/*    return null; //TODO throw exception*/
/*    throw new RuntimeException("bad type: " + typeName);*/
	}
	
	private void createEnumProperties(final String key, JSONArray enumCodes) {
	  JsonEnumType type = (JsonEnumType)getOwnersType();
    try {
      for (int i = 0; i < enumCodes.length(); i++) {
        final String code = enumCodes.getString(i);
        final String enumified = JsonEnumType.enumify(code);
        final IEnumValue value = type.getEnumValue(enumified);
        
    	  propertyNames.put(enumified, code);
        PropertyInfoBuilder property = new PropertyInfoBuilder()
          .withName(enumified).withWritable(false)
          .withAccessor(new IPropertyAccessor() {
          	@Override
          	public Object getValue(Object ctx) {
          		try {
          			return value;//((JsonEnumType) ctx).get(code);
          		} catch (Exception e) {
          			throw new RuntimeException(e);
          		}
          	}
          	
          	@Override
          	public void setValue(Object ctx, Object value) {
          		throw new UnsupportedOperationException("Cannot set an enum");
          	}
          });
        
        properties.add(property.withStatic(true)
                .withType(type).build(this));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
	}
	
	private void createProperties() {
    properties = new ArrayList<IPropertyInfo>();
    for (String key : json.keys()) {
      Object value = json.get(key);
      if (key.equals(ENUM_KEY)) {
        createEnumProperties(key, (JSONArray)value);
        continue;
      }
      if (JsonParser.isJSONObject(value)) {
        if (((JSONObject)value).has("map_of")) {
          //WOW SO UGLY
          try {
            JSONObject o = ((JSONObject)value).getJSONObject("map_of");
            if (!o.has("key") || !o.has("value")) {
              throw new RuntimeException("You must specify a key and value type");
            }
            IJavaType keyType = findJavaType((String)o.get("key"));
            IJavaType valueType = findJavaType((String)o.get("value"));
            //TODO if you're not a reg java type - kablooey
            properties.add(createWithMapType(key, keyType, valueType));
            continue;
          } catch (Exception e) {
            e.printStackTrace();
            continue;
          }
        }
        JsonType type = getOwnersType();
        IType propertyType = type.getTypeLoader()
          .getType(type.getNamespace() + "." + new JsonName(key).getName());
        properties.add(createWithType(key, propertyType));
        continue;
      } else if (JsonParser.isJSONArray(value)) {
    		Object firstEntry = json.getWithIndex(key, 0);
    		if (JsonParser.isJSONObject(firstEntry)) {
    		  JsonType type = getOwnersType();
    			IType propertyType = type.getTypeLoader()
    					.getType(type.getNamespace() + "." + new JsonName(key).getName());
    			if (propertyType == null) {
    				throw new RuntimeException("No type found");
    			}
    			properties.add(createWithListType(key, propertyType));
        } else {
          properties.add(createWithListType(key, findJavaType((String)firstEntry)));
        }
    	  continue;
    	}
      
      if (!(value instanceof String)) {
        throw new RuntimeException("Unless a JSONObject or a JSONArray, the value should be" +
          " a string name of the desired type");
      }
      
      IJavaType javaType = findJavaType((String)value);
      if (javaType != null) {
        properties.add(createWithType(key, javaType));
      }
      if (javaType == IJavaType.ENUM) {
        continue; //no properties for the enum, they're at the type level?
      } else if (JsonParser.isJSONNull(json.get(key))) {
    		Logger.getLogger(getClass().getName()).log(Level.FINE, 
    		  "Cannot handle NULL values. No type created.");
    	}
    }
	}

	private IConstructorInfo defaultConstructor = new ConstructorInfoBuilder()
			.withConstructorHandler(new IConstructorHandler() {
				@Override
				public Object newInstance(Object... args) {
					Json j = new Json(JsonTypeInfo.this.getOwnersType());
					return j;
				}
			}).withAccessibility(Accessibility.PUBLIC).build(this);

	@Override
	public List<? extends IConstructorInfo> getConstructors() {
		List<IConstructorInfo> constructors = new ArrayList<IConstructorInfo>();
		constructors.add(defaultConstructor);
		return constructors;
	}

	@Override
	public List<? extends IMethodInfo> getMethods() {
		return methods.get();
	}

	@Override
	public List<? extends IPropertyInfo> getProperties() {
		return properties;
	}

	@Override
	public IPropertyInfo getProperty(CharSequence propName) {
		for (IPropertyInfo prop : properties) {
			if (propName.equals(prop.getName())) {
				return prop;
			}
		}
		return null;
	}

	@Override
	public CharSequence getRealPropertyName(CharSequence propName) {
		for (IPropertyInfo prop : properties) {
			if (propName.equals(prop.getName())) {
				return prop.getName();
			}
		}
		return null;
	}
	
	public String getJsonPropertyName(String propertyName) {
	  return propertyNames.get(propertyName);
	}

	@Override
	public List<IAnnotationInfo> getDeclaredAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasAnnotation(IType type) {
		return false;
	}

	@Override
	public JsonType getOwnersType() {
		return owner;
	}

	public String toString() {
		return properties.toString();
	}
}