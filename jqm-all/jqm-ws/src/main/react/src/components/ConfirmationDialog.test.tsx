import React from "react";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi } from "vitest";
import { ConfirmationDialog } from "./ConfirmationDialog";
import { renderWithProviders } from "../test-utils/renderWithProviders";

describe("ConfirmationDialog", () => {
    it("renders title, message and action buttons", async () => {
        renderWithProviders(
            <ConfirmationDialog
                isOpen={true}
                onClose={vi.fn()}
                onConfirm={vi.fn()}
                title="Delete item?"
                message="This action cannot be undone."
            />
        );

        expect(screen.getByText("Delete item?")).toBeInTheDocument();
        expect(screen.getByText("This action cannot be undone.")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Close" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Confirm" })).toBeInTheDocument();
    });

    it("calls onConfirm when the Confirm button is clicked", async () => {
        const onConfirm = vi.fn();

        renderWithProviders(
            <ConfirmationDialog
                isOpen={true}
                onClose={vi.fn()}
                onConfirm={onConfirm}
                title="Delete item?"
                message="This action cannot be undone."
            />
        );

        await userEvent.click(screen.getByRole("button", { name: "Confirm" }));

        expect(onConfirm).toHaveBeenCalledTimes(1);
    });

    it("calls onClose when the Close button is clicked", async () => {
        const onClose = vi.fn();

        renderWithProviders(
            <ConfirmationDialog
                isOpen={true}
                onClose={onClose}
                onConfirm={vi.fn()}
                title="Delete item?"
                message="This action cannot be undone."
            />
        );

        await userEvent.click(screen.getByRole("button", { name: "Close" }));

        expect(onClose).toHaveBeenCalledTimes(1);
    });
});
