# System Architecture Overview

## System Components

### Application Architecture
- Spring Boot Backend (REST API)
- PostgreSQL database
- Angular Frontend

### Infrastructure Components
- **Jenkins CI/CD Server**: Automation and build orchestration
- **Kubernetes Cluster**: Test execution environment
- **Docker Build VM**: Container image builds
- **Developer Workstations**: Local development with Docker

## Data Flow

### Development Workflow
1. Developer pushes code to Git.
2. Jenkins webhook triggers a rescan of repository, looking for new branches/prs.
3. Pipelines execute in Kubernetes pods.
4. Results reported back to Github.

### Deployment Flow
1. Code merged to main/develop.
2. Docker build pipeline creates container image.
3. Image pushed to registry.
4. Kubernetes deployment updated.

## Technology Choices

### Why Jenkins?

- Our client primarily uses Jenkins for CI/CD.

### Why Kubernetes for CI?

- **Scalability**: Rather than potentially building up a large backlog of testing tasks that a single, large node has to work through, we can make use of the scalability of Kubernetes to provision resources on-demand.
- **Isolation**: Due to the flexibility of the Kubernetes environment, additional, out-of-band security controls can be used to mitigate and minimize any potential security issues related to remote code execution in testing environments.
- **Resource Efficiency**: Instead of running multiple, always-on virtual machines, we can make use of existing infrastructure and allow Jenkins to automatically provision new pods to run testing tasks.

### Why Separate Docker VM?

Docker builds require access to the docker socket or additional tooling to allow socket-less builds. Due to the complexity and security considerations of allowing a non-privileged kubernetes pod access to a docker socket, it was determined that it would be more time efficient to use a single pre-provisioned virtual machine to handle infrequent build tasks.

## Design Principles
- **Infrastructure as Code**: All configs related to Jenkins, (Pipelines, Pod Templates, Jenkins Deployments, etc) are version controlled, either in this repository or in a private infrastructure repository.
- **Security By Default**: Default deny all network policies, least privilege for runner rbac.
- **Developer Experience**: Fast feedback, easy local testing
- **Separation of Concerns**: Different pipelines for different purposes.
