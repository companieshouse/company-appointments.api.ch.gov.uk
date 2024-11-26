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

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        | public-data                                    | ECS cluster (stack) the service belongs to
**Load balancer**      | {env}-chs-apichgovuk <br> {env}-chs-apichgovuk-private                                 | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/company-appointments.api.ch.gov.uk) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/company-appointments.api.ch.gov.uk)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Building the docker image with local changes, requires access to AWS ECR

```bash
  mvn compile jib:dockerBuild
```

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)