A port of smea's client (WUPClient.py) for the wupserver.

Open it with
```
java -jar jwupclient 192.168.x.x
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

Server and smea's client.
https://github.com/smealum/iosuhax/tree/master/wupserver

Everything is from Smea, I'm just porting and extending it!