package be.howest.ti.mars.logic.domain;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private final Map<String, Session> sessions = new HashMap<>();

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }
}
