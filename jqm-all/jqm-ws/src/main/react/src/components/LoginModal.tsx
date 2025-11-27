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
import { useAuth } from "../utils/AuthService";

export const LoginModal: React.FC = () => {
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
                setError("Invalid credentials. Please try again.");
            } else {
                setUsername("");
                setPassword("");
            }
        } catch (err) {
            setError("An error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={true} disableEscapeKeyDown maxWidth="xs" fullWidth>
            <DialogTitle>Session Expired</DialogTitle>
            <form onSubmit={handleSubmit}>
                <DialogContent>
                    <Box sx={{ mb: 2 }}>
                        Your session has expired due to inactivity. Please log in
                        again to continue.
                    </Box>
                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }}>
                            {error}
                        </Alert>
                    )}
                    <TextField
                        autoFocus
                        margin="dense"
                        label="Username"
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
                        label="Password"
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
                        {loading ? "Logging in..." : "Log In"}
                    </Button>
                </DialogActions>
            </form>
        </Dialog>
    );
};
