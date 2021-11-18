/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

package com.enioka.jqm.engine.api.exceptions;

import com.enioka.jqm.shared.exceptions.JqmRuntimeException;

/**
 * The root exception for all exceptions internal to the engine.
 */
public class JqmEngineException extends JqmRuntimeException
{
    private static final long serialVersionUID = -5834325251715846234L;

    public JqmEngineException(String msg)
    {
        super(msg);
    }

    public JqmEngineException(String msg, Throwable e)
    {
        super(msg, e);
    }
}
