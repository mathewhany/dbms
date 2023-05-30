# DBMS
- Supports 4 datatypes (strings, integers, doubles, dates) and uses the strategy design pattern to allow for adding more data types easily.
- Stores pages and indicies in serialized object files, and uses strategy design pattern to make it easier to support other storage strategies.
- Supports multi dimensional queries, range or partial queries using an Octree index.
- Uses both the factory and strategy design patterns to allow supporting more indices easily.
- Saves metadata about tables in a CSV format, and uses strategy pattern to easily support other formats in the futures.
- The SQL parser supports SQL operations for: "CREATE TABLE", "CREATE INDEX", "INSERT", "UPDATE", "DELETE", "SELECT"
