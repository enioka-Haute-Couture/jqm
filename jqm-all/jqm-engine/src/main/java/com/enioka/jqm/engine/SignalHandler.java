/**
 * Copyright © 2013 enioka. All rights reserved
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
package com.enioka.jqm.engine;

/**
 * Handler of POSIX-like TERM/INT signals: gracefully shut down.
 */
class SignalHandler extends Thread
{
    private JqmEngine e = null;

    SignalHandler(JqmEngine e)
    {
        this.e = e;
    }

    @Override
    public void run()
    {
        e.stop();
    }

}
