#!/bin/sh

kubectl create secret generic document-validator-secrets \
    --from-literal=postgres-username=document_validator \
    --from-literal=postgres-password=document_validator
