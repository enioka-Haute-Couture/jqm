import React, { useEffect, useRef, useState } from "react";
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
import { useTranslation } from "react-i18next";
import { JndiParameter, JndiResource } from "./JndiResource";
import { JndiParametersTable } from "./JndiParametersTable";


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
    const { t } = useTranslation();
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
            <DialogTitle id="form-dialog-title">{t("jndi.editParametersDialog.title")}</DialogTitle>
            <DialogContent>
                <>
                    <TextField
                        className={classes.TextField}
                        label={`${t("jndi.editParametersDialog.name")}*`}
                        value={newParamName}
                        onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                            setNewParamName(event.target.value);
                        }}
                        fullWidth
                        variant="standard"
                    />
                    <TextField
                        className={classes.TextField}
                        label={`${t("jndi.editParametersDialog.value")}*`}
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
                        {t("jndi.editParametersDialog.addParameter")}
                    </Button>
                    <DialogContentText style={{ marginBottom: "16px" }}>
                        {t("jndi.editParametersDialog.description")}
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
                    {t("common.cancel")}
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
                    {t("common.validate")}
                </Button>
            </DialogActions>
        </Dialog>
    ) : null;
};
