# Distributed-Filestore-Using-gRPC-and-Zookeeper!

A distributed filestore using the concepts of HDFS and GFS.
[image](https://user-images.githubusercontent.com/26243594/158458792-d9c62c23-f77a-474a-a358-260c1fae8ab1.png)
There will be two important entities:
  FileStore
  NameServer

FileStore:
  FileStore will store all the files, will know the names of the files
  
  Operations that FileStore can perform:
  Create: given a name and version number and chain of replicas, a file will be written to a chain of replicas and stored with the given name and version number.
  Read: given a name, you will get back a packet a meta data containing the version number and length followed by a stream of requested chunks of the file.
  Delete: given a name and version number, a file will be recorded as deleted. (the file store need not contain the file for it to be deleted.)
  listFilesAndTombstones() -  this will return all the files and tombstones stored by the file store. They will have the form (name, version, isTombstone)
  copyFile(path, hostPort) - this will cause the FileStore to copy a file to itself from another FileStore.
  bumpVersion(listOfPaths, newVersion) - this will cause the FileStore to update all of the listed paths with the new version number.

NameServer:
  The nameserver will only have a soft state. Apache Zookeeper will be used to find the nameserver.
  The features of NameServer are:
  Management of the global namespace of files: The NameServer will provide addresses of FileStores to read and create files. The NameServer will also delete files.
  Replica management: The NameServer will cause under replicated files to be replicated to other FileStores.
  Garbage collection: The NameServer will use a minimum version number to allow FileStores to cleanup old files and tombstones.

  Oracle (Implemented using Zookeeper)
    An important component to all of this is the oracle that will provide the following information:
    The current NameServer (/leader). This will have two lines: first the host:port of the NameServer service and the second line the name of the server(ip addr) running the service. All potential NameServers should try an create this file to become the active NameServer. If it becomes an active NameServer it should stay that way until the program exits or ZooKeeper says that its session expired. Potential NameServers should watch this znode and try to become active if the znode goes away. FileStores should also watch this znode to find out which NameServer they should talk to.
    The minimum version (/minver). This will have an integer (represented as a string) that will indicate the minimum version of any active file or tombstone. FileStores will watch this znode and delete any files or tombstones they have with versions less than /minver.
    The max version number (/maxver). This will have an integer (represented as a string) that is the max version number that the current NameServer will assign. When a new NameServer takes over, it will read /maxver and start assigning version numbers at /maxver+1 it will also set a new /maxver, which for this assignment will be /maxver+10. If the NameServer needs a higher version number than the current /maxver, it will need to bump /maxver by 10 before using a higher number.

  NameServer interface
    The NameServer will provide services to clients and FileStores.
    The client interface consists of:
    doCreate(path) - this will return a list of FileStore host:port pairs and a version number to use to create the file. The NameNode doesn't actually use the path component since the file will not be considered created until the FileStores report in.
    doDelete(path) - this will return success or failure. The name node will contact the FileStores that store the file and delete them. 
    doRead(path) - this will return the list of host:port pairs that store the file.
    list(glob_expression) - this will return a list of all file paths that match the expression.

  The FileStore will call the following methods on NameServer:
    registerFilesAndTombstones(list) - this will list all the files and tombstones stored by the file store. They will have the form (name, version, isTombstone)
    heartBeat(stats) - this will happen every 15 seconds and will have the amount of bytes stored by the FileStore and the bytes available. (The quantities do not need to be accurate. You can use zero.) If two hearbeats are missed, the NameServer will consider the FileStore unavailable and stop tracking it and discard information about the files and tombstones it stores.
    addFileOrTombstone(data) - this will have the form (name, version, isTombstone) for a new file or tombstone stored by the FileStore. This will be called by the FileStore whenever it gets a new file or tombstone.

  NameServer operation
    Apart from implementing the methods above. The NameServer will start up and wait 30 seconds for all FileStores to report to it before serving clients. It will collect all the reported information into a database that will be used to service clients.
    Within 60 seconds after becoming the NameServer should make sure that all the all the files and tombstones are replicated on at least 3 FileStores.
    Every 60 seconds if more than 10 new version numbers have been used, the NameServer will bumpVersion on all the active files on all the FileStores (it must make sure not to bump the versions outdated files on a FileStore). The version used must be at least 120 seconds old. This means that the NameServer will periodically allocate a version number to be used in the future. It will then make sure that all files are fully replicated. Finally, it will change /minver to the bumped version number.
