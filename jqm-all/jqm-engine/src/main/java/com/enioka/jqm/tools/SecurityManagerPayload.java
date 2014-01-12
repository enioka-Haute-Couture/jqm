package com.enioka.jqm.tools;

import java.security.Permission;

/**
 * A security manager ensuring a minimal good behavior for payloads running inside the JQM engine.
 * 
 * @author Marc-Antoine
 * 
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
