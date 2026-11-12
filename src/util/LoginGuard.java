package util;

import constants.AppConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Blocks logins for a while after too many wrong passwords.
 * Each account (the admin, or a student username) is counted separately.
 * The counters live in memory, so they reset when the program is closed.
 */
public class LoginGuard {

    private static final Map<String, Integer> failures = new HashMap<>();
    private static final Map<String, Long> lockedUntil = new HashMap<>();

    private static String clean(String key) {
        return key == null ? "" : key.trim().toLowerCase();
    }

    /** Milliseconds left on the lock, or 0 if the account is not locked. */
    public static synchronized long millisLeft(String key) {
        key = clean(key);
        Long until = lockedUntil.get(key);
        if (until == null) return 0;

        long left = until - System.currentTimeMillis();
        if (left <= 0) { // lock finished: start fresh
            lockedUntil.remove(key);
            failures.remove(key);
            return 0;
        }
        return left;
    }

    /** Call after a wrong password. Returns true if this attempt locked the account. */
    public static synchronized boolean recordFailure(String key) {
        key = clean(key);
        if (millisLeft(key) > 0) return true;

        int count = failures.getOrDefault(key, 0) + 1;
        if (count >= AppConstants.MAX_LOGIN_ATTEMPTS) {
            lockedUntil.put(key, System.currentTimeMillis() + AppConstants.LOCKOUT_SECONDS * 1000L);
            failures.remove(key);
            return true;
        }
        failures.put(key, count);
        return false;
    }

    public static synchronized int attemptsLeft(String key) {
        return AppConstants.MAX_LOGIN_ATTEMPTS - failures.getOrDefault(clean(key), 0);
    }

    /** Call after a correct login. */
    public static synchronized void reset(String key) {
        key = clean(key);
        failures.remove(key);
        lockedUntil.remove(key);
    }
}