# CI/CD Pipeline Architecture

## Pipeline Strategy

We use three different pipelines for different purposes:

### 1. PR Validation Pipeline (`Jenkinsfile`)
**Triggers:** Every Push to any branch, all PRs
**Purpose:** Fast feedback for developers
**Runs:**
- Unit Testing
- Linting [TBD]

**Execution Environment:** Kubernetes pods (lightweight, fast, provisioned on-demand)
**Typical Duration:** TBD

### 2. **[WIP]** Docker Build Pipeline (`Jenkinsfile.docker`)
**Triggers:** Merges to main/develop branches
**Purpose:** Create deployable container images
**Runs:**
- Docker image build
- Image Vulnerability Scanning (TBD)
- Push to container registry (ghcr.io)
- Tag with git commit SHA

**Execution Environment:** Dedicated Docker VM
**Typical Duration:** TBD

## Future Enhancements
- Build caching optimization
- Deployment pipelines (staging/production)

