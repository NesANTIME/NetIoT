package com.netiot.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequestDTO {

    @NotBlank(message = "La contraseña actual es obligatoria.")
    @Size(max = 72, message = "La contraseña no puede exceder 72 caracteres.")
    private String oldPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 10, max = 72, message = "La nueva contraseña debe tener entre 10 y 72 caracteres.")
    private String newPassword;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}