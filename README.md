# Document Ingestor

## Quick Start

Getting the example application up and running for local development requires 2 steps:

1. A PostgreSQL database running on `localhost`
2. Running a local Akka cluster

### Running PostgreSQL locally

There's a `docker-compose` file located in `deploy/docker` which will start a PostgreSQL container and, when first run, will initialise the necessary schema required for Akka Persistence.

```shell script
cd deploy/docker
docker-compose up
```

The output of the above command should end with something that looks like:

```shell script
ba-postgres_1  | /usr/local/bin/docker-entrypoint.sh: sourcing /docker-entrypoint-initdb.d/document_validator.sh
ba-postgres_1  | CREATE DATABASE
ba-postgres_1  | REVOKE
ba-postgres_1  | CREATE ROLE
ba-postgres_1  | GRANT
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  |  set_config
ba-postgres_1  | ------------
ba-postgres_1  |
ba-postgres_1  | (1 row)
ba-postgres_1  |
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  | SET
ba-postgres_1  | CREATE TABLE
ba-postgres_1  | ALTER TABLE
ba-postgres_1  | CREATE SEQUENCE
ba-postgres_1  | ALTER TABLE
ba-postgres_1  | ALTER SEQUENCE
ba-postgres_1  | CREATE TABLE
ba-postgres_1  | ALTER TABLE
ba-postgres_1  | ALTER TABLE
ba-postgres_1  | ALTER TABLE
ba-postgres_1  | ALTER TABLE
ba-postgres_1  | CREATE INDEX
ba-postgres_1  | ALTER DEFAULT PRIVILEGES
ba-postgres_1  | ALTER DEFAULT PRIVILEGES
ba-postgres_1  | ALTER DEFAULT PRIVILEGES
ba-postgres_1  | ALTER DEFAULT PRIVILEGES
ba-postgres_1  |
ba-postgres_1  | /usr/local/bin/docker-entrypoint.sh: ignoring /docker-entrypoint-initdb.d/document_validator.sql.dump
ba-postgres_1  |
ba-postgres_1  | 2020-04-07 09:12:01.242 UTC [48] LOG:  received fast shutdown request
ba-postgres_1  | waiting for server to shut down...2020-04-07 09:12:01.251 UTC [48] LOG:  aborting any active transactions
ba-postgres_1  | .2020-04-07 09:12:01.262 UTC [48] LOG:  background worker "logical replication launcher" (PID 55) exited with exit code 1
ba-postgres_1  | 2020-04-07 09:12:01.280 UTC [50] LOG:  shutting down
ba-postgres_1  | 2020-04-07 09:12:01.450 UTC [48] LOG:  database system is shut down
ba-postgres_1  |  done
ba-postgres_1  | server stopped
ba-postgres_1  |
ba-postgres_1  | PostgreSQL init process complete; ready for start up.
ba-postgres_1  |
ba-postgres_1  | 2020-04-07 09:12:01.517 UTC [1] LOG:  listening on IPv4 address "0.0.0.0", port 5432
ba-postgres_1  | 2020-04-07 09:12:01.518 UTC [1] LOG:  listening on IPv6 address "::", port 5432
ba-postgres_1  | 2020-04-07 09:12:01.525 UTC [1] LOG:  listening on Unix socket "/var/run/postgresql/.s.PGSQL.5432"
ba-postgres_1  | 2020-04-07 09:12:01.652 UTC [75] LOG:  database system was shut down at 2020-04-07 09:12:01 UTC
ba-postgres_1  | 2020-04-07 09:12:01.720 UTC [1] LOG:  database system is ready to accept connections
ba-postgres_1  | 2020-04-07 09:12:21.283 UTC [82] FATAL:  unsupported frontend protocol 1234.5680: server supports 2.0 to 3.0
```

> If you don't see the SQL commands (CREATE, ALTER etc) then the database wasn't initialised correctly. Make sure the `deploy/docker/.postgres` file doesn't exist and run `docker-compose up --build`

### Running Akka Cluster locally

The default `application.conf` for the `validator` project will start 6 Akka nodes in the same JVM process and allow them to cluster together by defining explicit seed nodes.

```shell script
sbt "validator/run local"
```

This will run 2 back-end nodes, 2 front-end nodes and 2 nodes that each have 2 worker Actors running on them.

You should see output that looks like this:

```shell script
[2020-04-07 10:13:21,333] [INFO] [akka://ClusterSystem@127.0.0.1:5001] [worker.Worker$$anonfun$idle$1] [ClusterSystem-akka.actor.default-dispatcher-22] [akka://ClusterSystem/user/worker-1] - Work is complete. Result 2 * 2 = 4
```

Confirming that jobs are being sent by the front-end to the back-end and being picked up and executed by the worker Actors.

## Running on Kubernetes

TBD
