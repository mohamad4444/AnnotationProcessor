package com.mohamad4444.github;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.mohamad4444.github.CategoryDTO;
public class CategoryDTODB extends AbstractDB<CategoryDTO> {

    @Override
    protected CategoryDTO buildDTO(ResultSet rs) throws SQLException {
        return new CategoryDTO(rs.getInt("ekid"), rs.getString("category_name"), rs.getString("color"), rs.getInt("evid"));
    }

}
