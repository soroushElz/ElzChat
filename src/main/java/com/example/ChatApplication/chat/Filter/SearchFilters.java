package com.example.ChatApplication.chat.Filter;

import com.example.ChatApplication.chat.Filter.FieldFilter;
import com.example.ChatApplication.chat.Filter.Operation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchFilters {
    @NotNull
    private FieldFilter Field;
    @NotNull
    @NotEmpty
    private String Value;
    @NotNull
    private Operation operation;
}
