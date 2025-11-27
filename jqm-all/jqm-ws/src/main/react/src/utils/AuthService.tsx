import React, {
    createContext,
    ReactNode,
    useCallback,
    useContext,
    useEffect,
    useState,
} from "react";
import APIService from "./APIService";

export enum PermissionObjectType {
    queue = "queue",
    node = "node",
    qmapping = "qmapping",
    jndi = "jndi",
    prm = "prm",
    cl = "cl",
    jd = "jd",
    user = "user",
    role = "role",
    job_instance = "job_instance",
    logs = "logs",
    queue_position = "queue_position",
    files = "files"
}

export enum PermissionAction {
    read = "read",
    create = "create",
    update = "update",
    delete = "delete"
}

interface AuthState {
    status: "LOGGED_OUT" | "LOGGING_IN" | "REFRESHING" | "LOGGED_IN" | "SESSION_EXPIRED";
    error?: string;
    userLogin?: string;
    userPermissions?: Array<string>;
}

interface AuthInfo extends AuthState {
    logout: () => void;
    canUserAccess: (objectType: string, action: PermissionAction) => boolean;
    reLogin?: (username: string, password: string) => Promise<boolean>;
    handleSessionExpired?: () => void;
}

const defaultAuthState: AuthState = {
    status: "LOGGING_IN",
    error: undefined,
    userLogin: undefined,
    userPermissions: undefined
}

const defaultAuth: AuthInfo = {
    ...defaultAuthState,
    canUserAccess: () => false,
    logout: () => { },
    reLogin: async () => false,
    handleSessionExpired: () => { },
};

const AuthContext = createContext<AuthInfo>(defaultAuth);
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [authState, setAuthState] = useState<AuthState>(defaultAuthState);

    useEffect(() => {
        getProfile();

        const handleSessionExpiredEvent = () => {
            setAuthState(prev => {
                if (prev.status !== "SESSION_EXPIRED") {
                    return { ...prev, status: "SESSION_EXPIRED" };
                }
                return prev;
            });
        };
        window.addEventListener('auth:session-expired', handleSessionExpiredEvent);
        return () => window.removeEventListener('auth:session-expired', handleSessionExpiredEvent);
    }, []);

    const getProfile = async () => {
        try {
            const result = await APIService.get("/admin/me");
            setAuthState({ userLogin: result.login, userPermissions: result.permissions, status: "LOGGED_IN" });
        } catch (e) {
            setAuthState({ status: "LOGGED_OUT" });
            console.log(e);
        }
    };

    const handleSessionExpired = useCallback(() => {
        setAuthState(prev => ({ ...prev, status: "SESSION_EXPIRED" }));
    }, []);

    const reLogin = useCallback(async (username: string, password: string) => {
        setAuthState(prev => ({ ...prev, status: "LOGGING_IN" }));
        try {
            // Use Basic Auth to login and create a new session
            const credentials = btoa(`${username}:${password}`);

            const result = await fetch(`/ws/admin/me?_=${Date.now()}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Basic ${credentials}`,
                    'Accept': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest',
                    'Cache-Control': 'no-cache',
                },
                credentials: 'same-origin',
                cache: 'no-store', // make sure we hit the server to log in
            });

            if (!result.ok) {
                throw new Error('Login failed');
            }

            const profileData = await result.json();

            setAuthState({
                userLogin: profileData.login,
                userPermissions: profileData.permissions,
                status: "LOGGED_IN"
            });

            APIService.isSessionExpired = false;

            return true;
        } catch (e) {
            console.error('Re-login failed:', e);
            setAuthState(prev => ({ ...prev, status: "SESSION_EXPIRED", error: "Invalid credentials" }));
            return false;
        }
    }, []);

    const canUserAccess = useCallback((objectType: string, action: PermissionAction) => {
        if (!authState.userPermissions) {
            return false;
        }
        if (authState.userPermissions.includes("*:*")) {
            return true;
        }
        if (authState.userPermissions.includes(`${objectType}:*`)) {
            return true;
        }

        return authState.userPermissions.includes(`${objectType}:${action}`)
    }, [authState]);

    return (
        <AuthContext.Provider
            value={{
                ...authState,
                canUserAccess: canUserAccess,
                reLogin: reLogin,
                handleSessionExpired: handleSessionExpired,
                logout: useCallback(() => setAuthState({ ...defaultAuthState, status: "LOGGED_OUT" }), []),
            }}
        >
            {authState.status === "LOGGED_IN" || authState.status === "SESSION_EXPIRED" || authState.status === "LOGGING_IN" ?
                children : null}
        </AuthContext.Provider>
    );
};
