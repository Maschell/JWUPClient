package de.mas.wupclient.client.operations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.Logger;
import de.mas.wupclient.client.utils.MetaInformation;
import de.mas.wupclient.client.utils.Utils;

public class DumperOperations extends Operations {
    private static Map<WUPClient,DumperOperations> instances = new HashMap<>();
    public static DumperOperations DumperOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new DumperOperations(client));
        }
        return instances.get(client);        
    }
    UtilOperations util = null;
    FileOperations file = null;
    DownloadUploadOperations dlul = null;
    
    private DumperOperations(WUPClient client) {
        super(client);
        setUtil(UtilOperations.UtilOperationsFactory(client));
        setDLUL(DownloadUploadOperations.DownloadUploadOperationsFactory(client));
        setFile(FileOperations.FileOperationsFactory(client));
    }
    
    public boolean dumpDisc(String pattern,boolean deepSearch) throws IOException{
        try{     
            util.mount_sdcard();
            int res = util.mount("/dev/odd03", "/vol/storage_odd_content_dump", 2);
            if(res != 0){
                //throw new MountingFailedException();
            }
            System.out.println("Grabbing meta information!");
            byte[] metafile = dlul.downloadFileToByteArray("/vol/storage_odd_content_dump/meta/meta.xml");
            if(metafile == null){
                throw new LoadMetaFailedException();
            }
            MetaInformation title = Utils.readMeta(new ByteArrayInputStream(metafile));
            Logger.log("Dumping " + title.getLongnameEN() + "(" + title.getTitleIDAsString() + ")");
            file.mkdir("/vol/storage_sdcard/dumps", 0x600);
            file.mkdir("/vol/storage_sdcard/dumps/" + title.getTitleIDAsString(), 0x600);
            if(pattern != ".*"){
                System.out.println("Searching matching files. This could take a while");
            }
            file.cpdir("/vol/storage_odd_content_dump","/vol/storage_sdcard/dumps/" + title.getTitleIDAsString(),"",pattern,deepSearch);
           
        //}catch (MountingFailedException e) {
        //    Logger.logErr("Mounting failed");
        }catch (LoadMetaFailedException e){
            Logger.logErr("Loading meta.xml failed");
        }finally{
            util.unmount("/vol/storage_odd_content_dump", 2);
        }
        return true;        
    }
    
    public UtilOperations getUtil() {
        return util;
    }

    public void setUtil(UtilOperations util) {
        this.util = util;
    }

    public DownloadUploadOperations getDLUL() {
        return dlul;
    }

    public void setDLUL(DownloadUploadOperations dlul) {
        this.dlul = dlul;
    }

    public FileOperations getFile() {
        return file;
    }

    public void setFile(FileOperations file) {
        this.file = file;
    }
    
}
