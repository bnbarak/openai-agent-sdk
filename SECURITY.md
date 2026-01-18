# Security Policy

## Supported Versions

We release patches for security vulnerabilities. Currently supported versions:

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |

## Reporting a Vulnerability

We take the security of OpenAI Agent SDK seriously. If you believe you have found a security vulnerability, please report it to us as described below.

### Please DO NOT:

- Open a public GitHub issue for security vulnerabilities
- Discuss the vulnerability in public forums, social media, or mailing lists

### Please DO:

1. **Email us directly** at security@acolite.ai with:
   - A description of the vulnerability
   - Steps to reproduce the issue
   - Potential impact of the vulnerability
   - Any suggested fixes (if available)

2. **Allow us time to respond**:
   - We will acknowledge receipt within 48 hours
   - We will provide a more detailed response within 7 days
   - We will work with you to understand and validate the issue

3. **Coordinate disclosure**:
   - We will coordinate a timeline for public disclosure with you
   - We prefer to fix vulnerabilities before public disclosure
   - We will credit you in our security advisories (unless you prefer to remain anonymous)

## Security Best Practices

When using this SDK:

1. **API Keys**: Never commit API keys to version control
   - Use environment variables: `OPENAI_API_KEY`
   - Use secret management systems in production
   - Rotate keys regularly

2. **Input Validation**: Always validate and sanitize user inputs before passing to agents
   - Agent tools may execute code or access external systems
   - Implement input guardrails for sensitive operations

3. **Tool Permissions**: Carefully review tool permissions
   - Only grant tools the minimum permissions needed
   - Use approval workflows for destructive operations
   - Audit tool execution logs

4. **Network Security**: When using hosted tools or MCP servers
   - Use HTTPS for all external connections
   - Validate SSL certificates
   - Implement rate limiting

5. **Data Privacy**: Protect sensitive data
   - Be aware that data sent to OpenAI API is subject to their privacy policy
   - Don't include PII or sensitive information in prompts unless necessary
   - Implement data retention policies for session storage

## Security Updates

Security updates will be released as patch versions and announced via:
- GitHub Security Advisories
- Release notes
- Email notification to registered users (if applicable)

## Known Security Considerations

1. **Tool Execution**: Agent tools can execute arbitrary code. Review all custom tools carefully.
2. **Prompt Injection**: Agents may be vulnerable to prompt injection attacks. Implement input validation.
3. **API Rate Limits**: Implement rate limiting to prevent abuse.
4. **Session Storage**: Session data may contain sensitive information. Secure accordingly.

## Acknowledgments

We appreciate the security community's efforts in responsible disclosure. Contributors who report valid security issues will be acknowledged in our security advisories (with permission).

---

Last Updated: January 2026
