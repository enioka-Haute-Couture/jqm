import { http, HttpResponse } from "msw";

export const mockClassLoaders = [
    {
        id: 1,
        name: "DefaultClassLoader",
        childFirst: false,
        hiddenClasses: "com.example.Hidden",
        tracingEnabled: false,
        persistent: false,
        allowedRunners: "BasicRunner",
    },
];

export const classLoaderHandlers = [
    http.get("/ws/admin/cl", () => HttpResponse.json(mockClassLoaders)),
    http.post("/ws/admin/cl", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({ id: 99, ...body });
    }),
    http.put("/ws/admin/cl/:id", async () => HttpResponse.json({})),
    http.delete(
        "/ws/admin/cl/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
