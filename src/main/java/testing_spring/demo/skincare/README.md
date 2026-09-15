# Skincare Package Guide

This package implements CRUD (Create, Read, Update, Delete) operations for skincare
products. A request normally moves through the application like this:

```text
HTTP request -> Controller -> Service -> Repository -> Database
```

## Files

| File | Purpose |
| --- | --- |
| `SkincareProduct.java` | Defines the skincare product database entity. |
| `SkincareProductRequest.java` | The place where you define the requirements
| `SkincareProductRepository.java` | Provides database operations and the product search query. | describes how the body sent by the client
| `SkincareProductService.java` | Contains validation and business logic. |
| `SkincareProductController.java` | Defines the HTTP REST API endpoints. |
| `SkincareProductNotFoundException.java` | Represents a request for a product ID that does not exist. |
| `SkincareProductValidationException.java` | Represents invalid product input. |

## JPA Entity Keywords and Annotations

These are used in `SkincareProduct.java`.

| Keyword or annotation | Meaning |
| --- |  --- |
| `@Entity` | Tells JPA/Hibernate that this class represents or maps to a database table. By default, the table name is based on the class name. |
| `@Id` | Marks `id` as the table's primary key. |
| `@GeneratedValue` | Tells JPA to generate the primary-key value for a new product. The application does not need to assign the ID manually. |
| `strategy = GenerationType.IDENTITY` | Uses the database's identity or auto-increment column to generate IDs. |
| `@Column` | Configures the database column mapped to a field. |
| `nullable = false` | Makes a database column required; it must not contain SQL `NULL`. |
| `precision = 10` | Allows up to 10 total digits in the decimal value. |
| `scale = 2` | Allows 2 digits after the decimal point. For example, `12345678.99` fits. |
| `BigDecimal` | Stores decimal numbers accurately and is preferred over `double` for money. |
| No-argument constructor | `public SkincareProduct()` is required by JPA so it can create an entity when reading a database row. |
| Getters and setters | Allow application code and frameworks to read or change entity fields. |

`nullable = false` protects the database, while the service validation gives clients a
clear error before invalid data reaches the database. Both have a purpose.

## Controller Annotations and HTTP Keywords

These are used in `SkincareProductController.java`.

| Keyword or annotation | Meaning |
| --- | --- |
| `@RestController` | Marks the class as a REST controller. Return values are written to the HTTP response body, normally as JSON. |
| `@RequestMapping("/api/skincare-products")` | Sets the common URL prefix for every endpoint in the controller. |
| `@GetMapping` | Handles an HTTP `GET` request, normally used to read data. |
| `@PostMapping` | Handles an HTTP `POST` request, used here to create a product. |
| `@PutMapping` | Handles an HTTP `PUT` request, used here to update a product. |
| `@DeleteMapping` | Handles an HTTP `DELETE` request. |
| `@PathVariable` | Reads a value from the URL path. In `/{id}`, it supplies the `id` argument. |
| `@RequestParam` | Reads a query-string value. For example, `/search?keyword=cream`. |
| `@RequestBody` | Converts the incoming JSON body into a `SkincareProductRequest`. |
| `ResponseEntity<T>` | Represents the complete HTTP response, including status, headers, and a body of type `T`. |
| `ResponseEntity.created(...)` | Returns HTTP status `201 Created` and adds a `Location` header for the new product. |
| `ResponseEntity.ok(...)` | Returns HTTP status `200 OK`. |
| `URI.create(...)` | Creates the URI used in the response's `Location` header. |

### Endpoints

| Method | URL | Action |
| --- | --- | --- |
| `GET` | `/api/skincare-products` | Get every product. |
| `GET` | `/api/skincare-products/{id}` | Get one product by ID. |
| `GET` | `/api/skincare-products/search?keyword=value` | Search by name, brand, or category. |
| `POST` | `/api/skincare-products` | Create a product from a JSON request body. |
| `PUT` | `/api/skincare-products/{id}` | Replace a product's editable values. |
| `DELETE` | `/api/skincare-products/{id}` | Delete a product. |

## Service and Transaction Keywords

These are used in `SkincareProductService.java`.

| Keyword or annotation | Meaning |
| --- | --- |
| `@Service` | Registers the class as a Spring service containing business logic. |
| `@Transactional` | Runs a method inside a database transaction. On the class, it applies to all public methods unless a method overrides it. |
| `@Transactional(readOnly = true)` | Marks a transaction as read-only for query methods. This communicates intent and may allow performance optimizations. |
| `validate(request)` | Checks required text, price, and stock before saving data. |
| `trim()` | Removes whitespace from the beginning and end of a string. |
| `isBlank()` | Returns `true` when a string is empty or contains only whitespace. |
| `BigDecimal.ZERO` | An exact decimal value of zero used when checking whether a price is negative. |
| `compareTo(BigDecimal.ZERO) < 0` | Means the price is less than zero. `compareTo` should be used for numeric `BigDecimal` comparisons. |
| `orElseThrow(...)` | Returns the product inside an `Optional`, or throws an exception when it is absent. |
| `() -> ...` | A lambda expression. Here it delays creation of the exception until it is needed. |

The controller receives HTTP data, but the service owns validation and business rules.
This also allows the same rules to be reused outside the controller.

## Repository Keywords

These are used in `SkincareProductRepository.java`.

The repository is the database-access layer for skincare products:

```java
public interface SkincareProductRepository
        extends JpaRepository<SkincareProduct, Long> {
    // Custom search method
}
```

`SkincareProduct` tells `JpaRepository` which entity it manages, while `Long`
is the type of that entity's `id`. Because this is a Spring Data repository,
Spring creates the implementation when the application starts. We only declare
the interface and do not need to write a repository class ourselves.

| Keyword or API | Meaning |
| --- | --- |
| `interface` | Declares a contract. Spring creates the repository implementation at runtime. |
| `extends JpaRepository<SkincareProduct, Long>` | Inherits JPA database operations for a `SkincareProduct` entity whose ID type is `Long`. |
| `findAll()` | Returns all products. Inherited from `JpaRepository`. |
| `findById(id)` | Returns an `Optional` containing the matching product when it exists. |
| `save(product)` | Inserts a new entity or updates an existing entity. |
| `existsById(id)` | Checks whether a row with the given ID exists. |
| `deleteById(id)` | Deletes the row with the given ID. |
| `List<SkincareProduct>` | A collection containing zero or more products. The generic type states what the list contains. |

For example, the service can call the inherited methods directly:

```java
repository.findAll();        // Get all products
repository.findById(1L);     // Find the product whose ID is 1
repository.save(product);    // Insert a new product or update an existing one
repository.existsById(1L);   // Check whether product ID 1 exists
repository.deleteById(1L);   // Delete product ID 1
```

Spring Data JPA builds this query from the method name:

```java
List<SkincareProduct>
findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
        String name, String brand, String category);
```

The name is interpreted as follows:

| Name part | Meaning |
| --- | --- |
| `findBy` | Start a database query. |
| `Name`, `Brand`, `Category` | Entity fields to search. |
| `Containing` | Match text containing the provided value, similar to SQL `LIKE %value%`. |
| `IgnoreCase` | Ignore uppercase and lowercase differences. |
| `Or` | A product matches when any one of the three field conditions is true. |

The service passes the same search value into all three parameters:

```java
repository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
        value, value, value);
```

Searching for `cream` therefore matches a product when `cream` appears anywhere
in its name, brand, or category, even if the capitalization is different. The
generated query is approximately equivalent to:

```sql
SELECT *
FROM skincare_product
WHERE LOWER(name) LIKE LOWER('%cream%')
   OR LOWER(brand) LIKE LOWER('%cream%')
   OR LOWER(category) LIKE LOWER('%cream%');
```

The repository's place in the request flow is:

```text
Controller -> Service -> Repository -> Database
```

## Request Record

`SkincareProductRequest` is declared as a Java `record`:

```java
public record SkincareProductRequest(
        String name,
        String brand,
        String category,
        BigDecimal price,
        Integer stock
) {}
```

| Keyword | Meaning |
| --- | --- |
| `record` | Defines a compact data-carrier class. Java generates its constructor, accessors, `equals`, `hashCode`, and `toString`. |
| `request.name()` | Calls the generated accessor for `name`. Record accessors do not start with `get`. |
| `String` | Stores text. |
| `Integer` | Object form of `int`. It can be `null`, which allows validation to detect a missing JSON value. |

## Exceptions and Java Keywords

| Keyword or API | Meaning |
| --- | --- |
| `class` | Defines a Java class. |
| `public` | Makes a class, constructor, or method accessible from other classes. |
| `private` | Restricts a field or method to its own class. |
| `final` | Means a field reference is assigned once. It is used for constructor-injected dependencies. |
| `extends` | Creates a subtype and inherits behavior from a parent class or interface. |
| `RuntimeException` | An unchecked exception; callers are not required to catch or declare it. |
| `throw` | Stops the current flow by raising an exception. |
| `new` | Creates a new object. |
| `super(message)` | Calls the parent `RuntimeException` constructor and stores the error message. |
| `return` | Ends a method and sends a value back to its caller. |
| `void` | Means a method does not return a value. |
| `if` | Runs code only when its condition is true. |
| `null` | Represents the absence of an object value. |
| `this` | Refers to the current object. For example, `this.service = service` assigns the constructor argument to the field. |

Both custom exceptions describe domain-specific failures. The application's exception
handling layer can convert them into clear HTTP error responses.

## Dependency Injection

The controller and service use constructor injection:

```java
public SkincareProductController(SkincareProductService service) {
    this.service = service;
}
```

Spring finds the required component and supplies it when creating the class. No
`@Autowired` annotation is required when a Spring bean has a single constructor.

//common to check memory
ctr + shift + esp
