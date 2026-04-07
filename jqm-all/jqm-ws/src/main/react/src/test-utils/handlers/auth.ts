import { http, HttpResponse } from "msw";

export const authHandlers = [
    http.get("/ws/admin/me", () =>
        HttpResponse.json({ login: "admin", permissions: ["*:*"] })
    ),
    http.get("/ws/admin/version", () =>
        HttpResponse.json({ mavenVersion: "3.3.2" })
    ),
];
