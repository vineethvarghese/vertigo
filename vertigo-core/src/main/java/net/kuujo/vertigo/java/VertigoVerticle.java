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
package net.kuujo.vertigo.java;

import net.kuujo.vertigo.DefaultVertigo;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.context.WorkerContext;

import org.vertx.java.core.Future;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * A Vert.igo verticle implementation.
 *
 * The VertigoVerticle is an extension of the core Vert.x verticle that allows users
 * access to a Vert.igo instance for creating feeders, workers, and executors.
 *
 * @author Jordan Halterman
 */
public class VertigoVerticle extends Verticle {

  protected Vertigo vertigo;

  @Override
  public void setContainer(Container container) {
    super.setContainer(container);
  }

  @Override
  public void setVertx(Vertx vertx) {
    super.setVertx(vertx);
  }

  @Override
  public void start(Future<Void> startedResult) {
    vertigo = new DefaultVertigo(vertx, container);
    vertigo.setContext(new WorkerContext(container.config()));
    super.start(startedResult);
  }

}
