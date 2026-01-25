# JPA UPDATE / DELETE — Interview Notes

## 1. Why `@Transactional` is required

* **JPA standard mandates** that all data-modifying operations (`UPDATE`, `DELETE`, `INSERT`) must run inside a transaction.
* Without a transaction, changes may fail at runtime or may not be persisted.

**Best practice:**

* Keep `@Transactional` at the **service layer**, not the repository layer.

---

## 2. Why `@Modifying` is needed

* `@Modifying` is **NOT part of JPA**.
* It is a **Spring Data JPA–specific annotation**.
* Spring Data assumes every `@Query` is a SELECT query.
* `@Modifying` tells Spring:

  > This query modifies data, so execute it using `executeUpdate()` instead of `getResultList()`.

Without `@Modifying`, Spring throws:

```
Query executed via 'getResultList()' must be a 'select' query
```

---

## 3. Why UPDATE / DELETE queries don’t return entities

* JPQL UPDATE and DELETE are **bulk operations**.
* They:

  * bypass the persistence context (1st-level cache)
  * do not trigger entity lifecycle callbacks
* Therefore, they can only return:

  * `int` (number of rows affected), or
  * `void`

Returning an entity from a bulk query is invalid.

---

## 4. `clearAutomatically` & `flushAutomatically`

These are **Spring Data JPA conveniences**, not part of the JPA specification.

### `flushAutomatically = true`

* Flushes pending entity changes **before** executing the bulk JPQL query.
* Prevents out-of-order SQL execution when mixing entity updates and bulk updates.

### `clearAutomatically = true`

* Clears the persistence context **after** executing the bulk update.
* Prevents stale entity data remaining in memory.

Used when:

* bulk JPQL is mixed with managed entities in the same transaction.

---

## 5. Preferred patterns

### ✅ Safe & clean (default choice)

```java
@Transactional
Order order = repository.findById(id).orElseThrow();
order.setStatus(status);
```

* Uses JPA dirty checking
* Lifecycle callbacks are triggered
* Easier to debug and maintain

---

### ✅ Bulk update (performance-critical cases)

```java
@Transactional
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
int updateStatus(UUID id, OrderStatus status);
```

---

## 6. Interview-ready one-liner

> `@Transactional` is required by JPA for all data-modifying operations, while `@Modifying` is a Spring Data JPA annotation that enables correct execution of JPQL UPDATE and DELETE queries and proper persistence-context handling.

---

## 7. Key takeaway

* `@Transactional` → **JPA requirement**
* `@Modifying` → **Spring Data JPA requirement**
* `clearAutomatically` / `flushAutomatically` → **Spring Data safety features**
* Prefer dirty checking; use bulk updates only when needed
