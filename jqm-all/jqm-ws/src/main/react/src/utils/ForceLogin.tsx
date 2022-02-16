import React from "react";
import { Redirect, useLocation } from "react-router-dom";
import { useAuth } from "./AuthService";

export const ForceLogin = () => {
    const { status } = useAuth();
    const { pathname } = useLocation();
    return status === "LOGGED_OUT" ? (
        <Redirect to={{ pathname: "/auth/login.html", state: { next: pathname } }} />
    ) : null;
};
