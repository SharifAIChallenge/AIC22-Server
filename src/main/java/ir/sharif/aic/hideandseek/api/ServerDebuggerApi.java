package ir.sharif.aic.hideandseek.api;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.Debug;
import ir.sharif.aic.hideandseek.api.grpc.ServiceDebuggerGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Arrays;
import java.util.Map;

@GrpcService
@RequiredArgsConstructor
public class ServerDebuggerApi extends ServiceDebuggerGrpc.ServiceDebuggerImplBase {
    @Override
    public void getThreadDumps(Empty request, StreamObserver<Debug.ThreadDump> responseObserver) {
        String threadDump = getThreadDump();
        responseObserver.onNext(Debug.ThreadDump.newBuilder().setThreadDump(threadDump).build());
        responseObserver.onCompleted();
    }

    private String getThreadDump() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
            stringBuilder.append(threadEntry.getKey().getId()).append("\n");
            Arrays.stream(threadEntry.getValue()).map(StackTraceElement::toString).forEach(s -> stringBuilder.append(s).append("\n"));
        }
        return stringBuilder.toString();
    }
}
