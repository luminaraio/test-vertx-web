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

public class HealthCheckHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckHandler.class);

  @Override
  public void handle(RoutingContext routingContext) {
    JsonObject response = new JsonObject();
    response.put("status", HttpResponseStatus.OK.reasonPhrase());
    response.put("timestamp",
      LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));


    routingContext.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
      .setStatusCode(HttpResponseStatus.OK.code())
      .end(response.encode());
  }
}
