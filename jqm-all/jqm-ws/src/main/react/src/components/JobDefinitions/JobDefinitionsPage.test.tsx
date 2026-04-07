import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import { JobDefinitionsPage } from "./JobDefinitionsPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";
import { mockJobDefinitions } from "../../test-utils/handlers/jobDefinitions";

describe("JobDefinitionsPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<JobDefinitionsPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of job definitions", async () => {
        renderWithProviders(<JobDefinitionsPage />);

        await screen.findByDisplayValue("DemoFibo1");
    });

    it("enters inline edit mode and saves a job definition", async () => {
        const user = userEvent.setup({ delay: null });
        const updateSpy = vi.fn();
        server.use(
            http.put("/ws/admin/jd/:id", async ({ request }) => {
                updateSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<JobDefinitionsPage />);
        await screen.findByDisplayValue("DemoFibo1");

        await user.click(screen.getByRole("button", { name: "edit" }));

        // Open Edit specific properties dialog
        await user.click(screen.getByText(/Path to the jar file:/, { selector: "p" }));
        await screen.findByRole("heading", { name: "Edit java specific properties" });
        await user.click(screen.getByRole("button", { name: "Validate" }));

        // Open Edit tags dialog
        await user.click(screen.getByText("Application: JQM"));
        await screen.findByRole("heading", { name: "Edit tags" });
        await user.click(screen.getByRole("button", { name: "Validate" }));

        // Open Edit parameters dialog and add then remove a parameter
        await user.click(screen.getByText("p1: 1, p2: 2", { selector: "p" }));
        await screen.findByRole("heading", { name: "Edit parameters" });
        await user.type(screen.getByLabelText("Key*"), "testKey");
        await user.click(screen.getByRole("button", { name: "Add parameter" }));
        const paramDeleteButtons = await screen.findAllByRole("button", { name: "delete" });
        await user.click(paramDeleteButtons[paramDeleteButtons.length - 1]);
        await user.click(screen.getByRole("button", { name: "Validate" }));

        // Open Edit schedules dialog, add then remove a schedule
        await user.click(screen.getByTestId("ScheduleIcon"));
        await screen.findByRole("heading", { name: "Edit schedules" });
        await user.type(screen.getByLabelText("Cron expression*"), "0 0 * * *");
        await user.click(screen.getByRole("button", { name: "Add schedule" }));
        const scheduleDeleteButtons = await screen.findAllByRole("button", { name: "delete" });
        await user.click(scheduleDeleteButtons[scheduleDeleteButtons.length - 1]);
        await user.click(screen.getByRole("button", { name: "Validate" }));

        const saveButton = await screen.findByRole("button", { name: "save" });
        await user.click(saveButton);

        await waitFor(() => {
            expect(updateSpy).toHaveBeenCalled();
        });
    }, 15000);

    it("creates a new job definition", async () => {
        const createSpy = vi.fn();
        server.use(
            http.post("/ws/admin/jd", async ({ request }) => {
                createSpy(await request.json());
                return HttpResponse.json({ id: 99, applicationName: "NewFiboJob" });
            })
        );

        renderWithProviders(<JobDefinitionsPage />);
        await screen.findByDisplayValue("DemoFibo1");

        await userEvent.click(screen.getByRole("button", { name: "add" }));
        await screen.findByRole("dialog");

        await userEvent.type(screen.getByLabelText("Name*"), "DemoEcho");
        await userEvent.type(screen.getByLabelText("Path to the jar file*"), "jqm-demo/jqm-test-pyl-nodep/jqm-test-pyl-nodep.jar");
        await userEvent.type(screen.getByLabelText("Class to launch*"), "pyl.PckMain");
        await userEvent.click(screen.getByRole("button", { name: "Create" }));

        await waitFor(() => {
            expect(createSpy).toHaveBeenCalledWith(
                expect.objectContaining({ applicationName: "DemoEcho" })
            );
        });
    });

    it("deletes a job definition", async () => {
        const deleteSpy = vi.fn();
        server.use(
            http.delete("/ws/admin/jd/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<JobDefinitionsPage />);
        await screen.findByDisplayValue("DemoFibo1");

        const deleteButton = screen.getByRole("button", { name: "delete" });
        await userEvent.click(deleteButton);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith("50");
        });
    });

    it("opens the view schedules dialog", async () => {
        server.use(
            http.get("/ws/admin/jd", () =>
                HttpResponse.json([
                    {
                        ...mockJobDefinitions[0],
                        schedules: [
                            { cronExpression: "0 6 * * *", parameters: [] },
                        ],
                    },
                ])
            )
        );

        renderWithProviders(<JobDefinitionsPage />);
        await screen.findByDisplayValue("DemoFibo1");

        await userEvent.click(screen.getByTestId("ScheduleIcon"));

        await screen.findByRole("heading", { name: "View schedules" });
        expect(screen.getByText("0 6 * * *")).toBeInTheDocument();
    });

    it("hides action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({
                    login: "readonly",
                    permissions: ["jd:read", "queue:read"],
                })
            )
        );

        renderWithProviders(<JobDefinitionsPage />);
        await screen.findByDisplayValue("DemoFibo1");

        expect(screen.queryByRole("button", { name: "add" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
    });
});
