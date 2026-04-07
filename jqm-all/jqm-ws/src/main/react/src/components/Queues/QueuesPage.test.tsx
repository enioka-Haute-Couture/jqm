import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import QueuesPage from "./QueuesPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";

describe("QueuesPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<QueuesPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of queues", async () => {
        renderWithProviders(<QueuesPage />);

        await screen.findByDisplayValue("DefaultQueue");
        expect(screen.getByDisplayValue("OtherQueue")).toBeInTheDocument();
    });

    it("enters inline edit mode and saves a queue", async () => {
        const updateSpy = vi.fn();
        server.use(
            http.put("/ws/admin/q/:id", async ({ request }) => {
                updateSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<QueuesPage />);
        await screen.findByDisplayValue("DefaultQueue");

        const editButtons = screen.getAllByRole("button", { name: "edit" });
        await userEvent.click(editButtons[1]);

        const saveButton = await screen.findByRole("button", { name: "save" });
        await userEvent.click(saveButton);

        await waitFor(() => {
            expect(updateSpy).toHaveBeenCalled();
        });
    });

    it("hides action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "readonly", permissions: ["queue:read"] })
            )
        );

        renderWithProviders(<QueuesPage />);
        await screen.findByDisplayValue("DefaultQueue");

        expect(screen.queryByRole("button", { name: "add" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
    });

    it("creates a new queue", async () => {
        const createSpy = vi.fn();
        server.use(
            http.post("/ws/admin/q", async ({ request }) => {
                createSpy(await request.json());
                return HttpResponse.json({ id: 3, name: "NewQueue", defaultQueue: false });
            })
        );

        renderWithProviders(<QueuesPage />);
        await screen.findByDisplayValue("DefaultQueue");

        await userEvent.click(screen.getByRole("button", { name: "add" }));
        await screen.findByText("Create queue");

        await userEvent.type(screen.getByLabelText("Name*"), "NewQueue");
        await userEvent.click(screen.getByRole("button", { name: "Create" }));

        await waitFor(() => {
            expect(createSpy).toHaveBeenCalledWith(
                expect.objectContaining({ name: "NewQueue" })
            );
        });
    });

    it("deletes a queue", async () => {
        const deleteSpy = vi.fn();
        server.use(
            http.delete("/ws/admin/q/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<QueuesPage />);
        await screen.findByDisplayValue("DefaultQueue");

        const deleteButtons = screen.getAllByRole("button", { name: "delete" });
        await userEvent.click(deleteButtons[0]);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith("1");
        });
    });
});
