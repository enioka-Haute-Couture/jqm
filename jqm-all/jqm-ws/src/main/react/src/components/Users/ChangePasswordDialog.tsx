import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    TextField,
} from "@mui/material";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";

export const ChangePasswordDialog: React.FC<{
    closeDialog: () => void;
    changePassword: (password: string) => Promise<void>;
}> = ({ closeDialog, changePassword }) => {
    const { t } = useTranslation();
    const [password, setPassword] = useState<string>("");

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
        >
            <DialogTitle id="form-dialog-title">{t("users.changePasswordDialog.title")}</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    {t("users.changePasswordDialog.description")}
                </DialogContentText>
                <TextField
                    autoFocus
                    margin="dense"
                    label={t("users.changePasswordDialog.passwordLabel")}
                    type="password"
                    fullWidth
                    value={password}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setPassword(event.target.value);
                    }}
                    variant="standard"
                />
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={closeDialog}
                >
                    {t("common.cancel")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={async () => {
                        await changePassword(password);
                        closeDialog();
                    }}
                    color="primary"
                >
                    {t("users.changePasswordDialog.save")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
