import React from "react";
import { Button, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";

export const DisplayLogsDialog: React.FC<{
    showDialog: boolean;
    closeDialog: () => void;
    logs: { nodeName: string; data: string } | undefined;
}> = ({ showDialog, closeDialog, logs }) => {
    const { t } = useTranslation();
    return (
        <Dialog
            open={showDialog}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth="xl"
        >
            <DialogTitle>{t("nodes.displayLogsDialog.title", { nodeName: logs?.nodeName })}</DialogTitle>
            <DialogContent>
                <Typography sx={{ fontFamily: 'Monospace', fontSize: "small", whiteSpace: "pre-wrap" }}>{logs?.data}</Typography>
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    {t("nodes.displayLogsDialog.close")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
