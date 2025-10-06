package com.fiserv.userservice.util;

import com.fiserv.userservice.dto.UserDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UserCsvReader {
        public static void appendUserToCsv(String filePath, UserDTO user) throws IOException {
            java.io.File file = new java.io.File(filePath);
            boolean fileExists = file.exists();
            boolean needsNewline = false;
            if (fileExists && file.length() > 0) {
                try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r")) {
                    raf.seek(file.length() - 1);
                    int lastByte = raf.read();
                    if (lastByte != '\n') {
                        needsNewline = true;
                    }
                }
            }
            try (java.io.FileWriter fw = new java.io.FileWriter(file, true)) {
                if (!fileExists) {
                    fw.write("userId,loginName,password,personId,retry\n");
                }
                if (needsNewline) {
                    fw.write("\n");
                }
                fw.write(user.getUserId() + "," + user.getLoginName() + "," + user.getPassword() + "," + user.getPersonId() + "," + (user.getRetry() != null ? user.getRetry() : 0) + "\n");
            }
        }
    public static List<UserDTO> readUsersFromCsv(String filePath) throws IOException {
        List<UserDTO> users = new ArrayList<>();
        java.io.File file = new java.io.File("src/main/resources/user.csv");
        if (!file.exists()) {
            throw new IOException("File not found: src/main/resources/user.csv");
        }
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 5) continue;
                UserDTO user = new UserDTO();
                user.setUserId(Integer.valueOf(parts[0]));
                user.setLoginName(parts[1]);
                user.setPassword(parts[2]);
                user.setPersonId(Integer.valueOf(parts[3]));
                user.setRetry(Integer.valueOf(parts[4]));
                users.add(user);
                System.out.println("Loaded user: " + user.getLoginName() + ", password: " + user.getPassword());
            }
        }
        return users;
    }
}
