package com.perfect8.admin.config;

import com.perfect8.admin.model.AdminUser;
import com.perfect8.admin.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminUserService adminUserService;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdminUsers();
    }

    private void createDefaultAdminUsers() {
        // Create Super Admin if not exists
        if (!adminUserService.existsByUsername("admin")) {
            AdminUser superAdmin = new AdminUser();
            superAdmin.setUsername("admin");
            superAdmin.setEmail("admin@perfect8.com");
            superAdmin.setPassword("password123"); // Will be encrypted by service
            superAdmin.setFirstName("Perfect8");
            superAdmin.setLastName("Admin");
            superAdmin.setRole(AdminUser.Role.SUPER_ADMIN);
            superAdmin.setActive(true);

            adminUserService.save(superAdmin);
            System.out.println("✅ Super Admin user created: admin / password123");
        }

        // Create Shop Manager if not exists
        if (!adminUserService.existsByUsername("shop_manager")) {
            AdminUser shopManager = new AdminUser();
            shopManager.setUsername("shop_manager");
            shopManager.setEmail("manager@perfect8.com");
            shopManager.setPassword("password123");
            shopManager.setFirstName("Shop");
            shopManager.setLastName("Manager");
            shopManager.setRole(AdminUser.Role.SHOP_ADMIN);
            shopManager.setActive(true);

            adminUserService.save(shopManager);
            System.out.println("✅ Shop Manager user created: shop_manager / password123");
        }

        // Create Content Editor if not exists
        if (!adminUserService.existsByUsername("content_editor")) {
            AdminUser contentEditor = new AdminUser();
            contentEditor.setUsername("content_editor");
            contentEditor.setEmail("editor@perfect8.com");
            contentEditor.setPassword("password123");
            contentEditor.setFirstName("Content");
            contentEditor.setLastName("Editor");
            contentEditor.setRole(AdminUser.Role.CONTENT_ADMIN);
            contentEditor.setActive(true);

            adminUserService.save(contentEditor);
            System.out.println("✅ Content Editor user created: content_editor / password123");
        }
    }
}