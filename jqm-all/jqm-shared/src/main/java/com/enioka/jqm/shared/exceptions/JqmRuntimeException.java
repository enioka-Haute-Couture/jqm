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
package com.enioka.jqm.shared.exceptions;

/**
 * The root of all exceptions ever thrown in jqm code.
 */
public class JqmRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 8187758834667680389L;

    public JqmRuntimeException(String msg)
    {
        super(msg);
    }

    public JqmRuntimeException(String msg, Throwable e)
    {
        super(msg, e);
    }
}
