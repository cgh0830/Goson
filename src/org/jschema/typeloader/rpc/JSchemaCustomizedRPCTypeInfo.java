package org.jschema.typeloader.rpc;

import org.jschema.rpc.CustomRPCInstance;
import org.jschema.rpc.RPCConfig;

import java.util.*;

public class JSchemaCustomizedRPCTypeInfo extends JSchemaRPCTypeInfoBase
{
  public JSchemaCustomizedRPCTypeInfo(JSchemaCustomizedRPCType owner) {
    super(owner);
  }

  @Override
  protected boolean areRPCMethodsStatic() {
    return false;
  }

  protected String getRootTypeName() {
    String name = getOwnersType().getName();
    return name.substring(0, name.length() - JSchemaCustomizedRPCType.TYPE_SUFFIX.length());
  }

  @Override
  protected String handleRPCMethodInvocation(Object ctx, Map<String, String> argsMap) {
    CustomRPCInstance customRPCInstance = (CustomRPCInstance) ctx;
    RPCConfig config = customRPCInstance.getConfig();
    String url = customRPCInstance.getUrl();
    if (url == null || "".equals(url)) {
      url = getOwnersType().getDefaultURL();
    }
    return config.getCallHandler().handleCall(config.getMethod().name(),
      url, argsMap);
  }
}
