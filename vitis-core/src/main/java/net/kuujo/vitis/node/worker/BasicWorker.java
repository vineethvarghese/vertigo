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
package net.kuujo.vitis.node.worker;

import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.messaging.DefaultJsonMessage;
import net.kuujo.vitis.messaging.JsonMessage;
import net.kuujo.vitis.node.NodeBase;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A basic worker implementation.
 *
 * @author Jordan Halterman
 */
public class BasicWorker extends NodeBase implements Worker {

  protected Handler<JsonMessage> messageHandler;

  public BasicWorker(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  protected void doReceive(JsonMessage message) {
    if (messageHandler != null) {
      messageHandler.handle(message);
    }
  }

  @Override
  public Worker messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    return this;
  }

  @Override
  public void emit(JsonObject data) {
    output.emit(DefaultJsonMessage.create(data));
  }

  @Override
  public void emit(JsonObject data, String tag) {
    output.emit(DefaultJsonMessage.create(data, tag));
  }

  @Override
  public void emit(JsonObject data, JsonMessage parent) {
    JsonMessage child = parent.createChild(data);
    output.emit(child);
  }

  @Override
  public void emit(JsonObject data, String tag, JsonMessage parent) {
    JsonMessage child = parent.createChild(data, tag);
    output.emit(child);
  }

  @Override
  public void ack(JsonMessage message) {
    output.ack(message);
  }

  @Override
  public void ack(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      ack(message);
    }
  }

  @Override
  public void fail(JsonMessage message) {
    output.fail(message);
  }

  @Override
  public void fail(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      fail(message);
    }
  }

}