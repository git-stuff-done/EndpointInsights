# Security Hardening

## Threat Model

### Risks We Are Mitigating
- **Lateral movement:** Compromised build affecting other systems
- **Resource abuse:** Cryptomining, DOS

## Defense Layers

### 1. Network Segmentation

**Default Deny Policy:**
All egress traffic blocked unless explicitly allowed

### 2. Container Security
**Pod Security Standards:** Restricted profile enforced

**Security Context Settings:**
- `runAsNonRoot: true`
- `readOnlyRootFilesystem: true`
- `allowPrivilegeEscalation: false`
- All capabilities dropped

**Rationale:** Principle of least privilege

### 3. Image Security
TBD

### 4. Secrets Management

- No secrets in code or Jenkinsfiles
- Jenkins credentials plugin for sensitive values
- Environment-specific secrets in Kubernetes
