import { http, HttpResponse } from "msw";

export const mockJobDefinitions = [
    {
        id: 50,
        applicationName: "DemoFibo1",
        description:
            "Demonstrates the use of parameters and engine API (computes the Fibonacci suite).",
        enabled: true,
        queueId: 1,
        highlander: false,
        canBeRestarted: true,
        pathType: "FS",
        jarPath: "jqm-demo/jqm-test-pyl/jqm-test-pyl.jar",
        javaClassName: "pyl.StressFibo",
        application: "JQM",
        module: "Demos",
        keyword1: "EngineAPI",
        keyword2: "parameters",
        keyword3: null,
        parameters: [
            { key: "p1", value: "1" },
            { key: "p2", value: "2" },
        ],
        schedules: [],
        classLoaderId: null,
    },
];

export const jobDefinitionHandlers = [
    http.get("/ws/admin/jd", () => HttpResponse.json(mockJobDefinitions)),
    http.post("/ws/admin/jd", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({ id: 99, ...body });
    }),
    http.put("/ws/admin/jd/:id", async () => HttpResponse.json({})),
    http.delete(
        "/ws/admin/jd/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
