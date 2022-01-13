import React, { useState } from "react";
import { Parameter } from "./Parameter";
import { Button } from "@material-ui/core";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import TextField from "@material-ui/core/TextField/TextField";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        TextField: {
            padding: theme.spacing(0, 0, 3),
        },
        FormControlLabel: {
            padding: theme.spacing(0, 0, 0),
            margin: theme.spacing(0, 0, 0, 0),
            alignItems: "start",
        },
    })
);

export const CreateParameterDialog: React.FC<{
    showDialog: boolean;
    closeDialog: () => void;
    createParameter: (parameter: Parameter) => void;
}> = ({ showDialog, closeDialog, createParameter }) => {
    const [parameterName, setParameterName] = useState<string>("");
    const [parameterValue, setParameterValue] = useState<string>("");
    const classes = useStyles();
    return (
        <Dialog
            open={showDialog}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
        >
            <DialogTitle>Create parameter</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label="Name*"
                    value={parameterName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setParameterName(event.target.value);
                    }}
                    fullWidth
                />
                <TextField
                    className={classes.TextField}
                    label="Value"
                    value={parameterValue}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setParameterValue(event.target.value);
                    }}
                    fullWidth
                />
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
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
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    );
};
