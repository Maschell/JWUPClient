package de.mas.wupclient;

public class Result {
    private int resultValue;
    private int int_value;
    private byte[] data;
    
    public Result(int result, byte[] data){
        setData(data);
        setResultValue(result);
    }
    
    public Result(int resultValue2, int int_value) {
       setResultValue(resultValue2);
       setInt_value(int_value);
    }

    public int getResultValue() {    
        return resultValue;
    }
    
    public void setResultValue(int resultValue) {
        this.resultValue = resultValue;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public int getInt_value() {
        return int_value;
    }

    public void setInt_value(int int_value) {
        this.int_value = int_value;
    }
}
