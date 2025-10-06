package com.fiserv.roleservice.util;

import com.fiserv.roleservice.dto.RoleDTO;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoleCsvReader {
    public static List<RoleDTO> readRolesFromCsv(String fileName) throws IOException {
        List<RoleDTO> roles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/" + fileName))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] values = line.split(",");
                if (values.length >= 3) {
                    RoleDTO role = new RoleDTO();
                    role.setRoleId(Integer.valueOf(values[0]));
                    role.setRoleName(values[1]);
                    role.setDescription(values[2]);
                    roles.add(role);
                }
            }
        }
        return roles;
    }
}
