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
    status: "LOGGED_OUT" | "LOGGING_IN" | "REFRESHING" | "LOGGED_IN";
    error?: string;
    userLogin?: string;
    userPermissions?: Array<string>;
}

interface AuthInfo extends AuthState {
    logout: () => void;
    canUserAccess: (objectType: string, action: PermissionAction) => boolean;
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
};

const AuthContext = createContext<AuthInfo>(defaultAuth);
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [authState, setAuthState] = useState<AuthState>(defaultAuthState);

    useEffect(() => {
        getProfile();
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
                logout: useCallback(() => setAuthState({ ...defaultAuthState, status: "LOGGED_OUT" }), []),
            }}
        >
            {authState.status === "LOGGED_IN" ?
                children : null}
        </AuthContext.Provider>
    );
};
