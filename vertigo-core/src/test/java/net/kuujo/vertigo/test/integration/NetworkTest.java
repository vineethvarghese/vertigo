/*
* Copyright 2013 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.kuujo.vertigo.test.integration;

import net.kuujo.vertigo.Cluster;
import net.kuujo.vertigo.Groupings;
import net.kuujo.vertigo.LocalCluster;
import net.kuujo.vertigo.Networks;
import net.kuujo.vertigo.component.feeder.BasicFeeder;
import net.kuujo.vertigo.component.worker.Worker;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.definition.NetworkDefinition;
import net.kuujo.vertigo.java.VertigoVerticle;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.testtools.TestVerticle;

/**
 * A network deploy test.
 *
 * @author Jordan Halterman
 */
public class NetworkTest extends TestVerticle {

  public static class TestBasicFeeder extends VertigoVerticle {
    @Override
    public void start() {
      final BasicFeeder feeder = vertigo.createBasicFeeder();
      feeder.start(new Handler<AsyncResult<BasicFeeder>>() {
        @Override
        public void handle(AsyncResult<BasicFeeder> result) {
          feeder.feed(new JsonObject().putString("body", "Hello world"), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                assertTrue(result.cause().getMessage(), result.succeeded());
              }
              else {
                assertTrue(result.succeeded());
              }
              testComplete();
            }
          });
        }
      });
    }
  }

  public static class TestComponentOne extends VertigoVerticle {
    @Override
    public void start() {
      final Worker worker = vertigo.createWorker();
      worker.messageHandler(new Handler<JsonMessage>() {
        @Override
        public void handle(JsonMessage message) {
          JsonObject body = message.body();
          String hello = body.getString("body");
          worker.emit(new JsonObject().putString("body", hello + "!"), message);
          worker.ack(message);
        }
      }).start();
    }
  }

  public static class TestComponentTwo extends VertigoVerticle {
    @Override
    public void start() {
      final Worker worker = vertigo.createWorker();
      worker.messageHandler(new Handler<JsonMessage>() {
        @Override
        public void handle(JsonMessage message) {
          JsonObject body = message.body();
          String hello = body.getString("body");
          if (hello.equals("Hello world!")) {
            worker.ack(message);
          }
          else {
            worker.fail(message);
          }
        }
      }).start();
    }
  }

  private NetworkDefinition createSimpleTestDefinition() {
    NetworkDefinition network = Networks.createDefinition("test");
    network.fromVerticle("feeder", TestBasicFeeder.class.getName())
      .toVerticle("componentone", TestComponentOne.class.getName()).groupBy(Groupings.random())
      .toVerticle("componenttwo", TestComponentTwo.class.getName()).groupBy(Groupings.round());
    return network;
  }

  @Test
  public void testLocalSimpleNetwork() {
    Cluster cluster = new LocalCluster(vertx, container);
    NetworkDefinition network = createSimpleTestDefinition();
    cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          assertTrue(result.cause().getMessage(), result.succeeded());
        }
        else {
          assertTrue(result.succeeded());
        }
      }
    });
  }

}
