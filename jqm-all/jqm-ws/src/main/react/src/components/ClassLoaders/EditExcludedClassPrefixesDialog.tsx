import {useTranslation} from "react-i18next";
import {makeStyles} from "@mui/styles";
import {
    Button,
    Dialog, DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle, IconButton,
    Paper,
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    TextField,
    Theme
} from "@mui/material";
import React, { useState } from "react";
import DeleteIcon from "@mui/icons-material/Delete";

const useStyles = makeStyles((theme: Theme) => ({
    TextField: {
        padding: theme.spacing(0, 0, 3)
    }
}));

export const EditExcludedClassPrefixesDialog: React.FC<{
    closeDialog: () => void;
    excludedClassPrefixes: string[];
    setExcludedClassPrefixes: (excludedClassPrefixes: string[]) => void;
}> = ({ closeDialog, excludedClassPrefixes, setExcludedClassPrefixes}) => {

    const { t } = useTranslation();
    const classes = useStyles();

    const [editedExcludedClassPrefixesState, setEditedExcludedClassPrefixesState] = useState(excludedClassPrefixes);

    const [newExcludedClassPrefix, setNewExcludedClassPrefix] = useState<string>("");

    const wouldExceedMaxStringLength = (dirs: string[], newDir: string) =>
        [...dirs, newDir].join(",").length > 1024;

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">{t("classLoaders.editExcludedClassPrefixesDialog.title")}</DialogTitle>
            <DialogContent>
                <DialogContentText style={{marginBottom: "8px"}}>
                    {t("classLoaders.editExcludedClassPrefixesDialog.description")}
                </DialogContentText>
                <TextField
                    className={classes.TextField}
                    label={t("classLoaders.editExcludedClassPrefixesDialog.prefixLabel")}
                    value={newExcludedClassPrefix}
                    onChange={(
                        event: React.ChangeEvent<HTMLInputElement>
                        ) => {setNewExcludedClassPrefix(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                    error={wouldExceedMaxStringLength(editedExcludedClassPrefixesState, newExcludedClassPrefix)}
                    helperText={
                        wouldExceedMaxStringLength(editedExcludedClassPrefixesState, newExcludedClassPrefix)
                            ? t("errors.maxStringLengthExceeded", { length: 1024 })
                            : undefined
                    }
                />

                <Button
                    variant="contained"
                    size="small"
                    style={{ marginBottom: "16px" }}
                    disabled={
                        editedExcludedClassPrefixesState.filter(
                            (value) => value === newExcludedClassPrefix
                        ).length > 0 || !newExcludedClassPrefix || wouldExceedMaxStringLength(editedExcludedClassPrefixesState, newExcludedClassPrefix)
                    }
                    onClick={() => {
                        setEditedExcludedClassPrefixesState([
                            ...editedExcludedClassPrefixesState,
                            newExcludedClassPrefix
                        ]);
                    }}
                    color="primary"
                >
                    {t("classLoaders.editExcludedClassPrefixesDialog.addButton")}
                </Button>
                <TableContainer component={Paper}>
                    <Table size="small" aria-label="Parameters">
                        <TableHead>
                            <TableRow>
                                <TableCell>{t("classLoaders.editExcludedClassPrefixesDialog.prefixColumn")}</TableCell>
                                <TableCell>{t("common.actions")}</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {editedExcludedClassPrefixesState.map((value, index) => (
                                <TableRow key={value}>
                                    <TableCell component="th" scope="row">
                                        {value}
                                    </TableCell>
                                    <TableCell>
                                        <IconButton
                                            color="default"
                                            aria-label={"delete"}
                                            onClick={() => {
                                                setEditedExcludedClassPrefixesState(
                                                    editedExcludedClassPrefixesState.filter(
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
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={closeDialog}
                >
                    {t("common.cancel")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={() => {
                        setExcludedClassPrefixes(editedExcludedClassPrefixesState);
                        closeDialog();
                    }}
                    color="primary"
                >
                    {t("common.validate")}
                </Button>
            </DialogActions>
        </Dialog>
    )
};
