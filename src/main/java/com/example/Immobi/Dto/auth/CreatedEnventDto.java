package com.example.Immobi.Dto.auth;

import com.example.Immobi.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

 /* Author: QuanNH
 * DTO for user creation event
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatedEnventDto {
    /**
     * User entity
     */
    private User user;
}
