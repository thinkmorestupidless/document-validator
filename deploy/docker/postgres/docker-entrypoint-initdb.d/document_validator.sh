#!/bin/sh
set -e

psql -U postgres <<DDL
CREATE DATABASE document_validator;
REVOKE CONNECT ON DATABASE document_validator FROM PUBLIC;
CREATE USER document_validator WITH PASSWORD 'document_validator';
GRANT CONNECT ON DATABASE document_validator TO document_validator;
DDL

psql document_validator < /docker-entrypoint-initdb.d/document_validator.sql.dump
