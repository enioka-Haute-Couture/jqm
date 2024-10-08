import React, { useEffect, useRef, useState } from "react";
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
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
import { makeStyles } from "@mui/styles";
import DeleteIcon from "@mui/icons-material/Delete";
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
                                        variant="standard"
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
                                        variant="standard"
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
                                        size="large">
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

const useStyles = makeStyles((theme: Theme) => ({
    TextField: {
        padding: theme.spacing(0, 0, 3)
    }
}));

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

    useEffect(() => {
        if (selectedResource) {
            setTmpParams([...selectedResource.parameters]);
        }
    }, [selectedResource]);

    const classes = useStyles();

    return selectedResource !== null ? (
        <Dialog
            open={showDialog}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">Edit Parameters</DialogTitle>
            <DialogContent>
                <>
                    <TextField
                        className={classes.TextField}
                        label="Name*"
                        value={newParamName}
                        onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                            setNewParamName(event.target.value);
                        }}
                        fullWidth
                        variant="standard"
                    />
                    <TextField
                        className={classes.TextField}
                        label="Value*"
                        value={newParamValue}
                        onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                            setNewParamValue(event.target.value);
                        }}
                        fullWidth
                        variant="standard"
                    />
                    <Button
                        variant="contained"
                        size="small"
                        style={{ marginBottom: "16px" }}
                        disabled={!newParamName || !newParamValue}
                        onClick={() => {
                            setTmpParams([
                                ...tmpParams,
                                { key: newParamName, value: newParamValue },
                            ]);
                            scollToRef.current!.scrollIntoView();
                        }}
                        color="primary"
                    >
                        Add parameter
                    </Button>
                    <DialogContentText>
                        Add new or edit existing parameters
                    </DialogContentText>
                    <JndiParametersTable
                        parameters={tmpParams}
                        setParameters={setTmpParams}
                    />
                    <div ref={scollToRef}></div>
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
