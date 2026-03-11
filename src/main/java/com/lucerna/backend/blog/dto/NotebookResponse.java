package com.lucerna.backend.blog.dto;

import com.lucerna.backend.blog.entity.Notebook;
import com.lucerna.backend.blog.entity.VisibilityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotebookResponse {

    private Long id;
    private String name;
    private String colorCode;
    private VisibilityStatus visibilityStatus;
    private LocalDateTime createdAt;

    public static NotebookResponse from(Notebook notebook){
        return NotebookResponse.builder()
                .id(notebook.getId())
                .name(notebook.getName())
                .colorCode(notebook.getColorCode())
                .visibilityStatus(notebook.getVisibilityStatus())
                .createdAt(notebook.getCreatedAt())
                .build();
    }
}