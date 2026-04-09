package com.hunts.bail.service;

import java.util.List;

/**
 * Delivers outcome feedback to the acting user.
 *
 *
 * OOD principles applied:
 *  - Single Responsibility : notification delivery only.
 *  - Open/Closed Principle : output channel can be swapped (e.g. to a GUI
 *    callback) by subclassing or injecting a different implementation.
 */
public class NotificationService {

    private static final String GREEN = "\u001B[32m";
    private static final String RED   = "\u001B[31m";
    private static final String YELLOW= "\u001B[33m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String LINE  =
            "─────────────────────────────────────────────────────────";

    public void notifySuccess(String title, String detail) {
        System.out.println(LINE);
        System.out.println(BOLD + GREEN + "✔  " + title + RESET);
        System.out.println("   " + detail);
        System.out.println(LINE);
    }

    public void notifyError(String title, List<String> errors) {
        System.err.println(LINE);
        System.err.println(BOLD + RED + "✘  " + title + RESET);
        errors.forEach(e -> System.err.println("   • " + e));
        System.err.println(LINE);
    }

    public void notifyError(String title, String singleError) {
        notifyError(title, List.of(singleError));
    }

    public void notifyInfo(String message) {
        System.out.println(YELLOW + "ℹ  " + message + RESET);
    }
}
