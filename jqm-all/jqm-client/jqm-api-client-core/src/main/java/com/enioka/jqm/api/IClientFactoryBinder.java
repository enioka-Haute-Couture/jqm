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

/**
 * A Binder is a class inside a client implementation that gives the name of a class implementing {@link IClientFactory}. Every client
 * implementation should have a class named <code>com.enioka.jqm.api.StaticClientBinder</code> implementing this interface for the static
 * binding system to work. This system was copied from slf4j.
 */
interface IClientFactoryBinder
{
    /**
     * Return the {@link IClientFactory} that the {@link JqmClientFactory} should bind to.
     * 
     * @return
     */
    IClientFactory getClientFactory();

    /**
     * @return the class name of the intended {@link IClientFactory} instance
     */
    String getClientFactoryName();
}
