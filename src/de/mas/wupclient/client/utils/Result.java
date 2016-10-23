package de.mas.wupclient.client.utils;

public class Result<T> extends CommonResult{
    private T data;

    public Result(int result,T data) {
        super(result); 
        setData(data);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
