import React, { useState } from "react";
import { Button, FormGroup, Switch, Theme } from "@mui/material";
import { makeStyles } from "@mui/styles";
import { useTranslation } from "react-i18next";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField/TextField";
import FormControlLabel from "@mui/material/FormControlLabel";
import { ClassLoader } from "./ClassLoader";

const useStyles = makeStyles((theme: Theme) => ({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
    FormControlLabel: {
        padding: theme.spacing(0, 0, 0),
        margin: theme.spacing(0, 0, 0, 0),
        alignItems: "start",
    },
    Switch: {
        padding: theme.spacing(0, 0, 1),
    },
}));

export const CreateClassLoaderDialog: React.FC<{
    showDialog: boolean;
    closeDialog: () => void;
    createClassLoader: (classLoader: ClassLoader) => Promise<void>;
}> = ({ showDialog, closeDialog, createClassLoader }) => {
    const { t } = useTranslation();
    const [name, setName] = useState<string>("");
    const [childFirst, setChildFirst] = useState<boolean>(false);
    const [hiddenClasses, setHiddenClasses] = useState<string>("");
    const [tracingEnabled, setTracingEnabled] = useState<boolean>(false);
    const [persistent, setPersistent] = useState<boolean>(false);
    const [allowedRunners, setAllowedRunners] = useState<string>("");

    const classes = useStyles();
    return (
        <Dialog
            open={showDialog}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
        >
            <DialogTitle>{t("classLoaders.createClassLoaderDialog.title")}</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label={t("classLoaders.name")}
                    value={name}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setName(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <FormGroup className={classes.Switch}>
                    <FormControlLabel
                        className={classes.FormControlLabel}
                        control={
                            <Switch
                                checked={childFirst}
                                onChange={(
                                    event: React.ChangeEvent<HTMLInputElement>
                                ) => {
                                    setChildFirst(event.target.checked);
                                }}
                            />
                        }
                        label={t("classLoaders.createClassLoaderDialog.childFirstLabel")}
                        labelPlacement="top"
                    />
                </FormGroup>
                <TextField
                    className={classes.TextField}
                    label={t("classLoaders.hiddenClasses")}
                    value={hiddenClasses}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setHiddenClasses(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <FormGroup className={classes.Switch}>

                    <FormControlLabel
                        className={classes.FormControlLabel}
                        control={
                            <Switch
                                checked={tracingEnabled}
                                onChange={(
                                    event: React.ChangeEvent<HTMLInputElement>
                                ) => {
                                    setTracingEnabled(event.target.checked);
                                }}
                            />
                        }
                        label={t("classLoaders.createClassLoaderDialog.tracingEnabledLabel")}
                        labelPlacement="top"
                    />
                </FormGroup>
                <FormGroup className={classes.Switch}>

                    <FormControlLabel
                        className={classes.FormControlLabel}
                        control={
                            <Switch
                                checked={persistent}
                                onChange={(
                                    event: React.ChangeEvent<HTMLInputElement>
                                ) => {
                                    setPersistent(event.target.checked);
                                }}
                            />
                        }
                        label={t("classLoaders.createClassLoaderDialog.persistentLabel")}
                        labelPlacement="top"
                    />
                </FormGroup>
                <TextField
                    className={classes.TextField}
                    label={t("classLoaders.allowedRunners")}
                    value={allowedRunners}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setAllowedRunners(event.target.value);
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
                    disabled={!name}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        createClassLoader({
                            name: name!,
                            childFirst: childFirst,
                            hiddenClasses: hiddenClasses,
                            tracingEnabled: tracingEnabled,
                            persistent: persistent,
                            allowedRunners: allowedRunners,
                        });
                        closeDialog();
                        setName("");
                    }}
                >
                    {t("common.create")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
