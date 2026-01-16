import React, { useState } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Button,
    Alert,
    Box,
} from "@mui/material";
import { useTranslation } from "react-i18next";
import { useAuth } from "../utils/AuthService";

export const LoginModal: React.FC = () => {
    const { t } = useTranslation();
    const { reLogin, status } = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    if (status !== "SESSION_EXPIRED") {
        return null;
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const success = await reLogin!(username, password);
            if (!success) {
                setError(t("auth.invalidCredentials"));
            } else {
                setUsername("");
                setPassword("");
            }
        } catch (err) {
            setError(t("auth.loginError"));
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={true} disableEscapeKeyDown maxWidth="xs" fullWidth>
            <DialogTitle>{t("auth.sessionExpired")}</DialogTitle>
            <form onSubmit={handleSubmit}>
                <DialogContent>
                    <Box sx={{ mb: 2 }}>
                        {t("auth.sessionExpiredMessage")}
                    </Box>
                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }}>
                            {error}
                        </Alert>
                    )}
                    <TextField
                        autoFocus
                        margin="dense"
                        label={t("auth.username")}
                        type="text"
                        fullWidth
                        variant="outlined"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        disabled={loading}
                        required
                    />
                    <TextField
                        margin="dense"
                        label={t("auth.password")}
                        type="password"
                        fullWidth
                        variant="outlined"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        disabled={loading}
                        required
                    />
                </DialogContent>
                <DialogActions>
                    <Button
                        type="submit"
                        variant="contained"
                        color="primary"
                        disabled={loading || !username || !password}
                    >
                        {loading ? t("auth.loggingIn") : t("auth.logIn")}
                    </Button>
                </DialogActions>
            </form>
        </Dialog>
    );
};
