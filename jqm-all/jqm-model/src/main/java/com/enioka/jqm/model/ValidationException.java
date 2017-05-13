package com.enioka.jqm.model;

public class ValidationException extends RuntimeException
{
    private static final long serialVersionUID = 7648595935930278187L;

    public ValidationException(String field, String reason)
    {
        super("Field " + field + " is not valid. Reason is: " + reason);
    }

}
