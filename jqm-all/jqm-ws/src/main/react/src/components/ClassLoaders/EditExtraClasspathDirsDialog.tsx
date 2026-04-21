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
import { useTranslation } from "react-i18next";

const useStyles = makeStyles((theme: Theme) => ({
    TextField: {
        padding: theme.spacing(0, 0, 3)
    }
}));

export const EditExtraClasspathDirsDialog: React.FC<{
    closeDialog: () => void;
    extraClasspathDirs: string[];
    setExtraClasspathDirs: (extraClasspathDirs: string[]) => void;
}> = ({ closeDialog, extraClasspathDirs, setExtraClasspathDirs }) => {
    const { t } = useTranslation();
    const classes = useStyles();

    const [editedExtraClasspathDirs, setEditedExtraClasspathDirs] =
        useState<string[]>(extraClasspathDirs);

    const [newExtraClasspathDir, setNewExtraClasspathDir] = useState<string>("");

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
            <DialogTitle id="form-dialog-title">{t("classLoaders.editExtraClasspathDirsDialog.title")}</DialogTitle>
            <DialogContent>
                <DialogContentText style={{ marginBottom: "8px" }}>
                    {t("classLoaders.editExtraClasspathDirsDialog.description")}
                </DialogContentText>
                <TextField
                    className={classes.TextField}
                    label={t("classLoaders.editExtraClasspathDirsDialog.dirLabel")}
                    value={newExtraClasspathDir}
                    onChange={(
                        event: React.ChangeEvent<HTMLInputElement>
                    ) => {
                        setNewExtraClasspathDir(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                    error={wouldExceedMaxStringLength(editedExtraClasspathDirs, newExtraClasspathDir)}
                    helperText={
                        wouldExceedMaxStringLength(editedExtraClasspathDirs, newExtraClasspathDir)
                            ? t("errors.maxStringLengthExceeded", { length: 1024 })
                            : undefined
                    }
                />

                <Button
                    variant="contained"
                    size="small"
                    style={{ marginBottom: "16px" }}
                    disabled={
                        editedExtraClasspathDirs.filter(
                            (value) => value === newExtraClasspathDir
                        ).length > 0 || !newExtraClasspathDir || wouldExceedMaxStringLength(editedExtraClasspathDirs, newExtraClasspathDir)
                    }
                    onClick={() => {
                        setEditedExtraClasspathDirs([
                            ...editedExtraClasspathDirs,
                            newExtraClasspathDir
                        ]);
                    }}
                    color="primary"
                >
                    {t("classLoaders.editExtraClasspathDirsDialog.addButton")}
                </Button>
                <TableContainer component={Paper}>
                    <Table size="small" aria-label="Parameters">
                        <TableHead>
                            <TableRow>
                                <TableCell>{t("classLoaders.editExtraClasspathDirsDialog.dirColumn")}</TableCell>
                                <TableCell>{t("common.actions")}</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {editedExtraClasspathDirs.map((value, index) => (
                                <TableRow key={value}>
                                    <TableCell component="th" scope="row">
                                        {value}
                                    </TableCell>
                                    <TableCell>
                                        <IconButton
                                            color="default"
                                            aria-label={"delete"}
                                            onClick={() => {
                                                setEditedExtraClasspathDirs(
                                                    editedExtraClasspathDirs.filter(
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
                        setExtraClasspathDirs(editedExtraClasspathDirs);
                        closeDialog();
                    }}
                    color="primary"
                >
                    {t("common.validate")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
