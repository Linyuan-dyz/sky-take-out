package com.sky.dto;

import lombok.Data;

@Data
public class EmployeeEditDTO {

    private String oldPassword;

    private String newPassword;

    private Long id;
}
