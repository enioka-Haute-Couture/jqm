import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    TextField,
    Theme,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { JobDefinitionTags } from "./JobDefinition";

const useStyles = makeStyles((theme: Theme) => ({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
}));

export const EditTagsDialog: React.FC<{
    closeDialog: () => void;
    tags: JobDefinitionTags;
    setTags: (tags: JobDefinitionTags) => void;
}> = ({ closeDialog, tags, setTags }) => {
    const { t } = useTranslation();
    const [application, setApplication] = useState<string | undefined>(
        tags.application
    );
    const [module, setModule] = useState<string | undefined>(tags.module);
    const [keyword1, setKeyword1] = useState<string | undefined>(tags.keyword1);
    const [keyword2, setKeyword2] = useState<string | undefined>(tags.keyword2);
    const [keyword3, setKeyword3] = useState<string | undefined>(tags.keyword3);

    const classes = useStyles();

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">{t("jobDefinitions.editTagsDialog.title")}</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    {t("jobDefinitions.editTagsDialog.description")}
                </DialogContentText>
                <TextField
                    className={classes.TextField}
                    label={t("jobDefinitions.editTagsDialog.applicationLabel")}
                    value={application}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setApplication(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("jobDefinitions.editTagsDialog.moduleLabel")}
                    value={module}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setModule(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("jobDefinitions.editTagsDialog.keyword1Label")}
                    value={keyword1}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setKeyword1(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("jobDefinitions.editTagsDialog.keyword2Label")}
                    value={keyword2}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setKeyword2(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("jobDefinitions.editTagsDialog.keyword3Label")}
                    value={keyword3}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setKeyword3(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
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
                        setTags({
                            application: application,
                            module: module,
                            keyword1: keyword1,
                            keyword2: keyword2,
                            keyword3: keyword3,
                        });
                        closeDialog();
                    }}
                    color="primary"
                >
                    {t("jndi.editParametersDialog.validate")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
