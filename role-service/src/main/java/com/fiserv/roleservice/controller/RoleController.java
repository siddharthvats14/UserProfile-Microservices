package com.fiserv.roleservice.controller;

import com.fiserv.roleservice.dto.RoleDTO;
import com.fiserv.roleservice.util.RoleCsvReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.List;

@RestController
public class RoleController {
    @GetMapping("/role-service/role/{roleId}")
    public RoleDTO getRole(@PathVariable Integer roleId) throws IOException {
        List<RoleDTO> roles = RoleCsvReader.readRolesFromCsv("role.csv");
        for (RoleDTO role : roles) {
            if (role.getRoleId().equals(roleId)) {
                return role;
            }
        }
        return null;
    }
}
