import React from "react";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect } from "vitest";
import { renderWithProviders } from "../test-utils/renderWithProviders";
import { useNotificationService } from "./NotificationService";

const DisplayErrorFixture: React.FC<{ error: unknown }> = ({ error }) => {
    const { displayError } = useNotificationService();
    return (
        <button onClick={() => displayError(error)}>Trigger error</button>
    );
};

const DisplaySuccessFixture: React.FC<{ message: string }> = ({ message }) => {
    const { displaySuccess } = useNotificationService();
    return (
        <button onClick={() => displaySuccess(message)}>Trigger success</button>
    );
};

describe("NotificationService", () => {
    it("displays a translated error message using a userMessageKey with params", async () => {
        const error = {
            details: {
                userMessageKey: "itemNotFound",
                userMessageParams: { id: "42" },
            },
        };

        renderWithProviders(<DisplayErrorFixture error={error} />);
        await userEvent.click(screen.getByRole("button", { name: "Trigger error" }));

        await screen.findByText("Item with ID 42 not found");
    });

    it("falls back to userReadableMessage when the key has no translation", async () => {
        const error = {
            details: {
                userMessageKey: "unknownKey",
                userReadableMessage: "Something went wrong on the server",
            },
        };

        renderWithProviders(<DisplayErrorFixture error={error} />);
        await userEvent.click(screen.getByRole("button", { name: "Trigger error" }));

        await screen.findByText("Something went wrong on the server");
    });

    it("falls back to generic error when no details are provided", async () => {
        renderWithProviders(<DisplayErrorFixture error={{}} />);
        await userEvent.click(screen.getByRole("button", { name: "Trigger error" }));

        await screen.findByText("An error occurred, please contact support support@enioka.com for help");
    });

    it("displays a success notification", async () => {
        renderWithProviders(<DisplaySuccessFixture message="Operation completed successfully" />);
        await userEvent.click(screen.getByRole("button", { name: "Trigger success" }));

        await screen.findByText("Operation completed successfully");
    });
});
