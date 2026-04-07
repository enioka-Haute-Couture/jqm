import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import UsersPage from "./UsersPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";

describe("UsersPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<UsersPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of users", async () => {
        renderWithProviders(<UsersPage />);

        await screen.findByDisplayValue("root");
        expect(screen.getByDisplayValue("operator")).toBeInTheDocument();
    });

    it("enters inline edit mode and saves a user", async () => {
        const updateSpy = vi.fn();
        server.use(
            http.put("/ws/admin/user/:id", async ({ request }) => {
                updateSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<UsersPage />);
        await screen.findByDisplayValue("root");

        const editButtons = screen.getAllByRole("button", { name: "edit" });
        await userEvent.click(editButtons[0]);

        const saveButton = await screen.findByRole("button", { name: "save" });
        await userEvent.click(saveButton);

        await waitFor(() => {
            expect(updateSpy).toHaveBeenCalledWith(
                expect.objectContaining({ login: "root" })
            );
        });
    });

    it("changes password of a user", async () => {
        const passwordSpy = vi.fn();
        server.use(
            http.put("/ws/admin/user/:id", async ({ request }) => {
                const body = (await request.json()) as any;
                passwordSpy(body);
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<UsersPage />);
        await screen.findByDisplayValue("root");

        await userEvent.click(screen.getAllByRole("button", { name: /change password/i })[0]);
        await screen.findByRole("dialog");

        await userEvent.type(screen.getByLabelText("Password"), "s3cr3t!");
        await userEvent.click(screen.getByRole("button", { name: "Save" }));

        await waitFor(() => {
            expect(passwordSpy).toHaveBeenCalledWith(
                expect.objectContaining({ newPassword: "s3cr3t!" })
            );
        });
    });

    it("provides a certificate download link for a user", async () => {
        renderWithProviders(<UsersPage />);
        await screen.findByDisplayValue("root");

        const links = screen.getAllByRole("link", { name: "Download certificate" });
        expect(links[0]).toHaveAttribute("href", "/ws/admin/user/1/certificate");
    });

    it("creates a new user", async () => {
        const createSpy = vi.fn();
        server.use(
            http.post("/ws/admin/user", async ({ request }) => {
                createSpy(await request.json());
                return HttpResponse.json({ id: 99, login: "newuser", locked: false, roles: [] });
            })
        );

        renderWithProviders(<UsersPage />);
        await screen.findByDisplayValue("root");

        await userEvent.click(screen.getByRole("button", { name: "add" }));
        await screen.findByRole("dialog");

        await userEvent.type(screen.getByLabelText("Login*"), "newuser");

        await userEvent.click(screen.getByRole("button", { name: "Create" }));

        await waitFor(() => {
            expect(createSpy).toHaveBeenCalledWith(
                expect.objectContaining({ login: "newuser" })
            );
        });
    });

    it("deletes a user", async () => {
        const deleteSpy = vi.fn();
        server.use(
            http.delete("/ws/admin/user/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<UsersPage />);
        await screen.findByDisplayValue("root");

        const deleteButtons = screen.getAllByRole("button", { name: "delete" });
        await userEvent.click(deleteButtons[0]);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith("1");
        });
    });

    it("hides action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({
                    login: "readonly",
                    permissions: ["user:read", "role:read"],
                })
            )
        );

        renderWithProviders(<UsersPage />);
        await screen.findByDisplayValue("root");

        expect(screen.queryByRole("button", { name: "add" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: /change password/i })).not.toBeInTheDocument();
        expect(screen.queryByRole("link", { name: "Download certificate" })).not.toBeInTheDocument();
    });
});
