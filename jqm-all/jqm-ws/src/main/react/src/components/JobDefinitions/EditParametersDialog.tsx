import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextField,
    Theme,
} from "@mui/material";
import React, { useState } from "react";
import DeleteIcon from "@mui/icons-material/Delete";
import { makeStyles } from "@mui/styles";
import { JobDefinitionParameter } from "./JobDefinition";

const useStyles = makeStyles((theme: Theme) => ({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
}));

export const EditParametersDialog: React.FC<{
    closeDialog: () => void;
    parameters: Array<JobDefinitionParameter>;
    setParameters: (parameters: Array<JobDefinitionParameter>) => void;
}> = ({ closeDialog, parameters, setParameters }) => {
    const [editedParameters, setEditedParameters] =
        useState<Array<JobDefinitionParameter>>(parameters);

    const [key, setKey] = useState<string>("");
    const [value, setValue] = useState<string>("");

    const classes = useStyles();

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">Edit parameters</DialogTitle>
            <DialogContent>
                <>
                    <TextField
                        className={classes.TextField}
                        label="Key*"
                        value={key}
                        onChange={(
                            event: React.ChangeEvent<HTMLInputElement>
                        ) => {
                            setKey(event.target.value);
                        }}
                        fullWidth
                        variant="standard"
                    />
                    <TextField
                        className={classes.TextField}
                        label="Value"
                        value={value}
                        onChange={(
                            event: React.ChangeEvent<HTMLInputElement>
                        ) => {
                            setValue(event.target.value);
                        }}
                        fullWidth
                        variant="standard"
                    />

                    <Button
                        variant="contained"
                        size="small"
                        style={{ marginBottom: "16px" }}
                        disabled={
                            editedParameters.filter(
                                (parameter) => parameter.key === key
                            ).length > 0 || !key
                        }
                        onClick={() => {
                            setEditedParameters([
                                ...editedParameters,
                                {
                                    key: key,
                                    value: value,
                                },
                            ]);
                        }}
                        color="primary"
                    >
                        Add parameter
                    </Button>
                    <TableContainer component={Paper}>
                        <Table size="small" aria-label="Parameters">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Key</TableCell>
                                    <TableCell>Value</TableCell>
                                    <TableCell>Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {editedParameters.map((parameter, index) => (
                                    <TableRow key={parameter.key}>
                                        <TableCell component="th" scope="row">
                                            {parameter.key}
                                        </TableCell>
                                        <TableCell>{parameter.value}</TableCell>
                                        <TableCell>
                                            <IconButton
                                                color="default"
                                                aria-label={"delete"}
                                                onClick={() => {
                                                    setEditedParameters(
                                                        editedParameters.filter(
                                                            (_, i) =>
                                                                i !== index
                                                        )
                                                    );
                                                }}
                                                size="large">
                                                <DeleteIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </>
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={closeDialog}
                >
                    Cancel
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={() => {
                        setParameters(editedParameters);
                        closeDialog();
                    }}
                    color="primary"
                >
                    Validate
                </Button>
            </DialogActions>
        </Dialog>
    );
};
