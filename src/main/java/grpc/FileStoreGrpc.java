package grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: qddfs.proto")
public final class FileStoreGrpc {

  private FileStoreGrpc() {}

  public static final String SERVICE_NAME = "qddfs.FileStore";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<grpc.Qddfs.CreateFileRequest,
      grpc.Qddfs.CreateFileReply> getCreateFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "createFile",
      requestType = grpc.Qddfs.CreateFileRequest.class,
      responseType = grpc.Qddfs.CreateFileReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<grpc.Qddfs.CreateFileRequest,
      grpc.Qddfs.CreateFileReply> getCreateFileMethod() {
    io.grpc.MethodDescriptor<grpc.Qddfs.CreateFileRequest, grpc.Qddfs.CreateFileReply> getCreateFileMethod;
    if ((getCreateFileMethod = FileStoreGrpc.getCreateFileMethod) == null) {
      synchronized (FileStoreGrpc.class) {
        if ((getCreateFileMethod = FileStoreGrpc.getCreateFileMethod) == null) {
          FileStoreGrpc.getCreateFileMethod = getCreateFileMethod = 
              io.grpc.MethodDescriptor.<grpc.Qddfs.CreateFileRequest, grpc.Qddfs.CreateFileReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "qddfs.FileStore", "createFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.CreateFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.CreateFileReply.getDefaultInstance()))
                  .setSchemaDescriptor(new FileStoreMethodDescriptorSupplier("createFile"))
                  .build();
          }
        }
     }
     return getCreateFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.Qddfs.ReadFileRequest,
      grpc.Qddfs.ReadFileReply> getReadFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "readFile",
      requestType = grpc.Qddfs.ReadFileRequest.class,
      responseType = grpc.Qddfs.ReadFileReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<grpc.Qddfs.ReadFileRequest,
      grpc.Qddfs.ReadFileReply> getReadFileMethod() {
    io.grpc.MethodDescriptor<grpc.Qddfs.ReadFileRequest, grpc.Qddfs.ReadFileReply> getReadFileMethod;
    if ((getReadFileMethod = FileStoreGrpc.getReadFileMethod) == null) {
      synchronized (FileStoreGrpc.class) {
        if ((getReadFileMethod = FileStoreGrpc.getReadFileMethod) == null) {
          FileStoreGrpc.getReadFileMethod = getReadFileMethod = 
              io.grpc.MethodDescriptor.<grpc.Qddfs.ReadFileRequest, grpc.Qddfs.ReadFileReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "qddfs.FileStore", "readFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.ReadFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.ReadFileReply.getDefaultInstance()))
                  .setSchemaDescriptor(new FileStoreMethodDescriptorSupplier("readFile"))
                  .build();
          }
        }
     }
     return getReadFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.Qddfs.DeleteFileRequest,
      grpc.Qddfs.DeleteFileReply> getDeleteFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "deleteFile",
      requestType = grpc.Qddfs.DeleteFileRequest.class,
      responseType = grpc.Qddfs.DeleteFileReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.Qddfs.DeleteFileRequest,
      grpc.Qddfs.DeleteFileReply> getDeleteFileMethod() {
    io.grpc.MethodDescriptor<grpc.Qddfs.DeleteFileRequest, grpc.Qddfs.DeleteFileReply> getDeleteFileMethod;
    if ((getDeleteFileMethod = FileStoreGrpc.getDeleteFileMethod) == null) {
      synchronized (FileStoreGrpc.class) {
        if ((getDeleteFileMethod = FileStoreGrpc.getDeleteFileMethod) == null) {
          FileStoreGrpc.getDeleteFileMethod = getDeleteFileMethod = 
              io.grpc.MethodDescriptor.<grpc.Qddfs.DeleteFileRequest, grpc.Qddfs.DeleteFileReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "qddfs.FileStore", "deleteFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.DeleteFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.DeleteFileReply.getDefaultInstance()))
                  .setSchemaDescriptor(new FileStoreMethodDescriptorSupplier("deleteFile"))
                  .build();
          }
        }
     }
     return getDeleteFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.Qddfs.ListRequest,
      grpc.Qddfs.ListReply> getListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "list",
      requestType = grpc.Qddfs.ListRequest.class,
      responseType = grpc.Qddfs.ListReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.Qddfs.ListRequest,
      grpc.Qddfs.ListReply> getListMethod() {
    io.grpc.MethodDescriptor<grpc.Qddfs.ListRequest, grpc.Qddfs.ListReply> getListMethod;
    if ((getListMethod = FileStoreGrpc.getListMethod) == null) {
      synchronized (FileStoreGrpc.class) {
        if ((getListMethod = FileStoreGrpc.getListMethod) == null) {
          FileStoreGrpc.getListMethod = getListMethod = 
              io.grpc.MethodDescriptor.<grpc.Qddfs.ListRequest, grpc.Qddfs.ListReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "qddfs.FileStore", "list"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.ListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.ListReply.getDefaultInstance()))
                  .setSchemaDescriptor(new FileStoreMethodDescriptorSupplier("list"))
                  .build();
          }
        }
     }
     return getListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.Qddfs.CopyFileRequest,
      grpc.Qddfs.CopyFileReply> getCopyFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "copyFile",
      requestType = grpc.Qddfs.CopyFileRequest.class,
      responseType = grpc.Qddfs.CopyFileReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.Qddfs.CopyFileRequest,
      grpc.Qddfs.CopyFileReply> getCopyFileMethod() {
    io.grpc.MethodDescriptor<grpc.Qddfs.CopyFileRequest, grpc.Qddfs.CopyFileReply> getCopyFileMethod;
    if ((getCopyFileMethod = FileStoreGrpc.getCopyFileMethod) == null) {
      synchronized (FileStoreGrpc.class) {
        if ((getCopyFileMethod = FileStoreGrpc.getCopyFileMethod) == null) {
          FileStoreGrpc.getCopyFileMethod = getCopyFileMethod = 
              io.grpc.MethodDescriptor.<grpc.Qddfs.CopyFileRequest, grpc.Qddfs.CopyFileReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "qddfs.FileStore", "copyFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.CopyFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.CopyFileReply.getDefaultInstance()))
                  .setSchemaDescriptor(new FileStoreMethodDescriptorSupplier("copyFile"))
                  .build();
          }
        }
     }
     return getCopyFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.Qddfs.BumpVersionRequest,
      grpc.Qddfs.BumpVersionReply> getBumpVersionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "bumpVersion",
      requestType = grpc.Qddfs.BumpVersionRequest.class,
      responseType = grpc.Qddfs.BumpVersionReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.Qddfs.BumpVersionRequest,
      grpc.Qddfs.BumpVersionReply> getBumpVersionMethod() {
    io.grpc.MethodDescriptor<grpc.Qddfs.BumpVersionRequest, grpc.Qddfs.BumpVersionReply> getBumpVersionMethod;
    if ((getBumpVersionMethod = FileStoreGrpc.getBumpVersionMethod) == null) {
      synchronized (FileStoreGrpc.class) {
        if ((getBumpVersionMethod = FileStoreGrpc.getBumpVersionMethod) == null) {
          FileStoreGrpc.getBumpVersionMethod = getBumpVersionMethod = 
              io.grpc.MethodDescriptor.<grpc.Qddfs.BumpVersionRequest, grpc.Qddfs.BumpVersionReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "qddfs.FileStore", "bumpVersion"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.BumpVersionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.Qddfs.BumpVersionReply.getDefaultInstance()))
                  .setSchemaDescriptor(new FileStoreMethodDescriptorSupplier("bumpVersion"))
                  .build();
          }
        }
     }
     return getBumpVersionMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static FileStoreStub newStub(io.grpc.Channel channel) {
    return new FileStoreStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static FileStoreBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new FileStoreBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static FileStoreFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new FileStoreFutureStub(channel);
  }

  /**
   */
  public static abstract class FileStoreImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<grpc.Qddfs.CreateFileRequest> createFile(
        io.grpc.stub.StreamObserver<grpc.Qddfs.CreateFileReply> responseObserver) {
      return asyncUnimplementedStreamingCall(getCreateFileMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<grpc.Qddfs.ReadFileRequest> readFile(
        io.grpc.stub.StreamObserver<grpc.Qddfs.ReadFileReply> responseObserver) {
      return asyncUnimplementedStreamingCall(getReadFileMethod(), responseObserver);
    }

    /**
     */
    public void deleteFile(grpc.Qddfs.DeleteFileRequest request,
        io.grpc.stub.StreamObserver<grpc.Qddfs.DeleteFileReply> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteFileMethod(), responseObserver);
    }

    /**
     */
    public void list(grpc.Qddfs.ListRequest request,
        io.grpc.stub.StreamObserver<grpc.Qddfs.ListReply> responseObserver) {
      asyncUnimplementedUnaryCall(getListMethod(), responseObserver);
    }

    /**
     */
    public void copyFile(grpc.Qddfs.CopyFileRequest request,
        io.grpc.stub.StreamObserver<grpc.Qddfs.CopyFileReply> responseObserver) {
      asyncUnimplementedUnaryCall(getCopyFileMethod(), responseObserver);
    }

    /**
     */
    public void bumpVersion(grpc.Qddfs.BumpVersionRequest request,
        io.grpc.stub.StreamObserver<grpc.Qddfs.BumpVersionReply> responseObserver) {
      asyncUnimplementedUnaryCall(getBumpVersionMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCreateFileMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                grpc.Qddfs.CreateFileRequest,
                grpc.Qddfs.CreateFileReply>(
                  this, METHODID_CREATE_FILE)))
          .addMethod(
            getReadFileMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                grpc.Qddfs.ReadFileRequest,
                grpc.Qddfs.ReadFileReply>(
                  this, METHODID_READ_FILE)))
          .addMethod(
            getDeleteFileMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                grpc.Qddfs.DeleteFileRequest,
                grpc.Qddfs.DeleteFileReply>(
                  this, METHODID_DELETE_FILE)))
          .addMethod(
            getListMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                grpc.Qddfs.ListRequest,
                grpc.Qddfs.ListReply>(
                  this, METHODID_LIST)))
          .addMethod(
            getCopyFileMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                grpc.Qddfs.CopyFileRequest,
                grpc.Qddfs.CopyFileReply>(
                  this, METHODID_COPY_FILE)))
          .addMethod(
            getBumpVersionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                grpc.Qddfs.BumpVersionRequest,
                grpc.Qddfs.BumpVersionReply>(
                  this, METHODID_BUMP_VERSION)))
          .build();
    }
  }

  /**
   */
  public static final class FileStoreStub extends io.grpc.stub.AbstractStub<FileStoreStub> {
    private FileStoreStub(io.grpc.Channel channel) {
      super(channel);
    }

    private FileStoreStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FileStoreStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new FileStoreStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<grpc.Qddfs.CreateFileRequest> createFile(
        io.grpc.stub.StreamObserver<grpc.Qddfs.CreateFileReply> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getCreateFileMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<grpc.Qddfs.ReadFileRequest> readFile(
        io.grpc.stub.StreamObserver<grpc.Qddfs.ReadFileReply> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getReadFileMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void deleteFile(grpc.Qddfs.DeleteFileRequest request,
        io.grpc.stub.StreamObserver<grpc.Qddfs.DeleteFileReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void list(grpc.Qddfs.ListRequest request,
        io.grpc.stub.StreamObserver<grpc.Qddfs.ListReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void copyFile(grpc.Qddfs.CopyFileRequest request,
        io.grpc.stub.StreamObserver<grpc.Qddfs.CopyFileReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCopyFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void bumpVersion(grpc.Qddfs.BumpVersionRequest request,
        io.grpc.stub.StreamObserver<grpc.Qddfs.BumpVersionReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getBumpVersionMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class FileStoreBlockingStub extends io.grpc.stub.AbstractStub<FileStoreBlockingStub> {
    private FileStoreBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private FileStoreBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FileStoreBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new FileStoreBlockingStub(channel, callOptions);
    }

    /**
     */
    public grpc.Qddfs.DeleteFileReply deleteFile(grpc.Qddfs.DeleteFileRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeleteFileMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.Qddfs.ListReply list(grpc.Qddfs.ListRequest request) {
      return blockingUnaryCall(
          getChannel(), getListMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.Qddfs.CopyFileReply copyFile(grpc.Qddfs.CopyFileRequest request) {
      return blockingUnaryCall(
          getChannel(), getCopyFileMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.Qddfs.BumpVersionReply bumpVersion(grpc.Qddfs.BumpVersionRequest request) {
      return blockingUnaryCall(
          getChannel(), getBumpVersionMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class FileStoreFutureStub extends io.grpc.stub.AbstractStub<FileStoreFutureStub> {
    private FileStoreFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private FileStoreFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FileStoreFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new FileStoreFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.Qddfs.DeleteFileReply> deleteFile(
        grpc.Qddfs.DeleteFileRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteFileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.Qddfs.ListReply> list(
        grpc.Qddfs.ListRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.Qddfs.CopyFileReply> copyFile(
        grpc.Qddfs.CopyFileRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCopyFileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.Qddfs.BumpVersionReply> bumpVersion(
        grpc.Qddfs.BumpVersionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getBumpVersionMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_DELETE_FILE = 0;
  private static final int METHODID_LIST = 1;
  private static final int METHODID_COPY_FILE = 2;
  private static final int METHODID_BUMP_VERSION = 3;
  private static final int METHODID_CREATE_FILE = 4;
  private static final int METHODID_READ_FILE = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final FileStoreImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(FileStoreImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DELETE_FILE:
          serviceImpl.deleteFile((grpc.Qddfs.DeleteFileRequest) request,
              (io.grpc.stub.StreamObserver<grpc.Qddfs.DeleteFileReply>) responseObserver);
          break;
        case METHODID_LIST:
          serviceImpl.list((grpc.Qddfs.ListRequest) request,
              (io.grpc.stub.StreamObserver<grpc.Qddfs.ListReply>) responseObserver);
          break;
        case METHODID_COPY_FILE:
          serviceImpl.copyFile((grpc.Qddfs.CopyFileRequest) request,
              (io.grpc.stub.StreamObserver<grpc.Qddfs.CopyFileReply>) responseObserver);
          break;
        case METHODID_BUMP_VERSION:
          serviceImpl.bumpVersion((grpc.Qddfs.BumpVersionRequest) request,
              (io.grpc.stub.StreamObserver<grpc.Qddfs.BumpVersionReply>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_FILE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.createFile(
              (io.grpc.stub.StreamObserver<grpc.Qddfs.CreateFileReply>) responseObserver);
        case METHODID_READ_FILE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.readFile(
              (io.grpc.stub.StreamObserver<grpc.Qddfs.ReadFileReply>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class FileStoreBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    FileStoreBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return grpc.Qddfs.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("FileStore");
    }
  }

  private static final class FileStoreFileDescriptorSupplier
      extends FileStoreBaseDescriptorSupplier {
    FileStoreFileDescriptorSupplier() {}
  }

  private static final class FileStoreMethodDescriptorSupplier
      extends FileStoreBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    FileStoreMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (FileStoreGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new FileStoreFileDescriptorSupplier())
              .addMethod(getCreateFileMethod())
              .addMethod(getReadFileMethod())
              .addMethod(getDeleteFileMethod())
              .addMethod(getListMethod())
              .addMethod(getCopyFileMethod())
              .addMethod(getBumpVersionMethod())
              .build();
        }
      }
    }
    return result;
  }
}
