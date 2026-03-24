package com.edidev.academyApp.dto;

import com.edidev.academyApp.model.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentDTO {
    private Long studentId;
    private RoleName roleName;
}
