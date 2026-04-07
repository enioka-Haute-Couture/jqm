import { http, HttpResponse } from "msw";

export const mockJndiResources = [
    {
        id: 1,
        name: "mail/default",
        auth: "CONTAINER",
        type: "jakarta.mail.Session",
        factory: "com.enioka.jqm.providers.MailSessionFactory",
        description: "default parameters used to send e-mails",
        singleton: true,
        template: null,
        parameters: [
            {
                key: "smtpServerHost",
                value: "smtp.gmail.com",
            },
        ],
    },
];

export const jndiHandlers = [
    http.get("/ws/admin/jndi", () => HttpResponse.json(mockJndiResources)),
    http.post("/ws/admin/jndi", async ({ request }) => {
        const body = (await request.json()) as any;
        return HttpResponse.json({ id: 99, ...body });
    }),
    http.delete(
        "/ws/admin/jndi/:id",
        () => new HttpResponse(null, { status: 204 })
    ),
];
