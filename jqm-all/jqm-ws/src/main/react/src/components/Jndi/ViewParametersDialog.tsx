import React, { useRef } from "react";
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
} from "@mui/material";
import { useTranslation } from "react-i18next";
import { JndiResource } from "./JndiResource";
import { JndiParametersTable } from "./JndiParametersTable";

export const ViewParametersDialog: React.FC<{
    closeDialog: () => void;
    resource: JndiResource | null;
}> = ({ closeDialog, resource }) => {
    const { t } = useTranslation();

    if (!resource) {
        return null;
    }

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">{t("jndi.viewParametersDialog.title", { resourceName: resource.name })}</DialogTitle>
            <DialogContent>
                <JndiParametersTable
                    parameters={resource.parameters}
                />
            </DialogContent>
            <DialogActions>

                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={closeDialog}
                    color="primary"
                >
                    {t("common.close")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
