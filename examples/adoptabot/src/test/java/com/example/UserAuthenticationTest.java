package com.example;

import dev.kensa.*;
import dev.kensa.junit.KensaTest;
import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.state.CapturedInteractionBuilder;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static com.example.UserAuthenticationTest.SessionIdMatcher.startsWith;

public class UserAuthenticationTest implements KensaTest, WithHamcrest {

    private UserService userService;

    @RenderedValue
    private User user;

    @BeforeEach
    public void setup() {
        userService = new UserService();
    }

    @Test
    void authenticateValidUser() {
        given(aUserExists("user1", "password1"));

        whenever(userAttemptsToLogin());

        then(authenticationResult(), startsWith(user.username));
        then(userSession(), is(notNullValue()));
    }

    private Action<GivensContext> aUserExists(String username, String password) {
        return (context) -> {
            // In a real test, you might set up a test user in a database
            // For this example, we'll just populate a field
            user = new User(1, username, password);
        };
    }

    private Action<ActionContext> userAttemptsToLogin() {
        return (context) -> {
            // Capture the interaction
            // Using the CapturedInteractionBuilder causes this interaction to appear in the sequence diagram
            context.getInteractions().capture(CapturedInteractionBuilder.from(TestParty.User)
                    .to(TestParty.AuthenticationService)
                    .with(user.toString(), "Authentication Request"));

            // Perform the authentication
            UserSession session = userService.authenticate(user.getUsername(), user.getPassword());

            // Record the response
            context.getInteractions().capture(CapturedInteractionBuilder.from(TestParty.AuthenticationService)
                    .to(TestParty.User)
                    .with(session, "Authentication Response"));

            // Store the result for assertions
            context.getOutputs().put("session", session);
        };
    }

    private StateCollector<UserSession> authenticationResult() {
        return (context) -> context.getOutputs().get("session");
    }

    private StateCollector<UserSession> userSession() {
        return (context) -> context.getOutputs().get("session");
    }

    // Simple classes for the example

    private enum TestParty implements dev.kensa.state.Party {
        User,
        AuthenticationService;

        @Override
        public String asString() {
            return name();
        }
    }

    private static class User {
        private final Integer id;
        private final String username;
        private final String password;

        public User(Integer id, String username, String password) {
            this.id = id;
            this.username = username;
            this.password = password;
        }

        public Integer getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String toString() {
            return String.format("User[id=%d, username=%s]", id, username);
        }
    }

    private static class UserService {
        private UserSession userSession;

        public UserSession authenticate(String username, String password) {
            if (username != null && !username.isEmpty() &&
                    password != null && !password.isEmpty()) {
                return userSession = createSession(username);
            }
            return null;
        }

        public UserSession getUserSession(String username) {
            if (userSession.id.startsWith(username)) {
                return userSession;
            }
            return null;
        }

        private UserSession createSession(String username) {
            return new UserSession(username + "-" + System.currentTimeMillis());
        }
    }

    private static class UserSession {
        private final String id;

        public UserSession(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String toString() {
            return String.format("UserSession[id=%s]", id);
        }
    }

    public static class SessionIdMatcher extends TypeSafeMatcher<UserSession> {
        private final String expected;

        public SessionIdMatcher(String expected) {
            this.expected = expected;
        }

        @Override
        protected boolean matchesSafely(UserSession item) {
            return item.id.startsWith(expected);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a session id");
        }

        public static TypeSafeMatcher<UserSession> startsWith(String prefix) {
            return new SessionIdMatcher(prefix);
        }
    }
}