import { http, HttpResponse } from "msw";

export const mockMappings = [
    {
        id: 1,
        nodeId: 1,
        nodeName: "Node1",
        queueId: 1,
        queueName: "DefaultQueue",
        pollingInterval: 1000,
        nbThread: 5,
        enabled: true,
    },
];

export const mappingHandlers = [
    http.get("/ws/admin/qmapping", () => HttpResponse.json(mockMappings)),
    http.post("/ws/admin/qmapping", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({ id: 99, ...body });
    }),
    http.put("/ws/admin/qmapping/:id", async () => HttpResponse.json({})),
    http.delete(
        "/ws/admin/qmapping/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
