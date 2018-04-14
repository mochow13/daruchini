// Catalano Core Library
// The Catalano Framework
//
// Copyright 2015 Haifeng Li
// haifeng.hli at gmail.com
//
// Based on Smile (Statistical Machine Intelligence & Learning Engine)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package catalano.core.Concurrent;


import java.util.concurrent.ThreadFactory;

class SimpleDeamonThreadFactory implements ThreadFactory {
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r);
    t.setDaemon(true);
    return t;
  }
}