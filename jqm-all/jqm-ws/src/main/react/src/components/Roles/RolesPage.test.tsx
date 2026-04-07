import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import RolesPage from "./RolesPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";

describe("RolesPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<RolesPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of roles", async () => {
        renderWithProviders(<RolesPage />);

        await screen.findByDisplayValue("administrator");
        expect(screen.getByDisplayValue("client read only")).toBeInTheDocument();
    });

    it("enters inline edit mode and saves a role", async () => {
        const updateSpy = vi.fn();
        server.use(
            http.put("/ws/admin/role/:id", async ({ request }) => {
                updateSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<RolesPage />);
        await screen.findByDisplayValue("administrator");

        const editButtons = screen.getAllByRole("button", { name: "edit" });
        await userEvent.click(editButtons[0]);

        // Open the edit permissions dialog and add then remove a permission
        await userEvent.click(screen.getByText("*:*"));
        await screen.findByRole("heading", { name: "Edit permissions" });
        await userEvent.click(screen.getByRole("button", { name: "Add permission" }));
        const deleteButtons = await screen.findAllByRole("button", { name: "delete" });
        await userEvent.click(deleteButtons[deleteButtons.length - 1]);
        await userEvent.click(screen.getByRole("button", { name: "Validate" }));

        const saveButton = await screen.findByRole("button", { name: "save" });
        await userEvent.click(saveButton);

        await waitFor(() => {
            expect(updateSpy).toHaveBeenCalled();
        });
    });

    it("creates a new role", async () => {
        const createSpy = vi.fn();
        server.use(
            http.post("/ws/admin/role", async ({ request }) => {
                createSpy(await request.json());
                return HttpResponse.json({ id: 99, name: "auditor", permissions: [] });
            })
        );

        renderWithProviders(<RolesPage />);
        await screen.findByDisplayValue("administrator");

        await userEvent.click(screen.getByRole("button", { name: "add" }));
        await screen.findByRole("dialog");

        const inputs = screen.getAllByRole("textbox");
        await userEvent.type(inputs[0], "auditor");

        await userEvent.click(screen.getByRole("button", { name: "Create" }));

        await waitFor(() => {
            expect(createSpy).toHaveBeenCalledWith(
                expect.objectContaining({ name: "auditor" })
            );
        });
    });

    it("deletes a role", async () => {
        const deleteSpy = vi.fn();
        server.use(
            http.delete("/ws/admin/role/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<RolesPage />);
        await screen.findByDisplayValue("administrator");

        const deleteButtons = screen.getAllByRole("button", { name: "delete" });
        await userEvent.click(deleteButtons[0]);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith("1");
        });
    });

    it("hides action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "readonly", permissions: ["role:read"] })
            )
        );

        renderWithProviders(<RolesPage />);
        await screen.findByDisplayValue("administrator");

        expect(screen.queryByRole("button", { name: "add" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
    });
});
