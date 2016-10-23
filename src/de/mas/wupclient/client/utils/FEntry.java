package de.mas.wupclient.client.utils;

public class FEntry {
    private String filename;
    private boolean isFile;
    private byte[] unknowndata;

    public FEntry(String filename, boolean isFile, byte[] unknowndata) {
        setFilename(filename);
        setFile(isFile);
        setUnknowndata(unknowndata);
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
}
