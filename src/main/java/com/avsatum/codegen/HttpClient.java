package com.avsatum.codegen;

import java.io.StringReader;

import com.avsatum.codegen.model.StockResponse;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;

public class HttpClient extends AbstractVerticle {
    private CircuitBreakerOptions options = new CircuitBreakerOptions().setMaxFailures(3).setTimeout(200)
            .setMaxRetries(1).setResetTimeout(5000).setFallbackOnFailure(true);
    final SAXBuilder sax = new SAXBuilder();

    @Override
    public void start() {

        CircuitBreaker breaker = CircuitBreaker.create("http-client-circuit-breaker", vertx, options).openHandler(v -> {
            System.out.println("Circuit opened");
        }).closeHandler(v -> {
            System.out.println("Circuit closed");
        }).halfOpenHandler(v -> {
            System.out.println("Circuit half opened");
        });

        vertx.eventBus().consumer("com.avsatum.quote", message -> {
            Future<String> result = breaker.executeWithFallback(future -> {
                vertx.createHttpClient().getNow(80, "dev.markitondemand.com",
                        "/MODApis/Api/v2/Quote?Symbol=" + message.body(), response -> {
                            if (response.statusCode() != 200) {
                                future.fail("Error: " + response.statusMessage() + " (" + response.statusCode() + ")");
                            } else {
                                response.exceptionHandler(future::fail).bodyHandler(buffer -> {
                                    try {
                                        Document doc = sax.build(new InputSource(new StringReader(buffer.toString())));
                                        String price = doc.getRootElement().getChild("LastPrice").getText();
                                        String symbol = doc.getRootElement().getChild("Symbol").getText();
                                        StockResponse resp = new StockResponse();
                                        resp.setPrice(Float.parseFloat(price));
                                        resp.setSymbol(symbol);
                                        future.complete(Json.encode(resp));
                                    } catch (Exception ex) {
                                        future.fail(ex.getMessage());
                                    }
                                });
                            }
                        });
            }, v -> {
                return "Fallback: " + v.getMessage();
            });

            result.setHandler(ar -> {
                message.reply(ar.result());
            });

        });

    }
}