import React from "react";
import { Container, Typography } from "@mui/material";
import { useAuth } from "../utils/AuthService";

const AccessForbiddenPage: React.FC = () => {
    const { status } = useAuth();

    if (status === "LOGGING_IN") {
        return null;
    }

    return (
        <Container>
            <Typography variant="h6">
                You don't have permission to access this page.
            </Typography>
        </Container>
    );
};

export default AccessForbiddenPage;
