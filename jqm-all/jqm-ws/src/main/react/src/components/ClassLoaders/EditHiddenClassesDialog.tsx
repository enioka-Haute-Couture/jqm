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

export const EditHiddenClassesDialog: React.FC<{
    closeDialog: () => void;
    hiddenClasses: string[];
    setHiddenClasses: (hiddenClasses: string[]) => void;
}> = ({ closeDialog, hiddenClasses, setHiddenClasses }) => {
    const classes = useStyles();

    const [editedHiddenClasses, setEditedHiddenClasses] =
        useState<string[]>(hiddenClasses);

    const [newHiddenClass, setNewHiddenClass] = useState<string>("");
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
                    A hidden class is a regexp defining classes never to load from the parent class loader.
                </DialogContentText>
                <>
                    <TextField
                        className={classes.TextField}
                        label="Regexp*"
                        value={newHiddenClass}
                        onChange={(
                            event: React.ChangeEvent<HTMLInputElement>
                        ) => {
                            setNewHiddenClass(event.target.value);
                        }}
                        fullWidth
                        variant="standard"
                    />

                    <Button
                        variant="contained"
                        size="small"
                        style={{ marginBottom: "16px" }}
                        disabled={
                            editedHiddenClasses.filter(
                                (value) => value === newHiddenClass
                            ).length > 0 || !newHiddenClass
                        }
                        onClick={() => {
                            setEditedHiddenClasses([
                                ...editedHiddenClasses,
                                newHiddenClass
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
                                    <TableCell>Hidden class</TableCell>
                                    <TableCell>Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {editedHiddenClasses.map((value, index) => (
                                    <TableRow key={value}>
                                        <TableCell component="th" scope="row">
                                            {value}
                                        </TableCell>
                                        <TableCell>
                                            <IconButton
                                                color="default"
                                                aria-label={"delete"}
                                                onClick={() => {
                                                    setEditedHiddenClasses(
                                                        editedHiddenClasses.filter(
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
