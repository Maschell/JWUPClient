package de.mas.wupclient;

public class IoctlvResult{
    private int resultValue;
    private byte[][] data;
    
    public IoctlvResult(int result, byte[][] data) {
       setData(data);
       setResultValue(result);
    }

    public byte[][] getData() {
        return data;
    }

    public void setData(byte[][] data) {
        this.data = data;
    }

    public int getResultValue() {
        return resultValue;
    }

    public void setResultValue(int resultValue) {
        this.resultValue = resultValue;
    }

}
