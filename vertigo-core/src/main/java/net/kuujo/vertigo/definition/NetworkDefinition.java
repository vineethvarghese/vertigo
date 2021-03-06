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
package net.kuujo.vertigo.definition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.grouping.RoundGrouping;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;


/**
 * A default network definition implementation.
 *
 * @author Jordan Halterman
 */
public class NetworkDefinition implements Definition {

  private JsonObject definition = new JsonObject();

  private static final long DEFAULT_ACK_EXPIRE = 30000;

  public NetworkDefinition() {
  }

  public NetworkDefinition(JsonObject json) {
    definition = json;
  }

  /**
   * Gets the network address.
   */
  public String address() {
    return definition.getString("address");
  }

  /**
   * Sets the network address.
   *
   * @param address
   *   The network address.
   * @return
   *   The called network definition.
   */
  public NetworkDefinition setAddress(String address) {
    definition.putString("address", address);
    return this;
  }

  /**
   * Indicates whether acking is enabled for the network.
   * Acking is enabled by default.
   *
   * @return
   *   A boolean value indicating whether acking is enabled.
   */
  public boolean ackingEnabled() {
    return definition.getBoolean("ack", true);
  }

  /**
   * Enables acking for the network.
   * Acking is enabled by default.
   *
   * @return
   *   The called network definition.
   */
  public NetworkDefinition enableAcking() {
    definition.putBoolean("ack", true);
    return this;
  }

  /**
   * Disables acking for the network.
   *
   * @return
   *   The called network definition.
   */
  public NetworkDefinition disableAcking() {
    definition.putBoolean("ack", false);
    return this;
  }

  /**
   * Indicates the number of acker (auditor) verticle instances.
   *
   * @return
   *   The number of acker verticle instances for the network.
   */
  public int numAckers() {
    return definition.getInteger("ackers", 1);
  }

  /**
   * Sets the number of acker (auditor) verticle instances.
   *
   * @param numAckers
   *   The number of acker verticle instances for the network.
   * @return
   *   The called network definition.
   */
  public NetworkDefinition setNumAckers(int numAckers) {
    definition.putNumber("ackers", numAckers);
    return this;
  }

  /**
   * Indicates the internal auditor ack expiration.
   *
   * @return
   *   The internal network auditor ack expiration.
   */
  public long ackExpire() {
    return definition.getLong("expire", DEFAULT_ACK_EXPIRE);
  }

  /**
   * Sets the internal auditor ack expiration. This is the amount of time after
   * which the acker verticle will consider all messages to be timed out. Therefore,
   * this expiration should be greater than any feeder timeout in the network.
   *
   * @param expire
   *   The ack expiration.
   * @return
   *   The called network definition.
   */
  public NetworkDefinition setAckExpire(long expire) {
    definition.putNumber("expire", expire);
    return this;
  }

  /**
   * Gets a network option.
   *
   * @param option
   *   The option to get.
   * @return
   *   The option value.
   */
  public String option(String option) {
    return definition.getString(option);
  }

  /**
   * Sets a network option.
   *
   * @param option
   *   The option to set.
   * @param value
   *   The option value.
   * @return
   *   The called network definition.
   */
  public NetworkDefinition setOption(String option, String value) {
    switch (option) {
      case "address":
        return setAddress(value);
      default:
        definition.putString(option, value);
        break;
    }
    return this;
  }

  /**
   * Adds a root definition.
   */
  private ComponentDefinition addDefinition(ComponentDefinition definition) {
    // Add the root definition.
    JsonObject roots = this.definition.getObject("roots");
    if (roots == null) {
      roots = new JsonObject();
      this.definition.putObject("roots", roots);
    }
    if (!roots.getFieldNames().contains(definition.name())) {
      roots.putObject(definition.name(), definition.serialize());
    }
    return definition;
  }

  public ComponentDefinition from(ComponentDefinition definition) {
    return addDefinition(definition);
  }

  /**
   * Adds an initial verticle component.
   *
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromVerticle() {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.VERTICLE));
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromVerticle(String name) {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.VERTICLE).setName(name));
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromVerticle(String name, String main) {
    return fromVerticle(name, main, new JsonObject(), 1);
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromVerticle(String name, String main, JsonObject config) {
    return fromVerticle(name, main, config, 1);
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromVerticle(String name, String main, int workers) {
    return fromVerticle(name, main, new JsonObject(), workers);
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromVerticle(String name, String main, JsonObject config, int workers) {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.VERTICLE).setName(name).setMain(main).setConfig(config).setWorkers(workers));
  }

  /**
   * Adds an initial module component.
   *
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromModule() {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.MODULE));
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromModule(String name) {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.MODULE).setName(name));
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromModule(String name, String moduleName) {
    return fromModule(name, moduleName, new JsonObject(), 1);
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromModule(String name, String moduleName, JsonObject config) {
    return fromModule(name, moduleName, config, 1);
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromModule(String name, String moduleName, int workers) {
    return fromModule(name, moduleName, new JsonObject(), workers);
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition fromModule(String name, String moduleName, JsonObject config, int workers) {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.MODULE).setName(name).setModule(moduleName).setConfig(config).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

  /**
   * Creates a component address.
   */
  protected String createComponentAddress(String networkAddress, String componentName) {
    return String.format("%s.%s", networkAddress, componentName);
  }

  /**
   * Creates an array of worker addresses.
   */
  protected String[] createWorkerAddresses(String componentAddress, int numWorkers) {
    List<String> addresses = new ArrayList<String>();
    for (int i = 0; i < numWorkers; i++) {
      addresses.add(String.format("%s.%d", componentAddress, i+1));
    }
    return addresses.toArray(new String[addresses.size()]);
  }

  /**
   * Returns a network context representation of the network.
   *
   * @return
   *   A prepared network context.
   * @throws MalformedDefinitionException 
   */
  public NetworkContext createContext() throws MalformedDefinitionException {
    String address = definition.getString("address");
    if (address == null) {
      throw new MalformedDefinitionException("No address specified.");
    }

    JsonObject context = new JsonObject();
    context.putString("address", address);
    context.putString("audit", String.format("%s.audit", address));
    context.putString("broadcast", String.format("%s.broadcast", address));
    context.putObject("definition", definition);

    // First, create all component contexts and then add connections.
    JsonObject roots = definition.getObject("roots");
    Iterator<String> iter = roots.getFieldNames().iterator();

    // Create component contexts.
    JsonObject componentContexts = new JsonObject();
    while (iter.hasNext()) {
      JsonObject root = roots.getObject(iter.next());
      JsonObject componentDefinitions = buildComponentsRecursive(root);
      Iterator<String> iterComponents = componentDefinitions.getFieldNames().iterator();
      while (iterComponents.hasNext()) {
        JsonObject componentDef = componentDefinitions.getObject(iterComponents.next());
        JsonObject componentContext = new JsonObject();
        String componentName = componentDef.getString("name");
        if (componentName == null) {
          throw new MalformedDefinitionException("No component name specified.");
        }
        componentContext.putString("name", componentName);
        componentContext.putString("address", createComponentAddress(definition.getString("address"), componentDef.getString("name")));
        componentContext.putObject("definition", componentDef);
        componentContext.putArray("workers", new JsonArray(createWorkerAddresses(componentContext.getString("address"), componentContext.getObject("definition").getInteger("workers"))));
        componentContexts.putObject(componentContext.getString("name"), componentContext);
      }
    }

    // Component contexts are stored in context.workers.
    context.putObject("components", componentContexts);
    

    JsonArray connections = definition.getArray("connections");
    if (connections == null) {
      connections = new JsonArray();
    }

    JsonObject connectionContexts = new JsonObject();

    // Create an object of connection information.
    Iterator<Object> iter2 = connections.iterator();
    while (iter2.hasNext()) {
      String name = iter2.next().toString();
      JsonObject componentContext = componentContexts.getObject(name);
      if (componentContext == null) {
        continue;
      }

      JsonObject connection = new JsonObject();
      connection.putString("name", name);

      // Add a grouping definition to the connection context.
      JsonObject grouping = componentContext.getObject("definition").getObject("grouping");
      if (grouping == null) {
        grouping = new JsonObject().putString("grouping", RoundGrouping.class.getName()).putObject("definition", new JsonObject());
      }
      connection.putObject("grouping", grouping);

      // Add filter definitions to the connection context.
      JsonArray filters = componentContext.getObject("definition").getArray("filters");
      if (filters == null) {
        filters = new JsonArray();
      }
      connection.putArray("filters", filters);

      connection.putArray("addresses", componentContext.getArray("workers").copy());

      connectionContexts.putObject(name, connection);
    }

    // Connection information is stored in context.connections.
    context.putObject("connections", connectionContexts);

    // Now iterate through each component context and add connection information.
    // This needed to be done *after* those contexts are created because
    // we need to be able to get context information from connecting components.
    Iterator<String> componentNames = componentContexts.getFieldNames().iterator();
    while (componentNames.hasNext()) {
      JsonObject componentContext = componentContexts.getObject(componentNames.next());
      JsonObject componentDef = componentContext.getObject("definition");

      // Iterate through each of the component's connections.
      JsonObject componentCons = componentDef.getObject("connections");
      JsonObject componentConnectionContexts = new JsonObject();

      if (componentCons != null) {
        Set<String> conKeys = componentCons.getFieldNames();
        Iterator<String> iterCon = conKeys.iterator();
  
        while (iterCon.hasNext()) {
          // Get the component name and with it a reference to the component context.
          String name = iterCon.next().toString();
          JsonObject conContext = componentContexts.getObject(name);
          if (conContext == null) {
            continue;
          }
  
          // With the context, we can list all of the worker addresses.
          JsonObject connection = new JsonObject();
          connection.putString("name", name);

          // If the connection doesn't define a grouping, use a round grouping.
          JsonObject grouping = conContext.getObject("grouping");
          if (grouping == null) {
            grouping = new JsonObject().putString("grouping", RoundGrouping.class.getName()).putObject("definition", new JsonObject());
          }
          connection.putObject("grouping", grouping);

          // Add filter definitions to the connection.
          JsonArray filters = conContext.getArray("filters");
          if (filters == null) {
            filters = new JsonArray();
          }
          connection.putArray("filters", filters);

          connection.putArray("addresses", conContext.getArray("workers").copy());
  
          componentConnectionContexts.putObject(name, connection);
        }
      }

      // Finally, add the connections to the object.
      componentContext.putObject("connections", componentConnectionContexts);
    }

    return new NetworkContext(context);
  }

  private JsonObject buildComponentsRecursive(JsonObject componentDefinition) {
    return buildComponentsRecursive(componentDefinition, new JsonObject());
  }

  private JsonObject buildComponentsRecursive(JsonObject componentDefinition, JsonObject components) {
    components.putObject(componentDefinition.getString("name"), componentDefinition);
    JsonObject connections = componentDefinition.getObject("connections");
    if (connections != null) {
      Iterator<String> iter = connections.getFieldNames().iterator();
      while (iter.hasNext()) {
        buildComponentsRecursive(connections.getObject(iter.next()), components);
      }
    }
    return components;
  }

}
