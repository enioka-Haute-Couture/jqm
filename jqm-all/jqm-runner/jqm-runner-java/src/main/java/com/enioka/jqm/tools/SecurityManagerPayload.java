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
package com.enioka.jqm.tools;

import java.security.Permission;

/**
 * A security manager ensuring a minimal good behavior for payloads running inside the JQM engine.
 */
@SuppressWarnings("rawtypes")
public class SecurityManagerPayload extends SecurityManager
{
    /**
     * Default is: everything is allowed.
     */
    @Override
    public void checkPermission(Permission perm)
    {
        // Not throwing SecurityException = allowed.
    }

    @Override
    public void checkPermission(Permission perm, Object context)
    {
        // Default implementation for payloads, no checks for inner code.
        Class[] stack = getClassContext();
        if (stack.length > 3 && stack[3].getClassLoader() instanceof JarClassLoader)
        {
            super.checkPermission(perm, context);
        }
        else
        {
            return;
        }
    }

    /**
     * Ensures payloads are not allowed to call <code>System.exit()</code>
     */
    @Override
    public void checkExit(int status)
    {
        Class[] stack = getClassContext();
        if (stack.length > 3 && stack[3].getClassLoader() instanceof JarClassLoader)
        {
            throw new SecurityException("JQM payloads cannot call System.exit() - this would stop JQM itself!");
        }
    }
}
