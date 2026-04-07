import React from "react";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, it, expect, vi } from "vitest";
import ClusterwideParametersPage from "./ClusterwideParametersPage";
import { renderWithProviders } from "../../test-utils/renderWithProviders";
import { server } from "../../test-utils/server";

describe("ClusterwideParametersPage", () => {
    it("shows access forbidden page for user without read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "guest", permissions: [] })
            )
        );

        renderWithProviders(<ClusterwideParametersPage />);

        await screen.findByText("You don't have permission to access this page.");
    });

    it("renders the list of parameters", async () => {
        renderWithProviders(<ClusterwideParametersPage />);

        await screen.findByDisplayValue("logFilePerLaunch");
        expect(screen.getByDisplayValue("true")).toBeInTheDocument();
    });

    it("creates a new parameter", async () => {
        const createSpy = vi.fn();
        server.use(
            http.post("/ws/admin/prm", async ({ request }) => {
                createSpy(await request.json());
                return HttpResponse.json({ id: 99, key: "newParam", value: "test" });
            })
        );

        renderWithProviders(<ClusterwideParametersPage />);
        await screen.findByDisplayValue("logFilePerLaunch");

        await userEvent.click(screen.getByRole("button", { name: "add" }));
        await screen.findByRole("dialog");

        const inputs = screen.getAllByRole("textbox");
        await userEvent.type(inputs[0], "newParam");
        await userEvent.type(inputs[1], "test");

        await userEvent.click(screen.getByRole("button", { name: "Create" }));

        await waitFor(() => {
            expect(createSpy).toHaveBeenCalledWith(
                expect.objectContaining({ key: "newParam" })
            );
        });
    });

    it("deletes a parameter", async () => {
        const deleteSpy = vi.fn();
        server.use(
            http.delete("/ws/admin/prm/:id", ({ params }) => {
                deleteSpy(params.id);
                return new HttpResponse(null, { status: 204 });
            })
        );

        renderWithProviders(<ClusterwideParametersPage />);
        await screen.findByDisplayValue("logFilePerLaunch");

        const deleteButtons = screen.getAllByRole("button", { name: "delete" });
        await userEvent.click(deleteButtons[0]);

        await waitFor(() => {
            expect(deleteSpy).toHaveBeenCalledWith("1");
        });
    });

    it("enters inline edit mode and saves a parameter", async () => {
        const updateSpy = vi.fn();
        server.use(
            http.put("/ws/admin/prm/:id", async ({ request }) => {
                updateSpy(await request.json());
                return HttpResponse.json({});
            })
        );

        renderWithProviders(<ClusterwideParametersPage />);
        await screen.findByDisplayValue("logFilePerLaunch");

        const editButtons = screen.getAllByRole("button", { name: "edit" });
        await userEvent.click(editButtons[0]);

        const saveButton = await screen.findByRole("button", { name: "save" });
        await userEvent.click(saveButton);

        await waitFor(() => {
            expect(updateSpy).toHaveBeenCalled();
        });
    });

    it("hides action buttons for user with only read permission", async () => {
        server.use(
            http.get("/ws/admin/me", () =>
                HttpResponse.json({ login: "readonly", permissions: ["prm:read"] })
            )
        );

        renderWithProviders(<ClusterwideParametersPage />);
        await screen.findByDisplayValue("logFilePerLaunch");

        expect(screen.queryByRole("button", { name: "add" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "delete" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "edit" })).not.toBeInTheDocument();
    });
});
