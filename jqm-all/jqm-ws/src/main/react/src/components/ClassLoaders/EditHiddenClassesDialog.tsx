import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from "@mui/material";
import React, { useState } from "react";

export const EditHiddenClassesDialog: React.FC<{
    closeDialog: () => void;
    hiddenClasses: string[];
    setHiddenClasses: (hiddenClasses: string[]) => void;
}> = ({ closeDialog, hiddenClasses, setHiddenClasses }) => {
    const [editedHiddenClasses, setEditedHiddenClasses] =
        useState<string[]>(hiddenClasses);

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">Edit hidden classes</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Each hidden class is a regexp defining classes never to load from the parent class loader.
                </DialogContentText>
                {/* <>
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
                </> */}
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
                        setHiddenClasses(editedHiddenClasses);
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
