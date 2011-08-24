package org.jschema.rpc;

import gw.lang.reflect.IType;
import gw.lang.reflect.gs.IGosuObject;

public class CustomRPCInstance implements IGosuObject {

  private IType _ownersType;
  private RPCConfig _config;
  private String _url;

  public CustomRPCInstance(IType ownersType, RPCCallHandler handler, String url, HttpMethod method) {
    _ownersType = ownersType;
    _config = new RPCConfig();
    if (handler != null) {
      _config.setCallHandler(handler);
    }
    if (method != null) {
      _config.setMethod(method);
    }
    _url = url;
  }

  @Override
  public IType getIntrinsicType() {
    return _ownersType;
  }

  public RPCConfig getConfig() {
    return _config;
  }

  public String getUrl() {
    return _url;
  }
}
