package com.fintech.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "USD|EUR|KES|GBP", message = "Currency must be USD, EUR, KES, or GBP")
    private String currency;
    
    private String accountHolderName;
}
