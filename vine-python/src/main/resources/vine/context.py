# Copyright 2013 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
from definition import VineDefinition, SeedDefinition

class _AbstractContext(object):
  """
  An abstract context.
  """
  def __init__(self, context):
    self._context = context

class VineContext(_AbstractContext):
  """
  A vine context.
  """
  @property
  def address(self):
    return self._context.getAddress()

  @property
  def definition(self):
    return VineDefinition(self._context.getDefinition())

class SeedContext(_AbstractContext):
  """
  A seed context.
  """
  @property
  def address(self):
    return self._context.getAddress()

  @property
  def context(self):
    return VineContext(self._context.getContext())

  @property
  def definition(self):
    return SeedDefinition(self._context.getDefinition())

  @property
  def workers(self):
    return self._context.getWorkers()

class WorkerContext(_AbstractContext):
  """
  A worker context.
  """
  @property
  def address(self):
    return self._context.getAddress()

  @property
  def context(self):
    return SeedContext(self._context.getContext())

  @property
  def stem(self):
    return self._context.getStem()