Conventions and style
###########################

Java
******

Formatting
++++++++++++++

Our code style is c++ inspired, since the maintainers just hate having opening braces on the same line. This is a fully assumed personal bias, so please do not argue with it.

To ease the pain for the users of what we may call the "default Eclipse formatting", an Eclipse formatter is provided :download:`here </files/rules_sonar.csv>`). If you are an Eclipse user, please import it and associate it with the JQM project.
(The Sonar rules we use are also included inside that directory.)

Database
***********

General:

* always use upper-case letters for object names (whatever the database, even if case insensitive).
* in case multiple words are needed in a name, they are separated by an underscore.
* spaces and special characters are forbidden. Only A-Z and _ and 0-9.
* Always prefer choices compatible with ANSI SQL.
* Shorter names are better!

Main tables:

* the name of the table is the name of the notion contained by the table. E.g. QUEUE for the table containing the description of the different queues.
* always use singular: QUEUE, and not QUEUES.
* never more than 25 characters. If needed, use a shorter version of the notion (JOB_DEF instead of JOB_DEFINITION for example).

Constraints:

* constraints must all be named. Never use auto generated names as they make the schema harder to update. (exception for NOT NULL)
* PK: PK_TABLE_NAME
* FK: FK_TABLE_NAME_n where n is an integer incremented on each new FK. We choose not to mention the target table in the name, as it would make the name really long and unwieldy.
* UK: UK_TABLE_NAME_n
* All FK must be indexed. Always, even on small tables, to avoid some locks (abusive generalization, but this allows to avoid some nasty bugs)
* As much as possible, constraints are expressed inside the database rather than in code.

ID generation:

* use sequences if available. There should be one unique sequence for all needs.
* otherwise do the best you can. Beware MySQL auto ID, which are reset on startup.

Table columns:

* Name follows same guides as for  table names.
* FK columns do not need to add _ID: an FK to a queue is named QUEUE.
* max length is 20 characters.
* for columns in the style of KEYWORD1, KEYWORD2, ... : no underscore between the final figure and the rest of the name.
* for strings, always use VARCHAR (or equivalent for your database) variable length data type. The length must be checked before insertion from the Java code.
* defaults should be expressed in Java code, not in the column definitions
* sometimes, the "good name" is a reserved SQL keyword: key, value... in that case just put another word next to it, without underscore (KEYNAME instead of KEY...)

Indices:

* For indices on constraints, IDX_CONSTRAINT_NAME
* Otherwise, IDX_TABLE_NAME_n
