import { http, HttpResponse } from "msw";

export const mockNodes = [
    {
        id: 1,
        name: "Node1",
        dns: "localhost",
        port: 1789,
        outputDirectory: "./outputfiles",
        jobRepoDirectory: "./jobs",
        tmpDirectory: "./tmp",
        rootLogLevel: "INFO",
        jmxRegistryPort: 1790,
        jmxServerPort: 1791,
        stop: false,
        enabled: true,
        loadApiAdmin: true,
        loadApiClient: true,
        loadApiSimple: true,
        template: false,
        lastSeenAlive: new Date().toISOString(),
    },
];

export const nodeHandlers = [
    http.get("/ws/admin/node", () => HttpResponse.json(mockNodes)),
    http.get(
        "/ws/admin/node/:name/log",
        () =>
            new HttpResponse(
                "07/04.13:33:42.192|INFO |INTERNAL_POLLER;polling orders;         |        c.e.j.e.InternalPoller|Start of the internal poller",
                {
                    headers: { "Content-Type": "text/plain" },
                }
            )
    ),
    http.put("/ws/admin/node", async () => HttpResponse.json({})),
    http.delete(
        "/ws/admin/node/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
