import { http, HttpResponse } from "msw";

export const mockRoles = [
    {
        id: 1,
        name: "administrator",
        description: "all permissions without exception",
        permissions: ["*:*"],
    },
    {
        id: 2,
        name: "client read only",
        description: "can query job instances and get their files",
        permissions: [
            "job_instance:read",
            "queue:read",
            "logs:read",
            "files:read",
        ],
    },
];

export const roleHandlers = [
    http.get("/ws/admin/role", () => HttpResponse.json(mockRoles)),
    http.post("/ws/admin/role", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({ id: 99, ...body });
    }),
    http.put("/ws/admin/role/:id", async () => HttpResponse.json({})),
    http.delete(
        "/ws/admin/role/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
