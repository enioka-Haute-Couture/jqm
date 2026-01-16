import React from "react";
import { Container, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import { useAuth } from "../utils/AuthService";

const AccessForbiddenPage: React.FC = () => {
    const { t } = useTranslation();
    const { status } = useAuth();

    if (status === "LOGGING_IN") {
        return null;
    }

    return (
        <Container>
            <Typography variant="h6">
                {t("auth.accessForbidden")}
            </Typography>
        </Container>
    );
};

export default AccessForbiddenPage;
