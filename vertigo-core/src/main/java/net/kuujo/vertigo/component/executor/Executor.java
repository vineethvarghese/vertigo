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
package net.kuujo.vertigo.component.executor;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A network executor.
 *
 * @author Jordan Halterman
 */
public interface Executor<T extends Executor<T>> extends Component {

  /**
   * Starts the executor.
   *
   * @return
   *   The called executor instance.
   */
  public T start();

  /**
   * Starts the executor.
   *
   * @param doneHandler
   *   An asyncronous result handler to be invoked once the executor is started.
   * @return
   *   The called executor instance.
   */
  public T start(Handler<AsyncResult<T>> doneHandler);

  /**
   * Sets the execution reply timeout.
   *
   * @param timeout
   *   An execution reply timeout.
   * @return
   *   The called executor instance.
   */
  public T replyTimeout(long timeout);

  /**
   * Gets the execution reply timeout.
   *
   * @return
   *  An execution reply timeout.
   */
  public long replyTimeout();

  /**
   * Sets the maximum execution queue size.
   *
   * @param maxSize
   *   The maximum queue size allowed for the executor.
   * @return
   *   The called executor instance.
   */
  public T maxQueueSize(long maxSize);

  /**
   * Gets the maximum execution queue size.
   *
   * @return
   *   The maximum queue size allowed for the executor.
   */
  public long maxQueueSize();

  /**
   * Indicates whether the execution queue is full.
   *
   * @return
   *   A boolean indicating whether the execution queue is full.
   */
  public boolean queueFull();

  /**
   * Sets the executor auto-retry option.
   *
   * @param retry
   *   Indicates whether to automatically retry executing failed arguments.
   * @return
   *   The called executor instance.
   */
  public T autoRetry(boolean retry);

  /**
   * Gets the executor auto-retry option.
   *
   * @return
   *   Indicates whether the executor will automatically retry executing failed arguments.
   */
  public boolean autoRetry();

  /**
   * Sets the number of automatic retry attempts for a single failed execution.
   *
   * @param attempts
   *   The number of retry attempts allowed. If attempts is -1 then an infinite
   *   number of retry attempts will be allowed.
   * @return
   *   The called executor instance.
   */
  public T retryAttempts(int attempts);

  /**
   * Gets the number of automatic retry attempts.
   *
   * @return
   *   Indicates the number of retry attempts allowed for the executor.
   */
  public int retryAttempts();

  /**
   * Executes the network.
   *
   * @param args
   *   Execution arguments.
   * @param resultHandler
   *   An asynchronous result handler to be invoke with the execution result.
   * @return
   *   The called executor instance.
   */
  public T execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler);

  /**
   * Executes the network.
   *
   * @param args
   *   Execution arguments.
   * @param tag
   *   A tag to apply to the arguments.
   * @param resultHandler
   *   An asynchronous result handler to be invoke with the execution result.
   * @return
   *   The called executor instance.
   */
  public T execute(JsonObject args, String tag, Handler<AsyncResult<JsonMessage>> resultHandler);

}
