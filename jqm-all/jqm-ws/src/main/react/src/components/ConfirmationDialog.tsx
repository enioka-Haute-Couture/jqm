import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Typography } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";

export const ConfirmationDialog: React.FC<{
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    title: string;
    message: string;
}> = ({ isOpen, onClose: closeDialog, onConfirm, title, message }) => {
    const { t } = useTranslation();
    return (
        <Dialog
            open={isOpen}
            onClose={closeDialog}>
            <DialogTitle>{title}</DialogTitle>
            <DialogContent>
                <DialogContentText>{message}</DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    {t("common.close")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    onClick={onConfirm}
                    style={{ margin: "8px" }}
                >
                    {t("common.confirm")}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
