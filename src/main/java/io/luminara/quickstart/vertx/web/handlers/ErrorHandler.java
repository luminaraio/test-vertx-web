package io.luminara.quickstart.vertx.web.handlers;

import io.luminara.quickstart.vertx.web.MediaTypes;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

  public static void error(RoutingContext routingContext, int code, String message) {
    LOGGER.debug("In ErrorHandler.error(..) => gRPC: code: {}, message: {}", code, message);
    routingContext
      .put("message", message)
      .fail(code);
  }

  @Override
  public void handle(RoutingContext routingContext) {
    LOGGER.trace("In ErrorHandler.handle(..)");

    JsonObject problemJson = new JsonObject();
    problemJson.put("status", HttpResponseStatus.BAD_REQUEST.reasonPhrase());
    problemJson.put("timestamp", LocalDateTime.now()
      .format(DateTimeFormatter.ISO_DATE_TIME));

    routingContext.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
      .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
      .end(problemJson.encode());
  }

}
