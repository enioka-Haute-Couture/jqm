import React, { useState } from "react";
import { Button, Theme } from "@mui/material";
import { makeStyles } from "@mui/styles";
import { useTranslation } from "react-i18next";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField/TextField";
import { Parameter } from "./Parameter";

const useStyles = makeStyles((theme: Theme) => ({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
    FormControlLabel: {
        padding: theme.spacing(0, 0, 0),
        margin: theme.spacing(0, 0, 0, 0),
        alignItems: "start",
    },
}));

export const CreateParameterDialog: React.FC<{
    showDialog: boolean;
    closeDialog: () => void;
    createParameter: (parameter: Parameter) => void;
}> = ({ showDialog, closeDialog, createParameter }) => {
    const { t } = useTranslation();
    const [parameterName, setParameterName] = useState<string>("");
    const [parameterValue, setParameterValue] = useState<string>("");
    const classes = useStyles();
    return (
        <Dialog
            open={showDialog}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
        >
            <DialogTitle>{t("clusterParameters.createParameterDialog.title")}</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label={t("clusterParameters.createParameterDialog.nameLabel")}
                    value={parameterName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setParameterName(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("clusterParameters.createParameterDialog.valueLabel")}
                    value={parameterValue}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setParameterValue(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    {t("common.cancel")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    color="primary"
                    disabled={!parameterName}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        createParameter({
                            key: parameterName!,
                            value: parameterValue,
                        });
                        closeDialog();
                        setParameterName("");
                        setParameterValue("");
                    }}
                >
                    {t("common.create")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
