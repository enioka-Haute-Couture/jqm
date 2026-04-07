import React, { ReactElement } from "react";
import { render, RenderOptions } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { SnackbarProvider } from "notistack";
import { createTheme, StyledEngineProvider, ThemeProvider } from "@mui/material/styles";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFnsV3";
import { AuthProvider } from "../utils/AuthService";
import { RunsPaginationProvider } from "../utils/RunsPaginationProvider";
import "../i18n";

const theme = createTheme();

function AllProviders({ children }: { children: React.ReactNode }) {
    return (
        <MemoryRouter>
            <StyledEngineProvider injectFirst>
                <ThemeProvider theme={theme}>
                    <LocalizationProvider dateAdapter={AdapterDateFns}>
                        <AuthProvider>
                            <SnackbarProvider maxSnack={4}>
                                <RunsPaginationProvider>
                                    {children}
                                </RunsPaginationProvider>
                            </SnackbarProvider>
                        </AuthProvider>
                    </LocalizationProvider>
                </ThemeProvider>
            </StyledEngineProvider>
        </MemoryRouter>
    );
}

export function renderWithProviders(
    ui: ReactElement,
    options?: Omit<RenderOptions, "wrapper">
) {
    return render(ui, { wrapper: AllProviders, ...options });
}
