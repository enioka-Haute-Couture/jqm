import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import RunsPage from "./RunsPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";
import { mockJobInstances } from "../../test-utils/handlers/runs";

describe("RunsPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<RunsPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of job instances", async () => {
        renderWithProviders(<RunsPage />);

        await screen.findByText("SampleJob");
        expect(screen.getByText("AnotherJob")).toBeInTheDocument();
    });

    it("shows job state", async () => {
        renderWithProviders(<RunsPage />);

        await screen.findByText("ENDED");
        expect(screen.getByText("CRASHED")).toBeInTheDocument();
    });

    it("opens job instance details dialog when clicking show details button", async () => {
        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        const detailsButtons = screen.getAllByRole("button", { name: "Show details" });
        await userEvent.click(detailsButtons[0]);

        await screen.findByRole("dialog");
        await screen.findByText("Job details");
    });

    it("opens launch form dialog when clicking new launch button", async () => {
        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        await userEvent.click(screen.getByRole("button", { name: "New launch form" }));

        await screen.findByRole("dialog");
    });

    it("sends kill request for a running job in active view", async () => {
        const killSpy = vi.fn();
        server.use(
            http.post("/ws/client/ji/killed/:id", ({ params }) => {
                killSpy(params.id);
                return HttpResponse.json({});
            }),
            // Return a running job when querying live instances
            http.post("/ws/client/ji/query/", async ({ request }) => {
                const body = await request.json() as any;
                if (body.queryLiveInstances) {
                    return HttpResponse.json({
                        instances: [{ ...mockJobInstances[0], id: 44, state: "RUNNING" }],
                        resultSize: 1,
                    });
                }
                return HttpResponse.json({ instances: mockJobInstances, resultSize: 2 });
            })
        );

        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        await userEvent.click(screen.getByRole("button", { name: "Active" }));
        await screen.findByText("RUNNING");

        const killButton = screen.getByRole("button", { name: "Kill" });
        await userEvent.click(killButton);

        await waitFor(() => {
            expect(killSpy).toHaveBeenCalledWith("44");
        });
    });

    it("filters by application name via server-side query", async () => {
        const querySpy = vi.fn();
        server.use(
            http.post("/ws/client/ji/query/", async ({ request }) => {
                const body = await request.json() as any;
                querySpy(body);
                return HttpResponse.json({ instances: mockJobInstances, resultSize: 2 });
            })
        );

        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        const appFilter = screen.getByLabelText("Application");
        await userEvent.type(appFilter, "Sample");

        await waitFor(() => {
            const lastCall = querySpy.mock.calls.at(-1)?.[0];
            expect(lastCall?.applicationName).toEqual(["Sample"]);
        });
    });

    it("paginates via server-side query", async () => {
        const querySpy = vi.fn();
        server.use(
            http.post("/ws/client/ji/query/", async ({ request }) => {
                const body = await request.json() as any;
                querySpy(body);
                return HttpResponse.json({ instances: mockJobInstances, resultSize: 25 });
            })
        );

        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        const nextPage = await screen.findByRole("button", { name: /next page/i });
        await userEvent.click(nextPage);

        await waitFor(() => {
            expect(querySpy).toHaveBeenLastCalledWith(
                expect.objectContaining({ firstRow: 10 })
            );
        });
    });

    it("initial query includes sortby field", async () => {
        const querySpy = vi.fn();
        server.use(
            http.post("/ws/client/ji/query/", async ({ request }) => {
                const body = await request.json() as any;
                querySpy(body);
                return HttpResponse.json({ instances: mockJobInstances, resultSize: 2 });
            })
        );

        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        await waitFor(() => {
            expect(querySpy).toHaveBeenCalled();
            const firstCall = querySpy.mock.calls[0][0];
            expect(firstCall).toHaveProperty("sortby");
            expect(firstCall.sortby).toBeInstanceOf(Array);
            expect(firstCall.sortby[0]).toHaveProperty("col");
        });
    });

    it("opens the switch queue dialog in active view and changes the queue", async () => {
        const switchSpy = vi.fn();
        server.use(
            http.post("/ws/client/ji/query/", async ({ request }) => {
                const body = await request.json() as any;
                if (body.queryLiveInstances) {
                    return HttpResponse.json({
                        instances: [{ ...mockJobInstances[0], id: 44, state: "RUNNING" }],
                        resultSize: 1,
                    });
                }
                return HttpResponse.json({ instances: mockJobInstances, resultSize: 2 });
            }),
            http.post("/ws/client/q/:queueId/:jobId", ({ params }) => {
                switchSpy(params.queueId, params.jobId);
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        await userEvent.click(screen.getByRole("button", { name: "Active" }));
        await screen.findByText("RUNNING");

        await userEvent.click(screen.getByRole("button", { name: "Switch queue" }));
        await screen.findByText("Switch job 44 queue");

        await userEvent.click(screen.getByLabelText("Queue*"));
        await userEvent.click(await screen.findByRole("option", { name: "OtherQueue" }));

        await userEvent.click(screen.getByRole("button", { name: "Change" }));

        await waitFor(() => {
            expect(switchSpy).toHaveBeenCalledWith("2", "44");
        });
    });

    it("opens stdout log in the details dialog when clicking the Log stdout button", async () => {
        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        await userEvent.click(screen.getAllByRole("button", { name: "Log stdout" })[0]);

        await screen.findByRole("dialog");
        await screen.findByText("info logs");
    });

    it("opens stderr log in the details dialog when clicking the Log stderr button", async () => {
        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        await userEvent.click(screen.getAllByRole("button", { name: "Log stderr" })[0]);

        await screen.findByRole("dialog");
        await screen.findByText("error logs");
    });

    it("hides launch and relaunch action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({
                    login: "readonly",
                    permissions: ["job_instance:read", "queue:read"],
                })
            )
        );

        renderWithProviders(<RunsPage />);
        await screen.findByText("SampleJob");

        expect(screen.queryByRole("button", { name: "New launch form" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "Relaunch" })).not.toBeInTheDocument();
    });
});
