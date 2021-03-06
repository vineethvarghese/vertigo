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
package net.kuujo.vertigo;

import net.kuujo.vertigo.component.feeder.BasicFeeder;
import net.kuujo.vertigo.component.feeder.PollingFeeder;
import net.kuujo.vertigo.component.feeder.StreamFeeder;
import net.kuujo.vertigo.component.worker.Worker;
import net.kuujo.vertigo.context.WorkerContext;

/**
 * Primary Vert.igo API.
 *
 * This API mimics the core Vertx API. It allows users to create common Vertigo
 * feeders, workers, and executors from within a VertigoVerticle implementation.
 *
 * @author Jordan Halterman
 */
public interface Vertigo {

  /**
   * Sets the component instance context.
   *
   * @param context
   *   A worker context.
   */
  public void setContext(WorkerContext context);

  /**
   * Creates a basic feeder.
   *
   * @return
   *   A new basic feeder instance.
   */
  public BasicFeeder createBasicFeeder();

  /**
   * Creates a polling feeder.
   *
   * @return
   *   A new poll feeder instance.
   */
  public PollingFeeder createPollingFeeder();

  /**
   * Creates a stream feeder.
   *
   * @return
   *   A new stream feeder instance.
   */
  public StreamFeeder createStreamFeeder();

  /**
   * Creates a worker.
   *
   * @return
   *   A new worker instance.
   */
  public Worker createWorker();

}
