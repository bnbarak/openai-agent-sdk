# Project Lombok

Project Lombok is a Java library that automatically plugs into your editor and build tools to eliminate boilerplate code through annotations. It uses annotation processing to generate common code patterns like getters, setters, constructors, builders, and more at compile-time. Lombok transforms annotated source code during compilation, modifying the Abstract Syntax Tree before final bytecode generation, ensuring zero runtime overhead while significantly reducing code verbosity.

The library integrates seamlessly with popular IDEs and build tools, providing a cleaner, more maintainable codebase. By automating repetitive coding tasks, Lombok allows developers to focus on business logic rather than mechanical code generation. It supports a wide range of Java versions and works with standard compilation processes, making it easy to adopt in existing projects without major architectural changes.

## @Getter and @Setter Annotations

Generate getter and setter methods for class fields automatically. Can be applied to individual fields or entire classes with customizable access levels.

```java
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

public class User {
    @Getter @Setter private String username;
    @Getter @Setter private String email;
    @Getter private final Long id;
    @Setter(AccessLevel.PROTECTED) private String password;

    public User(Long id) {
        this.id = id;
    }
}

// Usage
User user = new User(1L);
user.setUsername("john_doe");
user.setEmail("john@example.com");
System.out.println(user.getUsername()); // Output: john_doe
System.out.println(user.getId()); // Output: 1
```

## @Builder Annotation

Creates a builder pattern implementation for flexible object construction with method chaining. Generates an inner builder class with fluent API for setting properties.

```java
import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class Product {
    private String name;
    private double price;
    private String category;
    private int quantity;
    private boolean available;
}

// Usage
Product laptop = Product.builder()
    .name("ThinkPad X1")
    .price(1299.99)
    .category("Electronics")
    .quantity(50)
    .available(true)
    .build();

System.out.println(laptop);
// Output: Product(name=ThinkPad X1, price=1299.99, category=Electronics, quantity=50, available=true)

// Partial building with defaults
Product book = Product.builder()
    .name("Clean Code")
    .price(45.99)
    .category("Books")
    .build();
```

## @Data Annotation

Comprehensive annotation combining @Getter, @Setter, @ToString, @EqualsAndHashCode, and @RequiredArgsConstructor. Creates a complete mutable data class with minimal code.

```java
import lombok.Data;

@Data
public class Customer {
    private final Long id;
    private String firstName;
    private String lastName;
    private String email;
    private int age;
}

// Usage
Customer customer = new Customer(101L);
customer.setFirstName("Alice");
customer.setLastName("Johnson");
customer.setEmail("alice@example.com");
customer.setAge(28);

System.out.println(customer);
// Output: Customer(id=101, firstName=Alice, lastName=Johnson, email=alice@example.com, age=28)

Customer customer2 = new Customer(101L);
customer2.setFirstName("Alice");
customer2.setLastName("Johnson");
customer2.setEmail("alice@example.com");
customer2.setAge(28);

System.out.println(customer.equals(customer2)); // Output: true
System.out.println(customer.hashCode() == customer2.hashCode()); // Output: true
```

### Using Optional Fields and Defaults with `@Builder`

When a class has optional fields, Lombok’s `@Builder` combined with `@Builder.Default` is the **cleanest and safest way** to model them. This avoids nulls, telescoping constructors, and ad hoc initialization logic.

#### Core rule

**Optional does not mean nullable.**
Use sensible defaults in the builder instead of forcing callers to pass null or wrap everything in `Optional`.

---

### Prefer defaults over `Optional` fields

Bad
Using `Optional` as a field type adds noise and complicates serialization and persistence.

```java
@Builder
public class Config {
    Optional<Integer> timeout;   // ❌ avoid
}
```

Good
Use defaults and primitives or normal types.

```java
@Builder
public class Config {
    @Builder.Default
    private int timeoutMs = 5000;

    @Builder.Default
    private boolean cacheEnabled = true;
}
```

Why this is better
No null checks
Clear behavior without reading documentation
Works cleanly with Jackson, JPA, and frameworks

---

### `@Builder.Default` is required for defaults

A common mistake is assuming field initializers work automatically with builders. They do not.

Wrong

```java
@Builder
public class Report {
    private String status = "DRAFT"; // ❌ ignored by builder
}
```

Correct

```java
@Builder
public class Report {
    @Builder.Default
    private String status = "DRAFT";
}
```

Rule
If a field should have a default when not set by the builder, **always** add `@Builder.Default`.

---

### Use null to mean “explicitly unset”

With builders, absence already means default. Null should be a conscious override, not the default state.

```java
Report report = Report.builder().build();
// status == "DRAFT"
```

If null is meaningful, document it clearly or model it explicitly.

---

### Use `Optional` at API boundaries, not in models

If you need to express optional input from callers, use `Optional` in method parameters or factory methods, not in the class itself.

```java
public static Report from(Optional<String> status) {
    return Report.builder()
        .status(status.orElse("DRAFT"))
        .build();
}
```

---

### Combine with immutability

Defaults are most powerful when paired with immutable objects.

```java
@Value
@Builder
public class Limits {
    @Builder.Default int maxConnections = 10;
    @Builder.Default int timeoutMs = 5000;
}
```

## @Value Annotation

Creates immutable value objects with all fields final, providing getters but no setters. Ideal for DTOs and domain objects that shouldn't change after creation.

```java
import lombok.Value;

@Value
public class Point {
    int x;
    int y;
}

@Value
public class Address {
    String street;
    String city;
    String zipCode;
    String country;
}

// Usage
Point origin = new Point(0, 0);
System.out.println(origin.getX()); // Output: 0
System.out.println(origin.getY()); // Output: 0
// origin.setX(5); // Compilation error - no setters generated

Address address = new Address("123 Main St", "Boston", "02101", "USA");
System.out.println(address);
// Output: Address(street=123 Main St, city=Boston, zipCode=02101, country=USA)
```

## @ToString Annotation

Generates toString() method with customizable field inclusion and formatting. Supports excluding fields, including superclass, and controlling field names display.

```java
import lombok.ToString;

@ToString
public class Employee {
    private Long id;
    private String name;
    private String department;
    @ToString.Exclude private String socialSecurityNumber;
}

@ToString(callSuper = true, includeFieldNames = false)
public class Manager extends Employee {
    private int teamSize;
    private String level;
}

// Usage
Employee emp = new Employee();
emp.id = 1001L;
emp.name = "Bob Smith";
emp.department = "Engineering";
emp.socialSecurityNumber = "123-45-6789";

System.out.println(emp);
// Output: Employee(id=1001, name=Bob Smith, department=Engineering)
// Note: socialSecurityNumber is excluded
```

## @EqualsAndHashCode Annotation

Generates equals() and hashCode() methods based on class fields. Supports field exclusion, superclass consideration, and caching strategies for performance.

```java
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Book {
    private String isbn;
    private String title;
    private String author;
    @EqualsAndHashCode.Exclude private int copiesSold;
    @EqualsAndHashCode.Exclude private double rating;
}

// Usage
Book book1 = new Book();
book1.setIsbn("978-0134685991");
book1.setTitle("Effective Java");
book1.setAuthor("Joshua Bloch");
book1.setCopiesSold(500000);

Book book2 = new Book();
book2.setIsbn("978-0134685991");
book2.setTitle("Effective Java");
book2.setAuthor("Joshua Bloch");
book2.setCopiesSold(600000); // Different copies sold

System.out.println(book1.equals(book2)); // Output: true
// Equality based on isbn, title, author only (copiesSold excluded)
```

## @AllArgsConstructor, @NoArgsConstructor, @RequiredArgsConstructor

Generate constructors with different parameter configurations. AllArgsConstructor includes all fields, NoArgsConstructor has no parameters, RequiredArgsConstructor includes final and @NonNull fields only.

```java
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import lombok.Getter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Order {
    private Long orderId;
    private String customerName;
    private double totalAmount;
}

@RequiredArgsConstructor
@Getter
public class Account {
    @NonNull private final String accountNumber;
    @NonNull private final String accountType;
    private double balance; // Not included in RequiredArgsConstructor
}

// Usage
Order order1 = new Order(); // NoArgsConstructor
Order order2 = new Order(1L, "John", 299.99); // AllArgsConstructor

Account account = new Account("ACC-001", "CHECKING");
System.out.println(account.getAccountNumber()); // Output: ACC-001
```

## @With Annotation

Generates wither methods for creating modified copies of immutable objects. Each with method returns a new instance with one field changed, preserving immutability.

```java
import lombok.With;
import lombok.Value;

@Value
@With
public class Configuration {
    String environment;
    int maxConnections;
    int timeout;
    boolean enableCache;
}

// Usage
Configuration devConfig = new Configuration("development", 10, 5000, false);
System.out.println(devConfig);
// Output: Configuration(environment=development, maxConnections=10, timeout=5000, enableCache=false)

Configuration prodConfig = devConfig
    .withEnvironment("production")
    .withMaxConnections(100)
    .withTimeout(30000)
    .withEnableCache(true);

System.out.println(prodConfig);
// Output: Configuration(environment=production, maxConnections=100, timeout=30000, enableCache=true)

// Original remains unchanged
System.out.println(devConfig.getEnvironment()); // Output: development
```

## @NonNull Annotation

Adds null-checking to parameters, fields, and method arguments. Throws NullPointerException with descriptive message if null value is passed.

```java
import lombok.NonNull;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class UserService {
    private String serviceName;

    public void processUser(@NonNull String username, @NonNull String email) {
        System.out.println("Processing user: " + username);
        System.out.println("Email: " + email);
    }

    public void setServiceName(@NonNull String serviceName) {
        this.serviceName = serviceName;
    }
}

// Usage
UserService service = new UserService();
service.setServiceName("UserManagement");

try {
    service.processUser(null, "test@example.com");
} catch (NullPointerException e) {
    System.out.println("Caught: " + e.getMessage());
    // Output: Caught: username is marked non-null but is null
}

service.processUser("alice", "alice@example.com");
// Output: Processing user: alice
//         Email: alice@example.com
```

## @SneakyThrows Annotation

Bypasses Java's checked exception handling requirement without wrapping exceptions. Allows throwing checked exceptions without declaring them in method signature.

```java
import lombok.SneakyThrows;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

public class FileProcessor {

    @SneakyThrows(UnsupportedEncodingException.class)
    public String convertToUtf8(byte[] bytes) {
        return new String(bytes, "UTF-8");
    }

    @SneakyThrows
    public String readFile(String path) {
        // This would normally require throws IOException
        throw new IOException("File not found: " + path);
    }
}

// Usage
FileProcessor processor = new FileProcessor();

byte[] data = {72, 101, 108, 108, 111};
String text = processor.convertToUtf8(data); // No try-catch needed
System.out.println(text); // Output: Hello

try {
    processor.readFile("/nonexistent/file.txt");
} catch (Exception e) {
    System.out.println("Error: " + e.getMessage());
    // Output: Error: File not found: /nonexistent/file.txt
}
```

## @Cleanup Annotation

Ensures resources are cleaned up by calling close method automatically. Wraps code in try-finally block for automatic resource management, similar to try-with-resources.

```java
import lombok.Cleanup;
import java.io.*;

public class FileHandler {

    public void copyFile(String inputPath, String outputPath) throws IOException {
        @Cleanup FileInputStream input = new FileInputStream(inputPath);
        @Cleanup FileOutputStream output = new FileOutputStream(outputPath);

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        // input.close() and output.close() called automatically
    }

    public String readFirstLine(String filePath) throws IOException {
        @Cleanup BufferedReader reader = new BufferedReader(new FileReader(filePath));
        return reader.readLine();
        // reader.close() called automatically
    }
}

// Usage with custom cleanup method
class DatabaseConnection {
    public void disconnect() {
        System.out.println("Connection closed");
    }
}

public void performQuery() {
    @Cleanup("disconnect") DatabaseConnection conn = new DatabaseConnection();
    // Use connection
    System.out.println("Executing query...");
    // conn.disconnect() called automatically at end of scope
}
```

## @Slf4j and Logging Annotations

Generates logger fields for various logging frameworks. Supports SLF4J, Log4j, Log4j2, java.util.logging, Commons Logging, and custom loggers.

```java
import lombok.extern.slf4j.Slf4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.java.Log;

@Slf4j
public class OrderService {

    public void placeOrder(String orderId, double amount) {
        log.info("Placing order: {} with amount: {}", orderId, amount);

        try {
            // Process order
            log.debug("Processing payment for order: {}", orderId);
            if (amount <= 0) {
                log.warn("Invalid amount for order {}: {}", orderId, amount);
                throw new IllegalArgumentException("Amount must be positive");
            }
            log.info("Order {} placed successfully", orderId);
        } catch (Exception e) {
            log.error("Failed to place order {}", orderId, e);
            throw e;
        }
    }
}

@Log4j2
public class PaymentProcessor {
    public void processPayment(String transactionId) {
        log.info("Processing payment: {}", transactionId);
    }
}

@Log
public class NotificationService {
    public void sendNotification(String message) {
        log.info("Sending notification: " + message);
    }
}

// Usage
OrderService orderService = new OrderService();
orderService.placeOrder("ORD-12345", 299.99);
// Log output: Placing order: ORD-12345 with amount: 299.99
//            Processing payment for order: ORD-12345
//            Order ORD-12345 placed successfully
```

## @Builder.Default and @Singular

Advanced builder features for default values and collection handling. @Builder.Default specifies default field values, @Singular provides convenient methods for adding collection elements.

```java
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import java.util.List;
import java.util.Set;

@Builder
@ToString
public class Report {
    @Builder.Default private String status = "DRAFT";
    @Builder.Default private int version = 1;
    @Builder.Default private boolean published = false;

    @Singular private List<String> sections;
    @Singular private Set<String> tags;
    private String title;
}

// Usage
Report report = Report.builder()
    .title("Q4 Sales Report")
    .section("Executive Summary")
    .section("Sales Analysis")
    .section("Projections")
    .tag("sales")
    .tag("quarterly")
    .tag("2024")
    .build();

System.out.println(report);
// Output: Report(status=DRAFT, version=1, published=false,
//         sections=[Executive Summary, Sales Analysis, Projections],
//         tags=[sales, quarterly, 2024], title=Q4 Sales Report)

// Using defaults
Report draft = Report.builder()
    .title("Draft Document")
    .build();

System.out.println(draft.getStatus()); // Output: DRAFT
System.out.println(draft.getVersion()); // Output: 1
```

## Summary

Project Lombok dramatically reduces Java boilerplate code through compile-time code generation, making codebases more maintainable and readable. The library's main use cases include creating POJOs and DTOs with @Data or @Value, implementing the builder pattern with @Builder for complex object construction, eliminating manual getter/setter creation, automating equals/hashCode/toString implementations, and simplifying logging setup. Lombok is particularly valuable in enterprise applications with numerous data transfer objects, domain models, and configuration classes.

Integration with existing projects is straightforward through Maven or Gradle dependencies, with IDE plugins available for Eclipse, IntelliJ IDEA, and VS Code. The annotations work at compile-time with zero runtime dependencies or performance overhead. Common patterns include using @Value for immutable DTOs in API responses, @Builder for entities with many optional fields, @Data for JPA entities and form objects, @Slf4j for standardized logging across services, and @NonNull for defensive programming. Lombok pairs well with frameworks like Spring Boot, Hibernate, and Jackson, reducing configuration code while maintaining type safety and IDE support for generated methods.
