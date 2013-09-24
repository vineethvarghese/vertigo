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
package com.blankstyle.vine.test.integration;

import net.kuujo.vine.java.SeedVerticle;
import net.kuujo.vine.messaging.JsonMessage;

import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertEquals;

public class TestSeedTwo extends SeedVerticle {

  @Override
  public void handle(JsonMessage message) {
    assertEquals("Hello world again!", message.body().getString("body"));
    emit(new JsonObject().putString("body", "Hello world again again!"));
    ack(message);
  }

}