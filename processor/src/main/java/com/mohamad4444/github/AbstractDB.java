package com.mohamad4444.github;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractDB<T> {

    protected abstract T buildDTO(ResultSet rs) throws SQLException;

    // Example of a generic query execution method
    protected List<T> executeQuery(Connection cn, String sql, Object... params) {
        // Implement query execution logic
        return null; // Placeholder
    }

    protected T executeSingleQuery(Connection cn, String sql, Object... params) {
        // Implement single row query logic
        return null; // Placeholder
    }

    protected int executeUpdate(Connection cn, String sql, Object... params) {
        // Implement update/insert/delete operation
        return 0; // Placeholder
    }
}
