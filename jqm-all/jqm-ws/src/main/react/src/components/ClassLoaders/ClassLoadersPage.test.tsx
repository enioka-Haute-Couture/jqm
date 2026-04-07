import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import ClassLoadersPage from "./ClassLoadersPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";

describe("ClassLoadersPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<ClassLoadersPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of class loaders", async () => {
        renderWithProviders(<ClassLoadersPage />);

        await screen.findByDisplayValue("DefaultClassLoader");
    });

    it("enters inline edit mode and saves a class loader", async () => {
        const user = userEvent.setup({ delay: null });
        const updateSpy = vi.fn();
        server.use(
            http.put("/ws/admin/cl/:id", async ({ request }) => {
                updateSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<ClassLoadersPage />);
        await screen.findByDisplayValue("DefaultClassLoader");

        const editButton = screen.getByRole("button", { name: "edit" });
        await user.click(editButton);

        // Open edit hidden classes dialog and add then remove a hidden class
        await user.click(screen.getByText("com.example.Hidden"));
        await screen.findByRole("heading", { name: "Edit hidden classes" });
        await user.type(screen.getByLabelText("Regexp*"), "com.example.New");
        await user.click(screen.getByRole("button", { name: "Add" }));
        const hiddenDeleteButtons = await screen.findAllByRole("button", { name: "delete" });
        await user.click(hiddenDeleteButtons[1]);
        await user.click(screen.getByRole("button", { name: "Validate" }));

        // Open edit allowed runners dialog and add then remove an allowed runner
        await user.click(screen.getByText("BasicRunner"));
        await screen.findByRole("heading", { name: "Edit allowed runners" });
        await user.type(screen.getByLabelText("Runner*"), "NewRunner");
        await user.click(screen.getByRole("button", { name: "Add" }));
        const runnersDeleteButtons = await screen.findAllByRole("button", { name: "delete" });
        await user.click(runnersDeleteButtons[1]);
        await user.click(screen.getByRole("button", { name: "Validate" }));

        const saveButton = await screen.findByRole("button", { name: "save" });
        await user.click(saveButton);

        await waitFor(() => {
            expect(updateSpy).toHaveBeenCalled();
        });
    }, 15000);

    it("creates a new class loader", async () => {
        const createSpy = vi.fn();
        server.use(
            http.post("/ws/admin/cl", async ({ request }) => {
                createSpy(await request.json());
                return HttpResponse.json({ id: 99, name: "NewLoader" });
            })
        );

        renderWithProviders(<ClassLoadersPage />);
        await screen.findByDisplayValue("DefaultClassLoader");

        await userEvent.click(screen.getByRole("button", { name: "add" }));
        await screen.findByRole("dialog");

        const inputs = screen.getAllByRole("textbox");
        await userEvent.type(inputs[0], "NewLoader");

        await userEvent.click(screen.getByRole("button", { name: "Create" }));

        await waitFor(() => {
            expect(createSpy).toHaveBeenCalledWith(
                expect.objectContaining({ name: "NewLoader" })
            );
        });
    });

    it("deletes a class loader", async () => {
        const deleteSpy = vi.fn();
        server.use(
            http.delete("/ws/admin/cl/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<ClassLoadersPage />);
        await screen.findByDisplayValue("DefaultClassLoader");

        const deleteButton = screen.getByRole("button", { name: "delete" });
        await userEvent.click(deleteButton);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith("1");
        });
    });

    it("hides action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "readonly", permissions: ["cl:read", "queue:read"] })
            )
        );

        renderWithProviders(<ClassLoadersPage />);
        await screen.findByDisplayValue("DefaultClassLoader");

        expect(screen.queryByRole("button", { name: "add" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
    });
});
