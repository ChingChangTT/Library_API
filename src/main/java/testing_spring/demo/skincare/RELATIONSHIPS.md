# Learning example: one category, many products

```text
skincare_category                 skincare_product
id (primary key) <--------------- category_id (foreign key)
name                              id, name, brand, price, stock
```

"Day Cream" and "Night Cream" can reference the same "Moisturizer" category ID.
The category name is stored once in `skincare_category`.

## Follow the code

1. `SkincareCategory.java` defines the category table and its generated primary key.
2. `SkincareProduct.java` uses `@ManyToOne`: many products reference one category.
   `@JoinColumn(name = "category_id")` maps the foreign key on the product table.
3. `SkincareProductService.resolveCategory` finds or creates a category by its
   trimmed name and assigns that entity to the product. Matching is case-sensitive.
4. `SkincareProductRepository` searches `Category_Name`, following the relationship.
5. `SkincareProductResponse` returns the category name in the existing JSON format.
6. `V3__add_skincare_categories.sql` preserves existing products, copies category
   names to the shared table, and replaces the text column with a required foreign key.

Java navigates from product to category: this is a unidirectional mapping. From
the category's perspective the relationship is one-to-many. A Java `@OneToMany`
collection is optional; the database relationship already exists without it.

Eager loading lets the controller read the category after the service transaction
closes. Larger applications often use lazy loading with deliberate fetching and
response mapping inside transactions to control query counts.

There is no delete cascade: deleting a product leaves its category intact. The
foreign key prevents deleting a category while products still reference it.
Concurrent requests creating the same new category can race in this simple
find-then-create example; the unique constraint rejects duplicates. A production
workflow can use an upsert or retry.

## Try it

Start with `.\mvnw.cmd spring-boot:run` on Windows. Open
`http://localhost:8082/docs` and send this to `POST /api/skincare-products` twice,
changing the second product's name to `Night Cream`:

```json
{"name":"Day Cream","brand":"Learning Brand","category":"Moisturizer","price":12.50,"stock":10}
```

In the development H2 console (`http://localhost:8082/h2-console`), connect to
`jdbc:h2:mem:librarydb` with username `sa` and an empty password. Run:

```sql
SELECT p.id, p.name, p.category_id, c.name AS category_name
FROM skincare_product p
JOIN skincare_category c ON c.id = p.category_id;
```

Both creams share a category ID. Try `GET /api/skincare-products?keyword=Moisturizer`
to search them. Delete one cream and repeat the query: the other keeps its category.

Run `.\mvnw.cmd test` for the relationship tests, including shared categories,
reassignment, deletion, search, migrated seed data, and foreign-key enforcement.
