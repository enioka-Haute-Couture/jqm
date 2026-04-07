import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import { NodesPage } from "./NodesPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";
import { mockNodes } from "../../test-utils/handlers/nodes";

describe("NodesPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<NodesPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of nodes", async () => {
        renderWithProviders(<NodesPage />);

        await screen.findByDisplayValue("Node1");
    });

    it("enters inline edit mode and saves a node", async () => {
        const updateSpy = vi.fn();
        server.use(
            http.put("/ws/admin/node", async ({ request }) => {
                updateSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<NodesPage />);
        await screen.findByDisplayValue("Node1");

        const editButton = screen.getByRole("button", { name: "edit" });
        await userEvent.click(editButton);

        const saveButton = await screen.findByRole("button", { name: "save" });
        await userEvent.click(saveButton);

        await waitFor(() => {
            expect(updateSpy).toHaveBeenCalled();
        });
    });

    it("hides action buttons for user with only read permission", async () => {
        const staleLastSeenAlive = new Date(Date.now() - 15 * 60 * 1000).toISOString();

        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({
                    login: "readonly",
                    permissions: ["node:read"],
                })
            ),
            http.get("/ws/admin/node", () =>
                HttpResponse.json([{ ...mockNodes[0], lastSeenAlive: staleLastSeenAlive }])
            )
        );

        renderWithProviders(<NodesPage />);
        await screen.findByDisplayValue("Node1");

        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
    });

    it("deletes a node whose lastSeenAlive is more than 10 minutes ago", async () => {
        const staleLastSeenAlive = new Date(Date.now() - 15 * 60 * 1000).toISOString();
        const deleteSpy = vi.fn();

        server.use(
            http.get("/ws/admin/node", () =>
                HttpResponse.json([{ ...mockNodes[0], lastSeenAlive: staleLastSeenAlive }])
            ),
            http.delete("/ws/admin/node/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<NodesPage />);
        await screen.findByDisplayValue("Node1");

        const deleteButton = screen.getByRole("button", { name: "delete" });
        await userEvent.click(deleteButton);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith(String(mockNodes[0].id));
        });
    });

    it("opens the node log dialog when clicking view node logs", async () => {
        renderWithProviders(<NodesPage />);
        await screen.findByDisplayValue("Node1");

        const viewLogsButton = screen.getByRole("button", { name: "View node logs" });
        await userEvent.click(viewLogsButton);

        await screen.findByText(/Latest logs for node Node1/);
    });
});
