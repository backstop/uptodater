uptodater
=========

git clone of https://svn.code.sf.net/p/uptodater/code/trunk/uptodater/

The code lives on [https://github.com/backstop/uptodater Github].


Writing Uptodater Scripts
=========================

Uptodater scripts are just files that contain SQL statements that will be executed against the database upon JBoss startup.

Besides the SQL statements in the files, you can have several annotations to tell Uptodater to do something special.

Annotations
===========

--uptodater.optional=true
-------------------------

* This is the FILE optional annotation.  This tells uptodater that if there is an error when processing the file, to just ignore the error and '''any statements in the file after the erroneous statement'''.  This can be problematic, because it can lead to statements not being processed when people think that they will.
* This annotation should only be used once in an Uptodater script, usually somewhere near the top.  In most cases you probably want to use '''--statement.optional''' instead of this.

--statement.optional
--------------------

* This is the STATEMENT optional annotation.  This tells Uptodater to ignore any errors when processing a statement, but continue to the next statement in the file.  The Uptodater script itself can still fail if a statement that doesn't have the --optional annotation fails.
* People should use this annotation instead of the FILE optional annotation.  You can use it on a per-statement basis.

--uptodater.statement.separator=^/
----------------------------------

* This annotation changes the statement separator to a newline then forward slash instead of the default semicolon.  Notice that the separator is a regex.
