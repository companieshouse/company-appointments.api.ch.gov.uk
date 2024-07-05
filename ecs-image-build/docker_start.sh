#!/bin/bash

# Start script for company-appointments.api.ch.gov.uk.jar

PORT=8080

exec java -jar -Dserver.port="${PORT}" "company-appointments.api.ch.gov.uk.jar"
