# Java Clean Code Best Practices

This document defines how we write Java.
The goal is code that reads like plain English, is easy to test, easy to reason about, and hard to misuse.

These rules are inspired by Clean Code principles and adapted for modern Java, Lombok, and production systems.

---

## 1. Prefer positive conditions first

Conditions should read naturally and avoid mental negation.

Bad

```java
if (isEnabled == false) {
  return;
}
```

Good

```java
if (!isEnabled) {
  return;
}
```

Better when the happy path is primary

```java
if (isEnabled) {
  start();
}
```

Avoid `== true` and `== false`.
Booleans already express intent.

### Null handling

If null is invalid, fail fast.

```java
Objects.requireNonNull(user, "user must not be null");
```

If absence is expected, model it explicitly.

```java
Optional<User> user = repository.findById(id);
user.ifPresent(this::sendEmail);
```

Avoid inverted null checks.

```java
if (user != null) { ... }
```

---

## 2. Return early

Handle invalid or edge cases immediately.
Keep the main logic flat and visible.

Bad

```java
if (order != null) {
  if (!order.isEmpty()) {
    if (order.hasPayment()) {
      charge(order);
    }
  }
}
```

Good

```java
Objects.requireNonNull(order, "order must not be null");
if (order.isEmpty()) {
  return;
}
if (!order.hasPayment()) {
  return;
}
charge(order);
```

The happy path should not be nested.

---

## 3. Avoid nested conditionals. Write flat code

Deep nesting hides intent and increases cognitive load.

Bad

```java
if (user.isActive()) {
  if (user.hasEmail()) {
    if (preferences.isMarketingEnabled()) {
      sendEmail(user);
    }
  }
}
```

Good

```java
if (!shouldSendMarketingEmail(user, preferences)) {
  return;
}
sendEmail(user);
```

```java
private boolean shouldSendMarketingEmail(User user, Preferences preferences) {
  return user.isActive()
      && user.hasEmail()
      && preferences.isMarketingEnabled();
}
```

---

## 4. Prefer immutability and Lombok builders

Immutable objects are easier to reason about and safer to share.

Use Lombok by default for data objects.

```java
@Value
@Builder
public class QuoteRequest {
  String brokerId;
  String clientName;
  int employeeCount;
}
```

This gives

* Final fields
* No setters
* Correct equality
* A fluent builder
* Minimal noise

### Validation belongs in construction

```java
@Value
@Builder
public class QuoteRequest {
  String brokerId;
  String clientName;
  int employeeCount;

  public QuoteRequest {
    Objects.requireNonNull(brokerId, "brokerId is required");
    Objects.requireNonNull(clientName, "clientName is required");
    if (employeeCount <= 0) {
      throw new IllegalArgumentException("employeeCount must be positive");
    }
  }
}
```

### Defaults

```java
@Value
@Builder
public class RetryPolicy {
  @Builder.Default
  int maxAttempts = 3;
}
```

### When not to use builders

Do not use builders for

* Objects that require IO
* Objects that depend on services
* Multi step domain creation logic

Use factories or domain services instead.

---

## 5. Keep classes tight

A class should have one clear reason to change.

If you describe a class using the word and, it is doing too much.

Bad

* Validates input and
* Calls external APIs and
* Formats output and
* Writes to the database

Good

* Validator
* Client
* Repository
* Renderer

Smaller classes are easier to test and safer to modify.

---

## 6. Use the tightest visibility possible

Default to private.
Expose only what is required.

Bad

```java
public class QuoteService {
  public Repository repository;
}
```

Good

```java
public class QuoteService {
  private final Repository repository;

  public QuoteService(Repository repository) {
    this.repository = repository;
  }
}
```

Prefer final fields whenever possible.

---

## 7. Use static utilities for pure logic

Static utilities are appropriate for stateless, pure functions.

Good

```java
public final class Strings {
  private Strings() {}

  public static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
```

Avoid static methods that

* Perform IO
* Access time
* Read configuration
* Hide dependencies

Those belong in injected services.

---

## 8. If something is hard to test, break it down

Hard to test code usually means too many responsibilities or hidden dependencies.

Bad

```java
public Result run(String orgId, String payload) {
  String token = secrets.read(orgId);
  HttpResponse response = http.post(url, token, payload);
  return mapper.map(response);
}
```

Good

```java
public Result run(String orgId, Payload payload) {
  String token = tokenProvider.get(orgId);
  Response response = client.send(token, payload);
  return mapper.map(response);
}
```

Now each dependency can be tested or mocked independently.

---

## 9. Small methods. One concept per method

Methods should do one thing at one level of abstraction.

Bad

```java
public void onboard(Client client) {
  validate(client);
  save(client);
  email(client);
  audit(client);
}
```

Better

```java
public void onboard(Client client) {
  validateClient(client);
  persistClient(client);
  notifyClient(client);
  recordAudit(client);
}
```

If a method needs explanation, it is too large.

---

## 10. Names describe what it does, not how

Names express intent, not implementation.

Bad

* processData
* handle
* doStuff
* updateDb

Good

* calculatePremium
* createQuote
* isEligibleForRenewal
* persistQuote

Boolean names should read naturally in conditions.

```java
if (user.isActive()) { ... }
if (policy.canBind()) { ... }
```

---

## 11. Comments are rare and written in plain English

Any comment should be a valid english sentenec or sentences with a period at the end.

Code should explain what it does through structure and naming.

If a comment explains what the code is doing, the code is not clear enough.

### Bad

```java
// check if user is active
if (user.isActive()) {
  ...
}
```

Use comments as a separator.
```
    // ========== Global Singleton ==========
```

Comments are allowed only to explain why something exists or why a decision was made.

All comments must

* Be written in plain English
* Be a complete sentence
* End with a period

### Good

```java
// The carrier rejects payloads larger than one megabyte.
byte[] payload = truncateAttachments(request);
```

If you feel the need to comment a block, extract it and name it instead.

---

## Final rule

Code should read like English.
If you have to explain it, simplify it.

---