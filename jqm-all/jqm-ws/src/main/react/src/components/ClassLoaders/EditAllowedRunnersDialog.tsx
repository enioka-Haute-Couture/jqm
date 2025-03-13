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
    Theme
} from "@mui/material";
import React, { useState } from "react";
import DeleteIcon from "@mui/icons-material/Delete";
import { makeStyles } from "@mui/styles";

const useStyles = makeStyles((theme: Theme) => ({
    TextField: {
        padding: theme.spacing(0, 0, 3)
    }
}));

export const EditAllowedRunnersDialog: React.FC<{
    closeDialog: () => void;
    allowedRunners: string[];
    setAllowedRunners: (AllowedRunners: string[]) => void;
}> = ({ closeDialog, allowedRunners, setAllowedRunners }) => {
    const classes = useStyles();

    const [editedAllowedRunners, setEditedAllowedRunners] =
        useState<string[]>(allowedRunners);

    const [newAllowedRunner, setNewAllowedRunner] = useState<string>("");
    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">Edit allowed runners</DialogTitle>
            <DialogContent>
                <>
                    <TextField
                        className={classes.TextField}
                        label="Runner*"
                        value={newAllowedRunner}
                        onChange={(
                            event: React.ChangeEvent<HTMLInputElement>
                        ) => {
                            setNewAllowedRunner(event.target.value);
                        }}
                        fullWidth
                        variant="standard"
                    />

                    <Button
                        variant="contained"
                        size="small"
                        style={{ marginBottom: "16px" }}
                        disabled={
                            editedAllowedRunners.filter(
                                (value) => value === newAllowedRunner
                            ).length > 0 || !newAllowedRunner
                        }
                        onClick={() => {
                            setEditedAllowedRunners([
                                ...editedAllowedRunners,
                                newAllowedRunner
                            ]);
                        }}
                        color="primary"
                    >
                        Add
                    </Button>
                    <TableContainer component={Paper}>
                        <Table size="small" aria-label="Parameters">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Allowed runner</TableCell>
                                    <TableCell>Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {editedAllowedRunners.map((value, index) => (
                                    <TableRow key={value}>
                                        <TableCell component="th" scope="row">
                                            {value}
                                        </TableCell>
                                        <TableCell>
                                            <IconButton
                                                color="default"
                                                aria-label={"delete"}
                                                onClick={() => {
                                                    setEditedAllowedRunners(
                                                        editedAllowedRunners.filter(
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
                        setAllowedRunners(editedAllowedRunners);
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
