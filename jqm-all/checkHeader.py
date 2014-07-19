#!/usr/bin/env python2
# coding:utf-8
import os
import re
import shutil

JQM_ROOT_DIR = os.path.abspath(os.path.dirname(__file__))
tmpFilePath = os.path.join(JQM_ROOT_DIR, "__tmp_file.java")

HEADER = """/**
 * Copyright © 2013 enioka. All rights reserved
%s *
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
"""

AUTHOR = re.compile(".* \(\S+@\S+\.\S+\)")

if __name__ == "__main__":
    for dirpath, dirnames, filenames in os.walk(JQM_ROOT_DIR):
        for filename in filenames:
            if filename.endswith(".java"):
                authors = []
                path = os.path.join(dirpath, filename)
                tmp = open(tmpFilePath, "w")
                inHeader = True
                for line in open(path, "r"):
                    if inHeader:
                        if line.startswith("/*") or line.startswith(" *"):
                            # print "reading header: %s " % line.strip()
                            if AUTHOR.match(line):
                                authors.append(line)
                                # print line
                        else:
                            # print "End of header %s" % line.strip()
                            inHeader = False
                            tmp.write(HEADER % "".join(authors))
                            tmp.write(line)
                    else:
                        tmp.write(line)
                tmp.close()
                shutil.copy(tmpFilePath, path)
                os.unlink(tmpFilePath)


