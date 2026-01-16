import React, { useEffect } from "react";
import { Container, Link, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import APIService from "../utils/APIService";
import { setPageTitle } from "../utils/title";

const HomePage: React.FC = () => {
    const { t } = useTranslation();
    const [documentationLink, setDocumentationLink] = React.useState("http://jqm.readthedocs.org/en/master");

    useEffect(() => {
        APIService.get("/admin/version").then((data) => {
            setDocumentationLink(`http://jqm.readthedocs.org/en/jqm-all-${data.mavenVersion}/`);
        })
        setPageTitle("Home");
    }, [])

    return (
        <Container>
            <Typography variant="h5">
                {t("home.title")}
            </Typography>
            <Typography variant="body1">
                {t("home.helpText")}
            </Typography>
            <Typography variant="body1">
                {t("home.documentationText")} <Link href={documentationLink}>{t("home.documentationLink")}</Link> {t("home.documentationSuffix")}
            </Typography>
        </Container>
    );
};

export default HomePage;
