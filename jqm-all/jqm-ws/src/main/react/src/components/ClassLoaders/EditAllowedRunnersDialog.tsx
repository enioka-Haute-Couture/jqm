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
import { useTranslation } from "react-i18next";

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
    const { t } = useTranslation();
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
            <DialogTitle id="form-dialog-title">{t("classLoaders.editAllowedRunnersDialog.title")}</DialogTitle>
            <DialogContent>
                <>
                    <TextField
                        className={classes.TextField}
                        label={t("classLoaders.editAllowedRunnersDialog.runnerLabel")}
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
                        {t("classLoaders.editAllowedRunnersDialog.addButton")}
                    </Button>
                    <TableContainer component={Paper}>
                        <Table size="small" aria-label="Parameters">
                            <TableHead>
                                <TableRow>
                                    <TableCell>{t("classLoaders.editAllowedRunnersDialog.allowedRunnerColumn")}</TableCell>
                                    <TableCell>{t("common.actions")}</TableCell>
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
                    {t("common.cancel")}
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
                    {t("classLoaders.editAllowedRunnersDialog.validate")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
