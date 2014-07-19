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
package com.enioka.jqm.api;

import java.util.Properties;

/**
 * This interface must be implemented by all client providers. It simply defines the "create client" interface.
 * 
 */
interface IClientFactory
{
    /**
     * Return the default client. Note this client is shared in the static context. (said otherwise: the same client is always returned
     * inside a same class loading context). The initialization cost is only paid at first call.
     * 
     * @return the default client
     */
    JqmClient getClient();

    /**
     * Return a named client. Note this client is shared in the static context. (said otherwise: for a given name, the same client is always
     * returned inside a same class loading context)<br>
     * This is helpful when multiple clients are needed, such as in some multithreading scenarios. Otherwise, just use the default client
     * return by {@link #getClient()}
     * 
     * @return the required client
     */
    JqmClient getClient(String name, Properties props, boolean cached);

    /**
     * Will remove the client of the given name from the static cache. Next time {@link #getClient(String)} is called, initialization cost
     * will have to be paid once again.<br>
     * Use <code>null</code> to reset the default client.<br>
     * This method is mostly useful for tests when databases are reset and therefore clients become invalid as they hold connections to
     * them.<br>
     * If the name does not exist, no exception is thrown.
     * 
     * @param name
     *            the client to reset, or <code>null</code> for the default client
     */
    void resetClient(String name);
}
