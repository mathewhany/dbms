package dbms.config;

import dbms.DBAppException;

public interface ConfigManager {
    Config load() throws DBAppException;
}
