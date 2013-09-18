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
package com.blankstyle.vine.seed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.context.ConnectionContext;
import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.eventbus.Actions;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;
import com.blankstyle.vine.heartbeat.DefaultHeartBeatEmitter;
import com.blankstyle.vine.heartbeat.HeartBeatEmitter;
import com.blankstyle.vine.messaging.ConnectionPool;
import com.blankstyle.vine.messaging.Dispatcher;
import com.blankstyle.vine.messaging.EventBusMessageBus;
import com.blankstyle.vine.messaging.JsonMessage;
import com.blankstyle.vine.messaging.MessageBus;
import com.blankstyle.vine.messaging.MessageBusConnection;
import com.blankstyle.vine.messaging.MessageBusConnectionPool;
import com.blankstyle.vine.messaging.MessageBusStream;
import com.blankstyle.vine.messaging.Stream;

/**
 * A basic seed implementation.
 *
 * @author Jordan Halterman
 */
public class BasicSeed implements Seed {

  protected Vertx vertx;

  protected Container container;

  protected Logger logger;

  protected ReliableEventBus eventBus;

  protected MessageBus messageBus;

  protected String address;

  protected WorkerContext context;

  protected HeartBeatEmitter heartbeat;

  protected OutputCollector output;

  @Override
  public Seed setVertx(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = new WrappedReliableEventBus(vertx.eventBus(), vertx);
    this.messageBus = new EventBusMessageBus(eventBus);
    return this;
  }

  @Override
  public Seed setContainer(Container container) {
    this.container = container;
    this.logger = container.logger();
    return this;
  }

  @Override
  public Seed setContext(WorkerContext context) {
    this.context = context;
    this.address = context.getAddress();
    setupHeartbeat();
    setupOutputs();
    return this;
  }

  /**
   * Sets up the seed verticle heartbeat.
   */
  private void setupHeartbeat() {
    heartbeat = new DefaultHeartBeatEmitter(vertx, vertx.eventBus());
    eventBus.send(context.getStem(), Actions.create("register", context.getAddress()), 10000, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.succeeded()) {
          Message<JsonObject> message = result.result();
          JsonObject body = message.body();
          String error = body.getString("error");
          if (error != null) {
            logger.error(error);
          }
          else {
            // Set the heartbeat address. This is returned by the "register" action.
            String address = body.getString("address");
            if (address != null) {
              heartbeat.setAddress(address);
            }

            // Set the heartbeat interval. This setting is derived from the
            // seed definition's heartbeat interval option.
            heartbeat.setInterval(context.getContext().getDefinition().getHeartbeatInterval());
            heartbeat.start();
          }
        }
        else {
          logger.error(String.format("Failed to fetch heartbeat address from stem at %s.", context.getStem()));
        }
      }
    });
  }

  /**
   * Sets up seed outputs.
   */
  private void setupOutputs() {
    output = new OutputCollector();

    Collection<ConnectionContext> connections = context.getContext().getConnectionContexts();
    Iterator<ConnectionContext> iter = connections.iterator();
    while (iter.hasNext()) {
      ConnectionContext connectionContext = iter.next();
      try {
        JsonObject grouping = connectionContext.getGrouping();
        Dispatcher dispatcher = (Dispatcher) Class.forName(grouping.getString("dispatcher")).newInstance();

        // Set options on the dispatcher. All non-"dispatcher" values
        // are considered to be dispatcher options.
        Iterator<String> fieldNames = grouping.getFieldNames().iterator();
        while (fieldNames.hasNext()) {
          String fieldName = fieldNames.next();
          if (fieldName != "dispatcher") {
            String value = grouping.getString(fieldName);
            dispatcher.setOption(fieldName, value);
          }
        }

        // Create a connection pool from which the dispatcher will dispatch messages.
        ConnectionPool<MessageBusConnection> connectionPool = new MessageBusConnectionPool();
        String[] addresses = connectionContext.getAddresses();
        for (String address : addresses) {
          connectionPool.add(new MessageBusConnection(address, messageBus));
        }

        // Initialize the dispatcher and add a channel to the channels list.
        dispatcher.init(connectionPool);
        output.addStream(connectionContext.getSeedName(), new MessageBusStream(dispatcher));
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        container.logger().error("Failed to find grouping handler.");
      }
    }
  }

  @Override
  public Seed messageHandler(Handler<JsonMessage> handler) {
    messageBus.registerHandler(address, handler);
    return this;
  }

  @Override
  public void emit(JsonObject data) {
    emit(new JsonMessage(data));
  }

  @Override
  public void emit(JsonMessage message) {
    output.emit(message);
  }

  @Override
  public void emit(Collection<JsonMessage> messages) {
    for (JsonMessage message : messages) {
      emit(message);
    }
  }

  @Override
  public void emit(JsonObject data, JsonMessage parent) {
    emit(parent.createChild(data));
    parent.ack();
  }

  @Override
  public void emit(JsonMessage message, JsonMessage parent) {
    output.emit(message.setIdentifier(parent.getIdentifier()));
    parent.ack();
  }

  @Override
  public void emit(Collection<JsonMessage> messages, JsonMessage parent) {
    for (JsonMessage message : messages) {
      emit(message, parent);
    }
  }

  @Override
  public void emitTo(String seedName, JsonObject data) {
    emitTo(seedName, new JsonMessage(data));
  }

  @Override
  public void emitTo(String seedName, JsonMessage message) {
    output.emitTo(seedName, message);
  }

  @Override
  public void emitTo(String seedName, Collection<JsonMessage> messages) {
    for (JsonMessage message : messages) {
      emitTo(seedName, message);
    }
  }

  @Override
  public void emitTo(String seedName, JsonObject data, JsonMessage parent) {
    emitTo(seedName, parent.createChild(data));
    parent.ack();
  }

  @Override
  public void emitTo(String seedName, JsonMessage message, JsonMessage parent) {
    emitTo(seedName, message.setIdentifier(parent.getIdentifier()));
    parent.ack();
  }

  @Override
  public void emitTo(String seedName, Collection<JsonMessage> messages, JsonMessage parent) {
    for (JsonMessage message : messages) {
      emitTo(seedName, message, parent);
    }
  }

  @Override
  public void ack(JsonMessage message) {
    message.ack();
  }

  @Override
  public void ack(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      message.ack();
    }
  }

  @Override
  public void fail(JsonMessage message) {
    message.fail();
  }

  @Override
  public void fail(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      message.fail();
    }
  }

  /**
   * Publishes seed output to the appropriate streams.
   */
  protected static class OutputCollector {

    private List<Stream<?>> streamList = new ArrayList<>();
    private Map<String, Stream<?>> streams = new HashMap<>();

    /**
     * Adds a stream to the output.
     */
    public OutputCollector addStream(String name, Stream<?> stream) {
      if (streams.containsKey(name)) {
        Stream<?> oldStream = streams.get(name);
        if (streamList.contains(oldStream)) {
          streamList.remove(oldStream);
        }
      }
      streams.put(name, stream);
      streamList.add(stream);
      return this;
    }

    /**
     * Removes a stream from the output.
     */
    public OutputCollector removeStream(String name) {
      if (streams.containsKey(name)) {
        streams.remove(name);
      }
      return this;
    }

    /**
     * Returns a set of stream names.
     */
    public Set<String> getStreamNames() {
      return streams.keySet();
    }

    /**
     * Returns a stream by name.
     */
    public Stream<?> getStream(String name) {
      return streams.get(name);
    }

    /**
     * Returns the number of streams in the output.
     */
    public int size() {
      return streams.size();
    }

    /**
     * Emits a message to all streams.
     */
    public void emit(JsonMessage message) {
      for (Stream<?> stream : streamList) {
        stream.emit(message);
      }
    }

    /**
     * Emits a message to all streams.
     */
    public void emit(JsonMessage message, Handler<Boolean> ackHandler) {
      for (Stream<?> stream : streamList) {
        stream.emit(message, ackHandler);
      }
    }

    /**
     * Emits a message to a specific stream.
     */
    public void emitTo(String name, JsonMessage message) {
      if (streams.containsKey(name)) {
        streams.get(name).emit(message);
      }
    }

    /**
     * Emits a message to a specific stream.
     */
    public void emitTo(String name, JsonMessage message, Handler<Boolean> ackHandler) {
      if (streams.containsKey(name)) {
        streams.get(name).emit(message, ackHandler);
      }
    }
  }

}
