package com.workshop.net;

public final class NetResponse {
    public final boolean ok;
    public final String message;
    public final String payload;

    private NetResponse(boolean ok, String message, String payload) {
        this.ok = ok;
        this.message = message;
        this.payload = payload;
    }

    public static NetResponse ok(String payload) {
        return new NetResponse(true, "OK", payload);
    }

    public static NetResponse fail(String message) {
        return new NetResponse(false, message, null);
    }

    public static NetResponse offline() {
        return new NetResponse(false, "OFFLINE", null);
    }

    public boolean isOffline() {
        return "OFFLINE".equals(message);
    }

    public static NetResponse parse(String line) {
        String[] parts = UserSnapshot.split(line);
        if (parts.length == 0) {
            return fail("Empty server response.");
        }
        String rest = parts.length > 1 ? UserSnapshot.joinFrom(parts, 1) : "";
        if ("OK".equals(parts[0])) {
            return ok(rest);
        }
        return fail(rest.isEmpty() ? "Server error." : rest);
    }
}
