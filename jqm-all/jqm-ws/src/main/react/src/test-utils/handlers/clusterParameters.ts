import { http, HttpResponse } from "msw";

export const mockParameters = [
    { id: 1, key: "logFilePerLaunch", value: "true" },
    { id: 2, key: "internalPollingPeriodMs", value: "60000" },
];

export const clusterParameterHandlers = [
    http.get("/ws/admin/prm", () => HttpResponse.json(mockParameters)),
    http.post("/ws/admin/prm", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({ id: 99, ...body });
    }),
    http.put("/ws/admin/prm/:id", async () => HttpResponse.json({})),
    http.delete(
        "/ws/admin/prm/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
