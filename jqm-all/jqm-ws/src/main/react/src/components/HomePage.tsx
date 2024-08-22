import React from "react";
import { Container, Link, Typography } from "@mui/material";

const HomePage: React.FC = () => {
    return (
        <Container>
            <Typography variant="h5">
                Welcome to the JQM administration web console
            </Typography>
            <Typography variant="body1">
                On each tab, click the question mark icon for contextual help.
            </Typography>
            <Typography variant="body1">
                Further reference can be found in the <Link href="http://jqm.readthedocs.org/en/master">full online documentation</Link> for the development branch .
            </Typography>
        </Container>
    );
};

export default HomePage;
