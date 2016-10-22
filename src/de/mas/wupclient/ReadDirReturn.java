package de.mas.wupclient;

public class ReadDirReturn {
    private int result;
    private String filename;
    private boolean isFile;
    private byte[] unknowndata;

    public ReadDirReturn(int result,String filename, boolean isFile, byte[] unknowndata) {
        setResult(result);
        setFilename(filename);
        setFile(isFile);
        setUnknowndata(unknowndata);
    }

    public ReadDirReturn(int resultValue) {
        setResult(resultValue);
    }

    public byte[] getUnknowndata() {
        return unknowndata;
    }

    public void setUnknowndata(byte[] unknowndata) {
        this.unknowndata = unknowndata;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

}
