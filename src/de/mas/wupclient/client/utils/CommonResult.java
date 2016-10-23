package de.mas.wupclient.client.utils;

public abstract class CommonResult {
    private int resultValue;
    
    public CommonResult(int result){
        setResultValue(result);
    }    
  
    public int getResultValue() {    
        return resultValue;
    }
    
    public void setResultValue(int resultValue) {
        this.resultValue = resultValue;
    }
}