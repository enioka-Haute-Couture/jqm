import React from "react";
import { Container, Typography } from "@material-ui/core";

const AccessForbiddenPage: React.FC = () => {
    return (
        <Container>
            <Typography variant="h6">
                You don't have permission to access this page.
            </Typography>
        </Container>
    );
};

export default AccessForbiddenPage;
