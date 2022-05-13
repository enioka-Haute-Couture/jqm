import React, { useState } from "react";
import {
    Button,
    FormControl,
    FormGroup,
    Input,
    InputLabel,
    MenuItem,
    Select,
    Switch,
} from "@material-ui/core";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import TextField from "@material-ui/core/TextField/TextField";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import { User } from "./User";
import { KeyboardDatePicker } from "@material-ui/pickers";
import { Role } from "../Roles/Role";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        TextField: {
            padding: theme.spacing(0, 0, 3),
        },
    })
);

export const CreateUserDialog: React.FC<{
    closeDialog: () => void;
    createUser: (user: User) => void;
    roles: Role[];
}> = ({ closeDialog, createUser, roles }) => {
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
            <DialogTitle>Create user</DialogTitle>
            <DialogContent>
                <TextField
                    className={classes.TextField}
                    label="Login*"
                    value={login}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setLogin(event.target.value);
                    }}
                    fullWidth
                />
                <TextField
                    className={classes.TextField}
                    label="E-mail"
                    type="email"
                    value={email}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setEmail(event.target.value);
                    }}
                    fullWidth
                />
                <TextField
                    className={classes.TextField}
                    label="Full name"
                    value={fullName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setFullName(event.target.value);
                    }}
                    fullWidth
                />
                <FormGroup>
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
                        label="Locked"
                        labelPlacement="end"
                    />
                </FormGroup>
                <FormControl fullWidth>
                    <InputLabel id="user-roles-select-label">Roles</InputLabel>
                    <Select
                        multiple
                        labelId="user-roles-select-label"
                        fullWidth
                        value={userRoles}
                        onChange={(
                            event: React.ChangeEvent<{ value: unknown }>
                        ) => {
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
                <KeyboardDatePicker
                    disableToolbar
                    variant="inline"
                    format="dd/MM/yyyy"
                    margin="normal"
                    label="Expiration date"
                    id="date-picker-inline"
                    value={expirationDate}
                    onChange={(date) => {
                        setExpirationDate(date);
                    }}
                    KeyboardButtonProps={{
                        "aria-label": "change date",
                    }}
                />
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    Cancel
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
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    );
};
