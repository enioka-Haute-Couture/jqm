import { http, HttpResponse } from "msw";

export const mockQueues = [
    {
        id: 1,
        name: "DefaultQueue",
        defaultQueue: true,
        description: "default queue",
    },
    {
        id: 2,
        name: "OtherQueue",
        defaultQueue: false,
    },
];

export const queueHandlers = [
    http.get("/ws/admin/q", () => HttpResponse.json(mockQueues)),
    http.post("/ws/admin/q", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({ id: 99, ...body });
    }),
    http.put("/ws/admin/q/:id", async () => HttpResponse.json({})),
    http.delete(
        "/ws/admin/q/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
