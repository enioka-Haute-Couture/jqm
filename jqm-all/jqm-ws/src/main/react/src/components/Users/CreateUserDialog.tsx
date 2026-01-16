import React, { ReactNode, useState } from "react";
import {
    Button,
    FormControl,
    FormGroup,
    Input,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent,
    Switch,
    Theme
} from "@mui/material";
import { useTranslation } from "react-i18next";
import { makeStyles } from "@mui/styles";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField/TextField";
import FormControlLabel from "@mui/material/FormControlLabel";
import { DatePicker } from "@mui/x-date-pickers";
import { User } from "./User";
import { Role } from "../Roles/Role";

const useStyles = makeStyles((theme: Theme) =>
({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
    Switch: {
        padding: theme.spacing(0, 0, 1),
    },
    DatePicker: {
        margin: theme.spacing(3, 0, 0),
    }

})
);

export const CreateUserDialog: React.FC<{
    closeDialog: () => void;
    createUser: (user: User) => void;
    roles: Role[];
}> = ({ closeDialog, createUser, roles }) => {
    const { t } = useTranslation();
    const [login, setLogin] = useState<string>("");
    const [email, setEmail] = useState<string>("");
    const [fullName, setFullName] = useState<string>("");
    const [locked, setLocked] = useState<boolean>(false);
    const [expirationDate, setExpirationDate] = useState<Date | null>(null);
    const [userRoles, setUserRoles] = useState<number[]>([]);

    const classes = useStyles();
    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
        >
            <DialogTitle>{t("users.createUserDialog.title")}</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label={t("users.login")}
                    value={login}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setLogin(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("users.email")}
                    type="email"
                    value={email}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setEmail(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <TextField
                    className={classes.TextField}
                    label={t("users.fullName")}
                    value={fullName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setFullName(event.target.value);
                    }}
                    fullWidth
                    variant="standard"
                />
                <FormGroup
                    className={classes.Switch}>
                    <FormControlLabel
                        control={
                            <Switch
                                checked={locked}
                                onChange={(
                                    event: React.ChangeEvent<HTMLInputElement>
                                ) => {
                                    setLocked(event.target.checked);
                                }}
                            />
                        }
                        label={t("users.createUserDialog.lockedLabel")}
                        labelPlacement="end"
                    />
                </FormGroup>
                <FormControl fullWidth>
                    <InputLabel id="user-roles-select-label">{t("users.roles")}</InputLabel>
                    <Select
                        multiple
                        labelId="user-roles-select-label"
                        fullWidth
                        value={userRoles}
                        onChange={
                            (event: SelectChangeEvent<number[]>, child: ReactNode) => {
                                setUserRoles(event.target.value as number[]);
                            }}
                        input={<Input />}
                    >
                        {roles!.map((role: Role) => (
                            <MenuItem key={role.id} value={role.id}>
                                {role.name}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <DatePicker
                    className={classes.DatePicker}
                    label={t("users.expirationDate")}
                    format="dd/MM/yyyy"
                    value={expirationDate}
                    onChange={(date) => {
                        setExpirationDate(date);
                    }}
                />
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    {t("common.cancel")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    color="primary"
                    disabled={!login}
                    style={{ margin: "8px" }}
                    onClick={() => {
                        createUser({
                            login: login,
                            email: email,
                            freeText: fullName,
                            locked: locked,
                            expirationDate: expirationDate
                                ? expirationDate
                                : undefined,
                            roles: userRoles,
                        });
                        closeDialog();
                    }}
                >
                    {t("common.create")}
                </Button>
            </DialogActions>
        </Dialog >
    );
};
