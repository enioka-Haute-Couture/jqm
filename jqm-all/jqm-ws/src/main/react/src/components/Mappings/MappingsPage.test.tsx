import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import MappingsPage from "./MappingsPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";

describe("MappingsPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<MappingsPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of mappings", async () => {
        renderWithProviders(<MappingsPage />);

        await screen.findByText("Node1");
        expect(screen.getByText("DefaultQueue")).toBeInTheDocument();
    });

    it("enters inline edit mode and saves a mapping", async () => {
        const updateSpy = vi.fn();
        server.use(
            http.put("/ws/admin/qmapping/:id", async ({ request }) => {
                updateSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<MappingsPage />);
        await screen.findByText("Node1");

        const editButton = screen.getByRole("button", { name: "edit" });
        await userEvent.click(editButton);

        const saveButton = await screen.findByRole("button", { name: "save" });
        await userEvent.click(saveButton);

        await waitFor(() => {
            expect(updateSpy).toHaveBeenCalled();
        });
    });

    it("creates a new mapping", async () => {
        const createSpy = vi.fn();
        server.use(
            http.post("/ws/admin/qmapping", async ({ request }) => {
                createSpy(await request.json());
                return HttpResponse.json({ id: 99 });
            })
        );

        renderWithProviders(<MappingsPage />);
        await screen.findByText("Node1");

        await userEvent.click(screen.getByRole("button", { name: "add" }));
        await screen.findByRole("dialog");

        await userEvent.type(screen.getByLabelText("Polling Interval (ms)*"), "1000");
        await userEvent.type(screen.getByLabelText("Max concurrent running instances*"), "5");

        await userEvent.click(screen.getByRole("button", { name: "Create" }));

        await waitFor(() => {
            expect(createSpy).toHaveBeenCalledWith(
                expect.objectContaining({ pollingInterval: 1000, nbThread: 5 })
            );
        });
    });

    it("deletes a mapping", async () => {
        const deleteSpy = vi.fn();
        server.use(
            http.delete("/ws/admin/qmapping/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<MappingsPage />);
        await screen.findByText("Node1");

        const deleteButton = screen.getByRole("button", { name: "delete" });
        await userEvent.click(deleteButton);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith("1");
        });
    });

    it("hides action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({
                    login: "readonly",
                    permissions: ["qmapping:read", "queue:read", "node:read"],
                })
            )
        );

        renderWithProviders(<MappingsPage />);
        await screen.findByText("Node1");

        expect(screen.queryByRole("button", { name: "add" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
    });
});
