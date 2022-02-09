import React, { useState, useEffect, useRef } from "react";
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
import { makeStyles } from "@material-ui/core/styles";
import DeleteIcon from "@material-ui/icons/Delete";
import { JndiParameter, JndiResource } from "./JndiResource";

export const JndiParametersTable: React.FC<{
    parameters: JndiParameter[];
    setParameters: (parameters: JndiParameter[]) => void;
}> = ({ parameters, setParameters }) => {
    return (
        <TableContainer component={Paper}>
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>Name</TableCell>
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

const useStyles = makeStyles({
    root: {
        justifyContent: "flex-start",
        margin: "1rem",
    },
});

export const EditParametersDialog: React.FC<{
    showDialog: boolean;
    closeDialog: () => void;
    selectedResource: JndiResource | null;
    setSelectedResource: (newResource: JndiResource) => void;
}> = ({ showDialog, closeDialog, selectedResource, setSelectedResource }) => {
    const [tmpParams, setTmpParams] = useState<JndiParameter[]>([]);
    const [newParamName, setNewParamName] = useState<string>("");
    const [newParamValue, setNewParamValue] = useState<string>("");
    const scollToRef = useRef<null | HTMLDivElement>(null);
    const classes = useStyles();

    useEffect(() => {
        if (selectedResource) {
            setTmpParams([...selectedResource.parameters]);
        }
    }, [selectedResource]);

    const clearNewParamValues = () => {
        setNewParamName("");
        setNewParamValue("");
    };

    const onCancel = () => {
        closeDialog();
        setTmpParams(selectedResource ? [...selectedResource.parameters] : []);
        clearNewParamValues();
    };

    return selectedResource !== null ? (
        <Dialog
            open={showDialog}
            onClose={onCancel}
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
                <div ref={scollToRef}></div>
            </DialogContent>
            <DialogActions classes={{ root: classes.root }}>
                <TextField
                    label="Name"
                    value={newParamName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setNewParamName(event.target.value);
                    }}
                />
                <TextField
                    label="Value"
                    value={newParamValue}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setNewParamValue(event.target.value);
                    }}
                />
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "1rem" }}
                    disabled={!newParamName || !newParamValue}
                    onClick={() => {
                        setTmpParams([
                            ...tmpParams,
                            { key: newParamName, value: newParamValue },
                        ]);
                        scollToRef.current!.scrollIntoView();
                        clearNewParamValues();
                    }}
                    color="primary"
                >
                    Add new parameter
                </Button>
            </DialogActions>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={onCancel}
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
                    Validate
                </Button>
            </DialogActions>
        </Dialog>
    ) : null;
};
