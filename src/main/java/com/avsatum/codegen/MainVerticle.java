package com.avsatum.codegen;

import com.avsatum.codegen.model.AbstractResponse;
import com.avsatum.codegen.model.ErrorResponse;
import com.avsatum.codegen.model.GeneratorResponse;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {
	private HttpServer server;

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		DeploymentOptions options = new DeploymentOptions().setInstances(10);

		vertx.deployVerticle("com.avsatum.codegen.HttpClient", options, res -> {
			if (res.succeeded()) {
				String deploymentID = res.result();
				System.out.println("Other verticle deployed ok, deploymentID = " + deploymentID);
			} else {
				res.cause().printStackTrace();
				System.exit(1);
			}
		});

		Router router = Router.router(vertx);
		router.get("/api/:param").handler(this::generateCode);

		server = vertx.createHttpServer().requestHandler(router::accept).listen(8080, http -> {
			if (http.succeeded()) {
				startFuture.complete();
				System.out.println("HTTP server started on http://localhost:8080");
			} else {
				startFuture.fail(http.cause());
			}
		});
	}

	private void generateCode(RoutingContext routingContext) {
		String paramValue = routingContext.request().getParam("param");

		DeliveryOptions opts = new DeliveryOptions();
		opts.setSendTimeout(1000);

		vertx.eventBus().send("com.avsatum.quote", paramValue, opts, ar -> {
			AbstractResponse response = null;
			if (ar.succeeded()) {
				System.out.println("Received reply: " + ar.result().body());
				response = new GeneratorResponse();
				((GeneratorResponse) response).setCode(ar.result().body().toString());
			} else {
				response = new ErrorResponse();
				((ErrorResponse) response).setMessage(ar.cause().getMessage());
			}
			routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
					.end(Json.encodePrettily(response));
		});
	}
}
