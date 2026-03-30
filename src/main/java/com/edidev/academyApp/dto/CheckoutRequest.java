package com.edidev.academyApp.dto;

import com.edidev.academyApp.enums.StorePaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String customerName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String customerEmail;

    private String customerPhone;

    @NotNull(message = "El método de pago es obligatorio")
    private StorePaymentMethod paymentMethod;

    private String shippingAddress;
    private String notes;
}
