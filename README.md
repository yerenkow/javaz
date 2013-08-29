javaz
=====

Set of mini utils, classes, routines and other useful things, all buildable as JAR

Sub Projects:
-------------

* cache - Simple and straightforward implementation of expirable Cache.

* jdbc - Handy tool to access JDBC/JNDI databases, for replicating data between different DBs

* queues - Helpers to make some batching/queues, both abstract and implementation part

Sub Projects cross-dependencies:
--------------------------------

* jdbc/queues - Queues interface implemented using JDBC tools; Threaded generic mass-updater;
