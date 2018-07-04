package com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.utils;

public class NVPException extends Exception {
    private static final long serialVersionUID = 1L;
    private final NVPCodec result;
  
    public NVPException(String message, NVPCodec result) {
        super(message);
        this.result = result;
    }
      
    public NVPCodec getResult() {
       return result;
    }
  
}
