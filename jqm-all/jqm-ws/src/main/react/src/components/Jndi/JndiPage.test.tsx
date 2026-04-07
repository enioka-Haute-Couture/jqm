import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import { JndiPage } from "./JndiPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";

describe("JndiPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<JndiPage />);

        await screen.findByText("You don't have permission to access this page.");
    });


    it("renders the list of JNDI resources", async () => {
        renderWithProviders(<JndiPage />);

        await screen.findByDisplayValue("mail/default");
    });

    it("enters inline edit mode and saves a JNDI resource", async () => {
        const user = userEvent.setup({ delay: null });
        const saveSpy = vi.fn();
        server.use(
            http.post("/ws/admin/jndi", async ({ request }) => {
                saveSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<JndiPage />);
        await screen.findByDisplayValue("mail/default");

        const editButton = screen.getByRole("button", { name: "edit" });
        await user.click(editButton);

        // Open edit parameters dialog and add then remove a parameter
        const parametersBadge = screen.getByText("1");
        await user.click(parametersBadge);
        await screen.findByText("Edit Parameters");
        await user.type(screen.getByLabelText("Name*"), "testKey");
        await user.type(screen.getByLabelText("Value*"), "testValue");
        await user.click(screen.getByRole("button", { name: "Add parameter" }));
        const deleteButtons = await screen.findAllByRole("button", { name: "delete" });
        await user.click(deleteButtons[1]);

        await user.click(screen.getByRole("button", { name: "Validate" }));

        const saveButton = await screen.findByRole("button", { name: "save" });
        await user.click(saveButton);

        await waitFor(() => {
            expect(saveSpy).toHaveBeenCalled();
        });
    }, 15000);

    it("creates a new JNDI resource", async () => {
        const createSpy = vi.fn();
        server.use(
            http.post("/ws/admin/jndi", async ({ request }) => {
                createSpy(await request.json());
                return HttpResponse.json({ id: 99 });
            })
        );

        renderWithProviders(<JndiPage />);
        await screen.findByDisplayValue("mail/default");

        await userEvent.click(screen.getByRole("button", { name: "add" }));
        await userEvent.click(screen.getByRole("option", { name: "Oracle Pool" }));

        await waitFor(() => {
            expect(createSpy).toHaveBeenCalledWith(
                expect.objectContaining({ type: "javax.sql.DataSource" })
            );
        });
    });

    it("deletes a JNDI resource", async () => {
        const deleteSpy = vi.fn();
        server.use(
            http.delete("/ws/admin/jndi/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<JndiPage />);
        await screen.findByDisplayValue("mail/default");

        const deleteButton = screen.getByRole("button", { name: "delete" });
        await userEvent.click(deleteButton);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith("1");
        });
    });

    it("opens the view parameters dialog", async () => {
        renderWithProviders(<JndiPage />);
        await screen.findByDisplayValue("mail/default");

        await userEvent.click(screen.getByTestId("SettingsIcon"));

        await screen.findByRole("heading", { name: "Parameters of mail/default" });
        expect(screen.getByText("smtpServerHost")).toBeInTheDocument();
        expect(screen.getByText("smtp.gmail.com")).toBeInTheDocument();
    });

    it("hides action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "readonly", permissions: ["jndi:read"] })
            )
        );

        renderWithProviders(<JndiPage />);
        await screen.findByDisplayValue("mail/default");

        expect(screen.queryByRole("button", { name: "add" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
    });
});
