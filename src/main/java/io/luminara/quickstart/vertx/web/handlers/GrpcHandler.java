package io.luminara.quickstart.vertx.web.handlers;

import com.google.rpc.Status;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Created by Luminara Team.
 */
public interface GrpcHandler {
  Logger LOGGER = LoggerFactory.getLogger(GrpcHandler.class);

  public default <T> void getStub(Vertx vertx,
                                  ServiceDiscovery discovery,
                                  JsonObject serviceFilter,
                                  Function<Channel, T> stubProvider,
                                  Handler<AsyncResult<T>> resultHandler) {
    discovery.getRecord(serviceFilter, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        if (ar.result() == null) {
          resultHandler.handle(
            Future.failedFuture("Cannot find service" +
              " ( Filter =" + serviceFilter + ") In the service registry"));
        } else {
          Record record = ar.result();
          LOGGER.info("Found gRPC service in the service discovery backend. " +
            "\nRecord details: " +
            "\nname: {} " +
            "\nlocation: {}", record.getName(), record.getLocation());
          ManagedChannel vertxChannel = VertxChannelBuilder
            .forAddress(vertx, record.getLocation().getString("host"),
              record.getLocation().getInteger("port"))
            .usePlaintext(true)
            .build();
          T vertxStub = stubProvider.apply(vertxChannel);
          resultHandler.handle(Future.succeededFuture(vertxStub));
        }
      }
    });
  }

  public default <T> void processServiceError(RoutingContext routingContext, AsyncResult<T> serviceResult) {
    StatusRuntimeException serviceException = (StatusRuntimeException) serviceResult.cause();

    Status status = StatusProto.fromStatusAndTrailers(serviceException.getStatus(), serviceException.getTrailers());
    if ((status != null) && (status.getDetailsList() != null) && (!status.getDetailsList().isEmpty())) {
      LOGGER.error("Could not recognise gRPC error: {}", status);
      ErrorHandler.error(routingContext, status.getCode(), status.getMessage());
    }
  }
}
