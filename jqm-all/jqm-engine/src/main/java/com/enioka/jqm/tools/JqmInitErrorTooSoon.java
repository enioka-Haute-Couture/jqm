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

/**
 * Thrown when an engine tries to start with a node name associated to a recent lastseenalive date.
 */
class JqmInitErrorTooSoon extends JqmInitError
{
    private static final long serialVersionUID = -5993404045975869943L;

    JqmInitErrorTooSoon(String msg)
    {
        super(msg);
    }
}
