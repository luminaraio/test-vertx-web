package io.luminara.quickstart.vertx.web.handlers;

import io.luminara.quickstart.vertx.GreeterGrpc;
import io.luminara.quickstart.vertx.HelloReply;
import io.luminara.quickstart.vertx.HelloRequest;
import io.luminara.quickstart.vertx.web.MediaTypes;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Created by Luminara Team.
 */
public class HelloHandler implements GrpcHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(HelloHandler.class);
  private static final String SERVICE_NAME = "test-vertx-grpc";

  private final Vertx vertx;
  private final ServiceDiscovery discovery;
  private final JsonObject serviceFilter;

  private HelloHandler(Vertx vertx, ServiceDiscovery discovery) {
    this.vertx = vertx;
    this.discovery = discovery;
    this.serviceFilter = new JsonObject()
      .put("name", SERVICE_NAME);
  }

  public static HelloHandler create(Vertx vertx, ServiceDiscovery discovery) {
    return new HelloHandler(vertx, discovery);
  }

  public void handleSayHello(RoutingContext routingContext) {
    LOGGER.trace("In HelloHandler.handleSayHello(..)");
    getStub(vertx, discovery, serviceFilter, GreeterGrpc::newVertxStub,
      proxyAsyncResult -> sayHello(routingContext, proxyAsyncResult));
  }

  private void sayHello(RoutingContext routingContext, AsyncResult<GreeterGrpc.GreeterVertxStub> proxyAsyncResult) {
    if (proxyAsyncResult.succeeded()) {
      GreeterGrpc.GreeterVertxStub service = proxyAsyncResult.result();
      JsonObject payload = routingContext.getBodyAsJson();
      HelloRequest request =
        HelloRequest.newBuilder()
          .setName(payload.getString("name"))
          .build();
      service.sayHello(request, serviceResult ->
        processServiceResponse(routingContext, HttpResponseStatus.OK, serviceResult));
    } else {
      LOGGER.error("ERROR while proxying request", proxyAsyncResult.cause());
      routingContext.fail(proxyAsyncResult.cause());
    }
  }

  private void processServiceResponse(RoutingContext routingContext, HttpResponseStatus httpStatus,
                                      AsyncResult<HelloReply> serviceResult) {
    if (serviceResult.succeeded()) {
      routingContext
        .response()
        .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
        .setStatusCode(OK.code())
        .end(new JsonObject().put("message", serviceResult.result().getMessage()).encode());
    } else {
      processServiceError(routingContext, serviceResult);
    }
  }
}
