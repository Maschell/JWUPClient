A port of smea's client (WUPClient.py) for the wupserver.

Open it with  
```
java -jar jwupclient.jar 192.168.x.x
```

The following commands are supported (in my ulgy written shell).  

cd:  
change directory to parent dir:  
cd ..   
change directory to relative dir:  
cd code  
change directory to absolute dir:  
cd /vol/storage_mlc01  

ls:  
lists content of the directory  

dl:  
downloads a file.  
dl filename [targetdir]  

dlfp:  
same as dl but keeps the absolute path of the wiiu  

dldir:  
downloads a directory  

downloading the current dir  
dldir   
arguments:  
-src <source folder on wiiu> (sets the source folder on the wiiu)  
-dst <destination folder on wiiu> (sets the destination folder on your PC)  
-fullpath (keeps the absolute path of the wiiu)  


###dumping a disc###  
Command to dump the whole disc (code, content and meta folder) to sd  
```
dumpdisc
```

The result will be stored on sd:/dumps/[TITLEID]  
  
You can set a regular expression all files and dir will be checked to.  
*Examples*  
To dump only the code folder
```
dumpdisc -file /code/.*  
```

To disable the check on dir (and so check the pattern on ALL files of the disc) use the -deepSearch parameter  
  
Example: to dump all .szs files
```
dumpdisc -file .*.szs -deepSearch
```

Server and smea's client.  
https://github.com/smealum/iosuhax/tree/master/wupserver  
 
Everything is from Smea, I'm just porting and extending it!