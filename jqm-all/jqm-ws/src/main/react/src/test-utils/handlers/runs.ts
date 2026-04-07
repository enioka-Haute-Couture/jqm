import { http, HttpResponse } from "msw";

export const mockJobInstances = [
    {
        id: 42,
        applicationName: "SampleJob",
        state: "ENDED",
        queueName: "DefaultQueue",
        nodeName: "Node1",
        user: "admin",
        enqueueDate: "2024-01-15T10:00:00Z",
        beganRunningDate: "2024-01-15T10:00:01Z",
        endDate: "2024-01-15T10:00:05Z",
        progress: 100,
        position: 0,
        highlander: false,
        fromSchedule: false,
        messages: [],
        parameters: [],
        definitionKeyword1: "",
        definitionKeyword2: "",
        definitionKeyword3: "",
    },
    {
        id: 43,
        applicationName: "AnotherJob",
        state: "CRASHED",
        queueName: "OtherQueue",
        nodeName: "Node1",
        user: "operator",
        enqueueDate: "2024-01-15T11:00:00Z",
        beganRunningDate: "2024-01-15T11:00:01Z",
        endDate: "2024-01-15T11:00:10Z",
        progress: 50,
        position: 0,
        highlander: false,
        fromSchedule: false,
        messages: [],
        parameters: [],
        definitionKeyword1: "",
        definitionKeyword2: "",
        definitionKeyword3: "",
    },
];

export const runsHandlers = [
    http.post("/ws/client/ji/query/", () =>
        HttpResponse.json({ instances: mockJobInstances, resultSize: 2 })
    ),
    http.post("/ws/client/ji/query/ids", () => HttpResponse.json([42, 43])),
    http.post("/ws/client/ji/killed/:id", () => HttpResponse.json({})),
    http.post("/ws/client/ji/paused/:id", () => HttpResponse.json({})),
    http.post("/ws/client/ji/resumed/:id", () => HttpResponse.json({})),
    http.post("/ws/client/ji/restarted/:id", () => HttpResponse.json({})),
    http.get(
        "/ws/client/ji/:id/stdout",
        () =>
            new HttpResponse("info logs", {
                headers: { "Content-Type": "text/plain" },
            })
    ),
    http.get(
        "/ws/client/ji/:id/stderr",
        () =>
            new HttpResponse("error logs", {
                headers: { "Content-Type": "text/plain" },
            })
    ),
    http.get("/ws/client/ji/:id/files", () => HttpResponse.json([])),
    http.post("/ws/client/ji", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({
            id: 999,
            applicationName: body.applicationName,
        });
    }),
];
