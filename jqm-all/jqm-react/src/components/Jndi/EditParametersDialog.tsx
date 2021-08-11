import React, { useState, useEffect } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions,
    Button,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    IconButton,
    TextField,
} from "@material-ui/core";
import DeleteIcon from "@material-ui/icons/Delete";
import { JndiParameter, JndiResource } from "./JndiResource";
import { isParameter } from "typescript";

export const JndiParametersTable: React.FC<{
    parameters: JndiParameter[];
    setParameters: (parameters: JndiParameter[]) => void;
}> = ({ parameters, setParameters }) => {
    return (
        <TableContainer component={Paper}>
            <Table size="small" aria-label="a dense table">
                <TableHead>
                    <TableRow>
                        <TableCell>Key</TableCell>
                        <TableCell>Value</TableCell>
                        <TableCell align="right"></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {parameters.map(
                        (
                            {
                                key: label,
                                value,
                            }: {
                                key: string;
                                value: string | number | boolean;
                            },
                            index
                        ) => (
                            <TableRow key={`${label}-${index}`}>
                                <TableCell component="th" scope="row">
                                    <TextField
                                        defaultValue={label}
                                        onChange={(evnt) => {
                                            parameters[index].key =
                                                evnt.target.value;
                                        }}
                                        fullWidth
                                        margin="normal"
                                        inputProps={{
                                            style: { fontSize: "0.875rem" },
                                        }}
                                    />
                                </TableCell>
                                <TableCell>
                                    <TextField
                                        defaultValue={value}
                                        onChange={(evnt) => {
                                            parameters[index].value =
                                                evnt.target.value;
                                        }}
                                        fullWidth
                                        margin="normal"
                                        inputProps={{
                                            style: { fontSize: "0.875rem" },
                                        }}
                                    />
                                </TableCell>
                                <TableCell>
                                    <IconButton
                                        color="default"
                                        aria-label={"delete"}
                                        onClick={() =>
                                            setParameters(
                                                parameters.filter(
                                                    (_, i) => i !== index
                                                )
                                            )
                                        }
                                    >
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        )
                    )}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

export const EditParametersDialog: React.FC<{
    showDialog: boolean;
    closeDialog: () => void;
    selectedResource: JndiResource | null;
    setSelectedResource: (newResource: JndiResource) => void;
}> = ({ showDialog, closeDialog, selectedResource, setSelectedResource }) => {
    const [tmpParams, setTmpParams] = useState<JndiParameter[]>([]);

    useEffect(() => {
        if (selectedResource) {
            setTmpParams([...selectedResource.parameters]);
        }
    }, [selectedResource]);

    return selectedResource !== null ? (
        <Dialog
            open={showDialog}
            onClose={() => closeDialog()}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">Edit Parameters</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Add new or edit existing parameters
                </DialogContentText>
                <JndiParametersTable
                    parameters={tmpParams}
                    setParameters={setTmpParams}
                />
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    style={{ marginTop: "16px", marginBottom: "1rem" }}
                    onClick={() => {
                        setTmpParams([
                            ...tmpParams,
                            { key: "name", value: "value" },
                        ]);
                    }}
                    color="primary"
                >
                    Add new parameter
                </Button>
                <Button
                    variant="contained"
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
                        selectedResource.parameters = tmpParams;
                        setSelectedResource(selectedResource);
                        closeDialog();
                    }}
                    color="primary"
                >
                    Save
                </Button>
            </DialogActions>
        </Dialog>
    ) : null;
};
