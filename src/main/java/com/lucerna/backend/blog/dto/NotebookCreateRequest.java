package com.lucerna.backend.blog.dto;

import com.lucerna.backend.blog.entity.VisibilityStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotebookCreateRequest {
    private Long lodgeId;
    private String name;
    private String colorCode;
    private Integer displayOrder;
    private VisibilityStatus visibilityStatus;
}