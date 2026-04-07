import { http, HttpResponse } from "msw";

export const mockUsers = [
    {
        id: 1,
        login: "root",
        email: "",
        freeText: "",
        locked: false,
        internal: false,
        roles: [1],
    },
    {
        id: 2,
        login: "operator",
        email: "",
        freeText: "",
        locked: false,
        internal: false,
        roles: [2],
    },
];

export const userHandlers = [
    http.get("/ws/admin/user", () => HttpResponse.json(mockUsers)),
    http.post("/ws/admin/user", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({ id: 99, ...body });
    }),
    http.put("/ws/admin/user/:id", async () => HttpResponse.json({})),
    http.delete(
        "/ws/admin/user/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
