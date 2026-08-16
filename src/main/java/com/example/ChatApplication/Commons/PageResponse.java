package com.example.ChatApplication.Commons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T>{
    private List<T> content;
    private int number;
    private int size;
    private int totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
