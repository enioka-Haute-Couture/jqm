import { useCallback, useState } from "react";
import { User } from "./User";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { Role } from "../Roles/Role";
import { useTranslation } from "react-i18next";

const API_URL = "/admin/user";

export const useUserAPI = () => {
    const { t } = useTranslation();
    const [users, setUsers] = useState<User[] | null>(null);
    const [roles, setRoles] = useState<Role[] | null>(null);
    const { displayError, displaySuccess } = useNotificationService();
    const fetchRoles = useCallback(async () => {
        APIService.get("/admin/role")
            .then((response) => {
                setRoles(response);
            })
            .catch(displayError);
    }, [displayError]);

    const fetchUsers = useCallback(async () => {
        APIService.get(API_URL)
            .then((response) => {
                setUsers(response);
            })
            .catch(displayError);
    }, [displayError]);

    const createUser = useCallback(
        async (newUser: User) => {
            return APIService.post(API_URL, newUser)
                .then(() => {
                    fetchUsers();
                    displaySuccess(
                        t("users.messages.successCreate", {
                            login: newUser.login,
                        })
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchUsers, t]
    );

    const deleteUsers = useCallback(
        async (userIds: number[]) => {
            return await Promise.all(
                userIds.map((id) => APIService.delete(`${API_URL}/${id}`))
            )
                .then(() => {
                    fetchUsers();
                    displaySuccess(
                        t("users.messages.successDelete", {
                            count: userIds.length,
                        })
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchUsers, t]
    );

    const updateUser = useCallback(
        async (user: User) => {
            return APIService.put(`${API_URL}/${user.id}`, user)
                .then(() => {
                    fetchUsers();
                    displaySuccess(
                        t("users.messages.successUpdate", { login: user.login })
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchUsers, t]
    );

    const changePassword = useCallback(
        (userId: string) => async (password: string) => {
            return APIService.put(`${API_URL}/${userId}`, {
                newPassword: password,
            })
                .then(() => {
                    displaySuccess(t("users.messages.successPasswordChange"));
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, t]
    );

    const getCertificateDownloadURL = useCallback((userId: number) => {
        return `/ws${API_URL}/${userId}/certificate`;
    }, []);

    return {
        users,
        roles,
        fetchUsers,
        fetchRoles,
        createUser,
        updateUser,
        deleteUsers,
        changePassword,
        getCertificateDownloadURL,
    };
};
