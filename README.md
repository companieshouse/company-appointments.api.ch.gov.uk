# company-appointments.api.ch.gov.uk
Java service for handling company level appointment data (company officer appointments)

## Makefile Changes
The jacoco exec file that SonarQube uses on GitHub is incomplete and, therefore, produces incorrect test coverage
reporting when code is pushed up to the repo. This is because the `analyse-pull-request` job runs when we push code to an open PR and this job runs `make test-unit`.
Therefore, the jacoco exec reporting only covers unit test coverage, not integration test coverage.

To remedy this, in the
short-term, we have decided to change the `make test-unit` command in the Makefile to run `mvn clean verify -Dskip.unit.tests=false -Dskip.integration.tests=false` instead as this
will ensure unit AND integration tests are run and that coverage is added to the jacoco reporting and, therefore, produce accurate SonarQube reporting on GitHub.

For a more in-depth explanation, please see: https://companieshouse.atlassian.net/wiki/spaces/TEAM4/pages/4357128294/DSND-1990+Tech+Debt+Spike+-+Fix+SonarQube+within+Pom+of+Projects

