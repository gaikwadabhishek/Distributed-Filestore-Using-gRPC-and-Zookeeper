package filestore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import com.google.protobuf.ByteString;

import grpc.FileStoreGrpc;
import grpc.Qddfs;
import grpc.FileStoreGrpc.FileStoreImplBase;
import grpc.Qddfs.BumpVersionReply;
import grpc.Qddfs.BumpVersionRequest;
import grpc.Qddfs.CreateFileReply;
import grpc.Qddfs.CreateFileRequest;
import grpc.Qddfs.DeleteFileReply;
import grpc.Qddfs.DeleteFileRequest;
import grpc.Qddfs.FileClose;
import grpc.Qddfs.FileCreate;
import grpc.Qddfs.FileData;
import grpc.Qddfs.FileRead;
import grpc.Qddfs.ListReply;
import grpc.Qddfs.ListRequest;
import grpc.Qddfs.NSAddReply;
import grpc.Qddfs.NSAddRequest;
import grpc.Qddfs.OpenResult;
import grpc.Qddfs.ReadFileReply;
import grpc.Qddfs.ReadFileRequest;
import grpc.Qddfs.ReadRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class FileStoreImpl extends FileStoreImplBase {

	@Override
	public StreamObserver<CreateFileRequest> createFile(StreamObserver<CreateFileReply> responseObserver) {
		return new StreamObserver<CreateFileRequest>() {
			String originalFileName;
			String name;
			int version;
			String next;
			ArrayList<String> chainList = new ArrayList<>();
			CountDownLatch latch = new CountDownLatch(1);
			StreamObserver<CreateFileRequest> requestObserver;
			BufferedOutputStream bufferedStream;
			File file;

			@Override
			public void onNext(CreateFileRequest request) {
				if (request.hasCreate()) {

					try {
						NSConfig.finalRC = 0;
						originalFileName = request.getCreate().getName();

						name = NSConfig.FILESTORE_FOLDER + originalFileName.replace("/", "").replace(":", "");

						version = request.getCreate().getVersion();

						if (NSConfig.metaData.containsKey(originalFileName)) {
							FSEntry storedFile = NSConfig.metaData.get(originalFileName);

							if ((!storedFile.isTombstone && storedFile.getVersion() > version)
									|| (storedFile.isTombstone && storedFile.version >= version)) {

								System.out.println("old version received");
								System.out.println("sending rc = 1 to client");
								NSConfig.finalRC = 3;
								responseObserver.onNext(
										CreateFileReply.newBuilder().setRc(1).setMessage("version too old").build());

								responseObserver.onCompleted();
								return;
							}
						}
						System.out.println("Creating a file at :" + name);

						// temporary storing the file
						file = new File(name + "_temp");
						bufferedStream = new BufferedOutputStream(new FileOutputStream(file));

						// send create info to next
						if (request.getCreate().getChainCount() > 0) {

							System.out.println("HOST PORT LIST NOT EMPTY");
							next = request.getCreate().getChain(0);
							System.out.println("Next IP : " + next);

							for (int i = 1; i < request.getCreate().getChainCount(); i++) {
								chainList.add(request.getCreate().getChain(i));
							}
							System.out.println("ChainList: " + chainList.toString());

							ManagedChannel channel = ManagedChannelBuilder.forTarget(next).usePlaintext().build();

							requestObserver = returnFileStoreGrpcStubForNext(channel);

							System.out.println("Sending Filename to Next: " + originalFileName);

							System.out.println("Sending CreateFileReq to Next ");
							requestObserver.onNext(CreateFileRequest.newBuilder().setCreate(FileCreate.newBuilder()
									.setName(originalFileName).setVersion(version).addAllChain(chainList).build())
									.build());

						} else {
							// I'm the last or only one
							latch.countDown();
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				} else if (NSConfig.finalRC == 0 && request.hasData()) {

					long offset = request.getData().getOffset();
					ByteString data = request.getData().getData();

					// send data to successor if it exists
					if (next != null && next.length() > 0) {
						System.out.print("Sending Data Request to Next");
						requestObserver.onNext(CreateFileRequest.newBuilder()
								.setData(FileData.newBuilder().setData(data).setOffset(offset).build()).build());
					}

					try {
						bufferedStream.write(data.toByteArray());
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else if (NSConfig.finalRC == 0 && request.hasClose()) {
					try {

						if (next != null && next.length() > 0) {
							requestObserver.onNext(
									CreateFileRequest.newBuilder().setClose(FileClose.newBuilder().build()).build());
							requestObserver.onCompleted();
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}

			private StreamObserver<CreateFileRequest> returnFileStoreGrpcStubForNext(ManagedChannel channel) {

				return FileStoreGrpc.newStub(channel).createFile(new StreamObserver<Qddfs.CreateFileReply>() {

					int tempRc = 0;

					@Override
					public void onNext(CreateFileReply value) {
						tempRc = value.getRc();
						System.out.println("RC received from next node: " + tempRc);
					}

					@Override
					public void onError(Throwable t) {
						t.printStackTrace();
						tempRc = 2;
						latch.countDown();
					}

					@Override
					public void onCompleted() {
						// decrement latch
						// finalRC send outside which we are getting from successor
						NSConfig.finalRC = tempRc;
						latch.countDown();
					}
				});
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void onCompleted() {
				// client is done
				// send rc from successor

				try {
					if (next != null) {
						System.out.println("Waiting for ack");
						latch.await();
					}

					if (NSConfig.finalRC == 0) {
						FSEntry entry = new FSEntry(originalFileName, version, file.length(), false);
						NSConfig.metaData.put(originalFileName, entry);

						NSConfig.mapper.writeValue(new File(NSConfig.FILESTORE_FOLDER + "metaData.json"),
								NSConfig.metaData.values());

						// call add file or tombstones
						if (FileStore.isNameServerAvailable) {
							try {
								NSAddRequest addFileRequest = NSAddRequest.newBuilder()
										.setHostPort(NSConfig.FILE_STORE_HOST_PORT)
										.setEntry(grpc.Qddfs.FSEntry.newBuilder().setName(originalFileName)
												.setVersion(version).setSize(file.length()).setIsTombstone(false)
												.build())
										.build();
								NSAddReply reply = FileStore.nameServer.addFileOrTombstone(addFileRequest);
								if (reply.getRc() == 0) {
									System.out.println("File added to nameserver");
								}
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("File NOT added to nameserver");
							}

						}

						Files.deleteIfExists(new File(name).toPath());
						file.createNewFile();
						bufferedStream.close();
						try {
							file.renameTo(new File(name));

						} catch (Exception e) {
							e.printStackTrace();
						}
						responseObserver.onNext(
								CreateFileReply.newBuilder().setRc(NSConfig.finalRC).setMessage("success").build());

					} else if (NSConfig.finalRC == 1) {
						// version old
						if (bufferedStream != null) {
							bufferedStream.flush();
							bufferedStream.close();

						}

						responseObserver.onNext(CreateFileReply.newBuilder().setRc(NSConfig.finalRC)
								.setMessage("version too old").build());

					} else if (NSConfig.finalRC == 2) {
						// error occurred
						if (bufferedStream != null) {
							bufferedStream.flush();
							bufferedStream.close();

						}

						responseObserver.onNext(CreateFileReply.newBuilder().setRc(NSConfig.finalRC)
								.setMessage("another error occurred").build());

					} else {
						// any other error
						if (bufferedStream != null) {
							bufferedStream.flush();
							bufferedStream.close();

						}
						// file not deleted due to successor reject
						Files.deleteIfExists(new File(name + "_temp").toPath());

					}

					responseObserver.onCompleted();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		};
	}

	@Override
	public StreamObserver<ReadFileRequest> readFile(StreamObserver<ReadFileReply> responseObserver) {

		return new StreamObserver<ReadFileRequest>() {

			FileChannel channel;
			ByteBuffer data;
			String fileName;
			String originalFileName;
			RandomAccessFile raf;

			@Override
			public void onNext(ReadFileRequest value) {

				OpenResult open = null;
				if (value.hasRead()) {
					FileRead read = value.getRead();
					originalFileName = read.getName();
					fileName = originalFileName.replaceAll(":", "").replaceAll("/", "");
					System.out.println("read request received for file: " + fileName);
					if (NSConfig.metaData.containsKey(originalFileName)) {
						FSEntry entry = NSConfig.metaData.get(originalFileName);
						int version = entry.getVersion();
						if (entry.isTombstone()) {
							open = OpenResult.newBuilder().setRc(1).setVersion(version)
									.setError("File has been deleted").setLength(-1).build();
							responseObserver.onNext(ReadFileReply.newBuilder().setOpen(open).build());
							responseObserver.onCompleted();
						} else {
							String file = NSConfig.FILESTORE_FOLDER + fileName;
							try {
								raf = new RandomAccessFile(file, "rw");
								try {
									open = OpenResult.newBuilder().setRc(0).setVersion(version).setLength(raf.length())
											.build();
								} catch (IOException e) {
									e.printStackTrace();
								}
								responseObserver.onNext(ReadFileReply.newBuilder().setOpen(open).build());
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}
					} else {
						open = OpenResult.newBuilder().setRc(1).setVersion(-1).setError("File not present")
								.setLength(-1).build();
						responseObserver.onNext(ReadFileReply.newBuilder().setOpen(open).build());
						responseObserver.onCompleted();
					}
					if (raf == null) {
						open = OpenResult.newBuilder().setRc(2).setVersion(-1).setError("file is empty").setLength(-1)
								.build();
						responseObserver.onNext(ReadFileReply.newBuilder().setOpen(open).build());
						responseObserver.onCompleted();
					}
				}
				if (value.hasReq()) {
					ReadRequest request = value.getReq();
					int length = request.getLength();
					long offset = request.getOffset();

					if (NSConfig.metaData.containsKey(originalFileName)
							&& !NSConfig.metaData.get(originalFileName).isTombstone()) {
						try {
							channel = raf.getChannel();
							data = ByteBuffer.allocate(length);
							int writtenBytes = channel.read(data, offset);
							ByteString byteStr = ByteString.copyFrom(data.flip());
							FileData data = FileData.newBuilder().setData(byteStr).setOffset(offset).build();
							responseObserver.onNext(ReadFileReply.newBuilder().setData(data).build());

							if (writtenBytes != length) {
								return;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				if (value.hasClose()) {
					responseObserver.onCompleted();
				}
			}

			@Override
			public void onError(Throwable t) {
				responseObserver.onCompleted();
			}

			@Override
			public void onCompleted() {
				try {
					if (channel != null)
						channel.close();
					if (raf != null)
						raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				responseObserver.onCompleted();
			}
		};
	}

	@Override
	public void deleteFile(DeleteFileRequest request, StreamObserver<DeleteFileReply> responseObserver) {

		int rc = 0; // success
		String message = "deleted successfully";
		String originalFileName;
		try {
			originalFileName = request.getName();
			String name = originalFileName.replace("/", "").replace(":", "");

			int version = request.getVersion();
			System.out.println("delete request received for file: " + name);

			if (NSConfig.metaData.containsKey(originalFileName)) {
				FSEntry entry = NSConfig.metaData.get(originalFileName);

				if (version < entry.getVersion()) {
					rc = 1;
					message = "version too old";
				}
			}

			// file doesn't exist
			if (rc == 0) {
				try {
					Files.deleteIfExists(new File(NSConfig.FILESTORE_FOLDER + name).toPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				FSEntry entry;
				if (NSConfig.metaData.containsKey(originalFileName)) {
					entry = NSConfig.metaData.get(originalFileName);
					entry.setTombstone(true);
					entry.setVersion(version);
				} else {
					entry = new FSEntry(originalFileName, version, -1, true);
				}
				NSConfig.metaData.put(originalFileName, entry);

				// register tombstone with nameserver
				if (FileStore.isNameServerAvailable) {
					try {
						NSAddRequest addFileRequest = NSAddRequest.newBuilder()
								.setHostPort(NSConfig.FILE_STORE_HOST_PORT)
								.setEntry(grpc.Qddfs.FSEntry.newBuilder().setName(originalFileName).setVersion(version)
										.setSize(0).setIsTombstone(true).build())
								.build();
						NSAddReply reply = FileStore.nameServer.addFileOrTombstone(addFileRequest);
						if (reply.getRc() == 0) {
							System.out.println("File added to nameserver");
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Tombstone not added to filestore due to error");
					}
				}
			}

			// write to file

			NSConfig.mapper.writeValue(new File(NSConfig.FILESTORE_FOLDER + "metaData.json"),
					NSConfig.metaData.values());
		} catch (Exception e) {
			rc = 2;
			e.printStackTrace();
			message = "another error occurred";
		}

		responseObserver.onNext(DeleteFileReply.newBuilder().setMessage(message).setRc(rc).build());
		responseObserver.onCompleted();

	}

	@Override
	public void list(ListRequest request, StreamObserver<ListReply> responseObserver) {

		List<grpc.Qddfs.FSEntry> entries = new ArrayList<>();

		for (Entry<String, FSEntry> entry : NSConfig.metaData.entrySet()) {
			FSEntry entryValue = entry.getValue();
			grpc.Qddfs.FSEntry qddfsEntry = grpc.Qddfs.FSEntry.newBuilder().setName(entryValue.getName())
					.setVersion(entryValue.getVersion()).setSize(entryValue.getSize())
					.setIsTombstone(entryValue.isTombstone()).build();
			entries.add(qddfsEntry);
		}

		ListReply reply = ListReply.newBuilder().addAllEntries(entries).build();
		responseObserver.onNext(reply);
		responseObserver.onCompleted();

	}

	@Override
	public void bumpVersion(BumpVersionRequest request, StreamObserver<BumpVersionReply> responseObserver) {
		List<String> filesToBump = request.getNameList();
		int newVersion = request.getNewVersion();
		boolean metaDataChanged = false;
		for (String fileName : filesToBump) {
			if (NSConfig.metaData.containsKey(fileName)) {
				FSEntry versionObj = NSConfig.metaData.get(fileName);
				if (newVersion > versionObj.getVersion()) {
					metaDataChanged = true;
					versionObj.setVersion(newVersion);
					NSConfig.metaData.put(fileName, versionObj);

					if (FileStore.isNameServerAvailable) {
						try {
							NSAddRequest addFileRequest = NSAddRequest.newBuilder()
									.setHostPort(NSConfig.FILE_STORE_HOST_PORT)
									.setEntry(grpc.Qddfs.FSEntry.newBuilder().setName(fileName)
											.setVersion(versionObj.getVersion()).setSize(versionObj.getSize())
											.setIsTombstone(versionObj.isTombstone()).build())
									.build();
							NSAddReply reply = FileStore.nameServer.addFileOrTombstone(addFileRequest);
							if (reply.getRc() == 0) {
								System.out.println("File added to nameserver");
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("File not added to nameserver");
						}
					}
				}
			}
		}
		if (metaDataChanged) {
			try {
				NSConfig.mapper.writeValue(new File(NSConfig.FILESTORE_FOLDER + "metaData.json"),
						NSConfig.metaData.values());
			} catch (IOException e) {
				System.out.println("Error while writing to metaData.json");
				e.printStackTrace();
			}
		}

		BumpVersionReply rep = BumpVersionReply.newBuilder().build();
		responseObserver.onNext(rep);
		responseObserver.onCompleted();
	}

}
