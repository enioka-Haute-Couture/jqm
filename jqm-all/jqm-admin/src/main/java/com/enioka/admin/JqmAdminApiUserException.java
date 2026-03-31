package com.enioka.admin;

import java.util.Map;

/**
 * Exception thrown when a user error occurs while using the JQM Admin API.
 */
public class JqmAdminApiUserException extends JqmAdminApiException
{
    private static final long serialVersionUID = 8470196707989067977L;

    private String userMessageKey;
    private Map<String, Object> userMessageParams;

    /**
     * Create a new exception with a specific message.
     *
     * @param e
     *            the error message
     */
    public JqmAdminApiUserException(String e)
    {
        super(e);
    }

    /**
     * Create a new exception with a message key for i18n and a fallback message.
     *
     * @param userMessageKey
     *            the i18n key for frontend translation
     * @param fallbackMessage
     *            the fallback error message (used if translation is missing)
     * @param userMessageParams
     *            optional parameters for the translated message
     */
    public JqmAdminApiUserException(String userMessageKey, String fallbackMessage, Map<String, Object> userMessageParams)
    {
        super(fallbackMessage);
        this.userMessageKey = userMessageKey;
        this.userMessageParams = userMessageParams;
    }

    /**
     * Create a new exception wrapping an existing one.
     *
     * @param e
     *            the root cause
     */
    public JqmAdminApiUserException(Exception e)
    {
        super(e);
    }

    /**
     * Create a new exception with a custom message and a root cause.
     *
     * @param m
     *            the error message
     * @param e
     *            the root cause
     */
    public JqmAdminApiUserException(String m, Exception e)
    {
        super(m, e);
    }

    public String getUserMessageKey()
    {
        return userMessageKey;
    }

    public Map<String, Object> getUserMessageParams()
    {
        return userMessageParams;
    }
}
