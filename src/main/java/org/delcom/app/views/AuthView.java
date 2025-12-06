package org.delcom.app.views;

import org.delcom.app.services.UserService;
import org.springframework.stereotype.Component;
import java.util.Scanner;

@Component
public class AuthView {

    private final UserService userService;

    public AuthView(UserService userService) {
        this.userService = userService;
    }

    public void showLogin() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== LOGIN ===");
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (userService.authenticate(email, password)) {
            System.out.println("Login Successful!");
        } else {
            System.out.println("Invalid Email or Password.");
        }
    }
    
    public void showRegister() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== REGISTER ===");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        userService.createUser(name, email, password);
        System.out.println("Registration Successful! Please Login.");
    }
}