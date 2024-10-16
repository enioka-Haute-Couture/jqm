import React, { useEffect } from "react";
import { Container, Link, Typography } from "@mui/material";
import APIService from "../utils/APIService";

const HomePage: React.FC = () => {
    const [documentationLink, setDocumentationLink] = React.useState("http://jqm.readthedocs.org/en/master");

    useEffect(() => {
        APIService.get("/admin/version").then((data) => {
            setDocumentationLink(`http://jqm.readthedocs.org/en/jqm-all-${data.mavenVersion}/`);
        })
    }, [])

    return (
        <Container>
            <Typography variant="h5">
                Welcome to the JQM administration web console
            </Typography>
            <Typography variant="body1">
                On each tab, click the question mark icon for contextual help.
            </Typography>
            <Typography variant="body1">
                Further reference can be found in the <Link href={documentationLink}>full online documentation</Link> for the development branch .
            </Typography>
        </Container>
    );
};

export default HomePage;
