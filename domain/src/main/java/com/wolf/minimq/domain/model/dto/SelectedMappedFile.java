package com.wolf.minimq.domain.model.dto;

import com.wolf.minimq.domain.service.store.infra.MappedFile;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectedMappedFile implements Serializable {
    private int size;
    protected MappedFile mappedFile;
}