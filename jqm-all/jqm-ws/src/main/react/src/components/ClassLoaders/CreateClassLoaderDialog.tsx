import React, { useState } from "react";
import { Button, FormGroup, Switch, Theme } from "@mui/material";
import { makeStyles } from "@mui/styles";
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
            <DialogTitle>Create class loader</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label="Name*"
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
                        label="Child first class loading"
                        labelPlacement="top"
                    />
                </FormGroup>
                <TextField
                    className={classes.TextField}
                    label="Hidden classes"
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
                        label="Tracing enabled"
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
                        label="Persistent"
                        labelPlacement="top"
                    />
                </FormGroup>
                <TextField
                    className={classes.TextField}
                    label="Allowed runners"
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
                    Cancel
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
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    );
};
