import { http, HttpResponse } from "msw";

export const statsHandlers = [
    http.get("/ws/admin/stats/usage", () =>
        HttpResponse.json({
            activeNodes: 1,
            queues: 1,
            pausedJobs: 0,
            submittedJobs: 0,
            runningJobs: 0,
        })
    ),
    http.get("/ws/admin/stats/history", () => HttpResponse.json([])),
];
