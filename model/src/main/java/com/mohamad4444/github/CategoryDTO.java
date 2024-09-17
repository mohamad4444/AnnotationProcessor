package com.mohamad4444.github;
import com.mohamad4444.github.DbEntity;
import com.mohamad4444.github.DbField;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@DbEntity(tableName = "ev_category") 
public class CategoryDTO {

    @DbField(columnName = "ekid", isPrimaryKey = true)
    private int ekid;

    @DbField(columnName = "category_name")
    private String categoryName;

    @DbField(columnName = "color")
    private String color;

    @DbField(columnName = "evid")
    private int evid;
}
