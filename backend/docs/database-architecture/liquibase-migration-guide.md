# **SwiftTrack – Liquibase Database Migration Guide**

This document provides a comprehensive guide to using Liquibase for database schema management in SwiftTrack microservices. Liquibase enables version-controlled, repeatable, and auditable database migrations.

---

## **1. What is Liquibase?**

Liquibase is a database schema change management tool that tracks, versions, and deploys database changes. Instead of using Hibernate's `ddl-auto: update`, Liquibase provides:

- **Version Control**: All schema changes are tracked in changelog files
- **Repeatability**: Same migrations can be applied across all environments (dev, staging, prod)
- **Rollback Support**: Changes can be reverted if needed
- **Audit Trail**: `DATABASECHANGELOG` table tracks all applied changes
- **Team Collaboration**: Multiple developers can add migrations without conflicts

---

## **2. Why Liquibase Over Hibernate DDL-Auto?**

| Aspect | Hibernate `ddl-auto: update` | Liquibase |
|--------|------------------------------|-----------|
| Production Safety | ❌ Risky - can cause data loss | ✅ Safe - explicit migrations |
| Column Renames | ❌ Creates new column, drops old | ✅ Proper rename with data preservation |
| Rollbacks | ❌ Not supported | ✅ Full rollback support |
| Audit Trail | ❌ No history | ✅ Complete change history |
| Team Workflows | ❌ Conflicts possible | ✅ Sequential changelog IDs |
| Data Migrations | ❌ Not supported | ✅ Fully supported |

---

## **3. Project Structure**

```
src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.yaml    # Master changelog (includes all changes)
        └── changes/
            ├── 001-create-orders-table.yaml
            ├── 002-create-order-locations-table.yaml
            ├── 003-create-order-provider-assignments-table.yaml
            ├── 004-create-order-tracking-events-table.yaml
            ├── 005-create-order-tracking-state-table.yaml
            ├── 006-create-order-ai-features-table.yaml
            ├── 007-create-order-quote-sessions-table.yaml
            └── 008-create-order-quotes-table.yaml
```

---

## **4. Configuration**

### **4.1 Maven Dependency (pom.xml)**

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

### **4.2 Application Configuration (application.yaml)**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none  # IMPORTANT: Disable Hibernate DDL
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
```

### **4.3 Optional Maven Plugin (for CLI commands)**

Add to `pom.xml` for additional Liquibase commands:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-maven-plugin</artifactId>
            <version>4.29.2</version>
            <configuration>
                <changeLogFile>src/main/resources/db/changelog/db.changelog-master.yaml</changeLogFile>
                <driver>org.postgresql.Driver</driver>
                <url>${env.DB_URL}</url>
                <username>${env.DB_USERNAME}</username>
                <password>${env.DB_PASSWORD}</password>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## **5. Changelog File Format**

### **5.1 Master Changelog**

The master changelog includes all individual change files in order:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/001-create-orders-table.yaml
  - include:
      file: db/changelog/changes/002-create-order-locations-table.yaml
```

### **5.2 Individual Changelog Structure**

```yaml
databaseChangeLog:
  - changeSet:
      id: unique-changeset-id       # Must be unique across all changelogs
      author: developer-name        # Who created this change
      changes:
        - createTable:              # The actual change
            tableName: my_table
            columns:
              - column:
                  name: id
                  type: uuid
                  constraints:
                    primaryKey: true
```

---

## **6. Common Operations**

### **6.1 Creating a New Table**

```yaml
databaseChangeLog:
  - changeSet:
      id: 009-create-new-table
      author: swifttrack
      changes:
        - createTable:
            tableName: my_new_table
            columns:
              - column:
                  name: id
                  type: uuid
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: name
                  type: varchar(100)
                  constraints:
                    nullable: false
              - column:
                  name: created_at
                  type: timestamp
```

### **6.2 Adding a New Column**

```yaml
databaseChangeLog:
  - changeSet:
      id: 010-add-email-to-orders
      author: swifttrack
      changes:
        - addColumn:
            tableName: orders
            columns:
              - column:
                  name: customer_email
                  type: varchar(255)
```

### **6.3 Renaming a Column** ⚠️

This is the most important operation when migrating from Hibernate DDL!

```yaml
databaseChangeLog:
  - changeSet:
      id: 011-rename-customer-reference-id
      author: swifttrack
      changes:
        - renameColumn:
            tableName: orders
            oldColumnName: customer_reference_id
            newColumnName: external_reference_id
            columnDataType: varchar(100)
```

### **6.4 Modifying Column Type**

```yaml
databaseChangeLog:
  - changeSet:
      id: 012-modify-column-type
      author: swifttrack
      changes:
        - modifyDataType:
            tableName: orders
            columnName: customer_reference_id
            newDataType: varchar(200)
```

### **6.5 Adding an Index**

```yaml
databaseChangeLog:
  - changeSet:
      id: 013-add-index
      author: swifttrack
      changes:
        - createIndex:
            indexName: idx_orders_customer_email
            tableName: orders
            columns:
              - column:
                  name: customer_email
```

### **6.6 Adding a Foreign Key**

```yaml
databaseChangeLog:
  - changeSet:
      id: 014-add-foreign-key
      author: swifttrack
      changes:
        - addForeignKeyConstraint:
            baseTableName: order_items
            baseColumnNames: order_id
            constraintName: fk_order_items_order
            referencedTableName: orders
            referencedColumnNames: id
            onDelete: CASCADE
```

### **6.7 Dropping a Column**

```yaml
databaseChangeLog:
  - changeSet:
      id: 015-drop-deprecated-column
      author: swifttrack
      changes:
        - dropColumn:
            tableName: orders
            columnName: deprecated_field
```

### **6.8 Data Migration (SQL)**

```yaml
databaseChangeLog:
  - changeSet:
      id: 016-migrate-data
      author: swifttrack
      changes:
        - sql:
            sql: UPDATE orders SET order_status = 'CREATED' WHERE order_status IS NULL
```

---

## **7. Handling Schema Changes (Step-by-Step)**

When you need to change your entity (e.g., rename a column), follow these steps:

### **Step 1: Create a New Changelog File**

Create a new file in `db/changelog/changes/` with the next sequential number:

```
009-rename-customer-reference-id.yaml
```

### **Step 2: Write the Migration**

```yaml
databaseChangeLog:
  - changeSet:
      id: 009-rename-customer-reference-id
      author: your-name
      changes:
        - renameColumn:
            tableName: orders
            oldColumnName: customer_reference_id
            newColumnName: external_reference_id
            columnDataType: varchar(100)
```

### **Step 3: Add to Master Changelog**

Update `db.changelog-master.yaml`:

```yaml
databaseChangeLog:
  # ... existing includes ...
  - include:
      file: db/changelog/changes/009-rename-customer-reference-id.yaml
```

### **Step 4: Update the JPA Entity**

Update your Java entity to match:

```java
@Column(name = "external_reference_id", length = 100)
private String externalReferenceId;
```

### **Step 5: Restart the Application**

Liquibase will automatically apply the new migration on startup.

---

## **8. Rollback Support**

### **8.1 Automatic Rollback**

Liquibase can auto-generate rollbacks for many operations:

```yaml
databaseChangeLog:
  - changeSet:
      id: 017-add-column-with-rollback
      author: swifttrack
      changes:
        - addColumn:
            tableName: orders
            columns:
              - column:
                  name: priority
                  type: integer
      rollback:
        - dropColumn:
            tableName: orders
            columnName: priority
```

### **8.2 Execute Rollback (via Maven)**

```bash
# Rollback last changeset
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Rollback to a specific tag
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0.0
```

---

## **9. Best Practices**

### **✅ DO:**

1. **Use sequential numbering** for changelog files (001, 002, 003...)
2. **One logical change per changeset** for easier rollbacks
3. **Always include rollback** for complex operations
4. **Test migrations** on a copy of production data before deploying
5. **Use meaningful changeset IDs** that describe the change
6. **Add comments** for complex migrations
7. **Never modify** an already-applied changeset - create a new one

### **❌ DON'T:**

1. **Never delete** or modify an existing changelog that has been applied
2. **Never use** `ddl-auto: update` or `create` with Liquibase
3. **Never skip** changeset numbers (001, 002, 004 - missing 003)
4. **Never use** the same changeset ID twice

---

## **10. Useful Maven Commands**

```bash
# Validate changelog syntax
mvn liquibase:validate

# Preview pending changes (dry-run)
mvn liquibase:updateSQL

# Apply all pending changes
mvn liquibase:update

# Rollback last N changesets
mvn liquibase:rollback -Dliquibase.rollbackCount=N

# Generate changelog from existing database
mvn liquibase:generateChangeLog

# Mark changesets as executed (for existing databases)
mvn liquibase:changelogSync

# Show changelog status
mvn liquibase:status

# Clear all checksums (use with caution)
mvn liquibase:clearCheckSums

# Tag current database state
mvn liquibase:tag -Dliquibase.tag=v1.0.0
```

---

## **11. Database Tracking Tables**

Liquibase creates two tables in your database:

### **DATABASECHANGELOG**

Tracks all applied changesets:

| Column | Description |
|--------|-------------|
| ID | Changeset ID |
| AUTHOR | Who created the changeset |
| FILENAME | Changelog file path |
| DATEEXECUTED | When it was applied |
| MD5SUM | Checksum to detect modifications |

### **DATABASECHANGELOGLOCK**

Prevents concurrent migrations:

| Column | Description |
|--------|-------------|
| LOCKED | Whether migration is running |
| LOCKGRANTED | When lock was acquired |
| LOCKEDBY | Which instance holds the lock |

---

## **12. Troubleshooting**

### **Issue: Checksum Validation Failed**

If you modified an already-applied changeset:

```bash
# Option 1: Clear the checksum (changeset will be re-validated)
mvn liquibase:clearCheckSums

# Option 2: Mark the changeset as valid (keeps existing checksum)
# Update DATABASECHANGELOG table manually
```

### **Issue: Migration Fails on Existing Database**

If Liquibase tries to create tables that already exist:

```bash
# Mark all changesets as applied without executing
mvn liquibase:changelogSync
```

### **Issue: Lock Not Released**

If a previous migration failed and left the lock:

```sql
-- Manually release the lock
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE;
```

---

## **13. Environment-Specific Configuration**

Use Spring profiles for different environments:

```yaml
# application-dev.yaml
spring:
  liquibase:
    enabled: true
    contexts: dev

# application-prod.yaml  
spring:
  liquibase:
    enabled: true
    contexts: prod
```

Then in changelogs:

```yaml
databaseChangeLog:
  - changeSet:
      id: 018-dev-only-data
      author: swifttrack
      context: dev
      changes:
        - sql:
            sql: INSERT INTO orders (id, ...) VALUES (...)
```

---

## **14. Migration from Existing Database**

If you already have tables created by Hibernate, follow these steps:

### **Step 1: Generate Changelog from Existing Schema**

```bash
mvn liquibase:generateChangeLog -Dliquibase.outputChangeLogFile=existing-schema.yaml
```

### **Step 2: Sync Changelog with Database**

This marks all changesets as executed without running them:

```bash
mvn liquibase:changelogSync
```

### **Step 3: Verify**

Check `DATABASECHANGELOG` table to confirm all changesets are marked as applied.

---

## **15. Quick Reference Card**

| Task | Changelog Operation |
|------|---------------------|
| Create table | `createTable` |
| Drop table | `dropTable` |
| Add column | `addColumn` |
| Drop column | `dropColumn` |
| Rename column | `renameColumn` |
| Change column type | `modifyDataType` |
| Add primary key | `addPrimaryKey` |
| Add foreign key | `addForeignKeyConstraint` |
| Add index | `createIndex` |
| Drop index | `dropIndex` |
| Add constraint | `addUniqueConstraint`, `addNotNullConstraint` |
| Raw SQL | `sql` |
| Insert data | `insert` |
| Update data | `update` |

---

This document is part of the SwiftTrack technical documentation. For questions, refer to the database architecture docs or contact the platform team.
