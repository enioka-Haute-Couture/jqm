import { useState, useCallback } from "react";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { Role } from "../Roles/Role";
import { User } from "./User";

const API_URL = "/admin/user";

export const useUserAPI = () => {
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
                        `Successfully created user: ${newUser.login}`
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchUsers]
    );

    const deleteUsers = useCallback(
        async (userIds: number[]) => {
            return await Promise.all(
                userIds.map((id) => APIService.delete(`${API_URL}/${id}`))
            )
                .then(() => {
                    fetchUsers();
                    displaySuccess(
                        `Successfully deleted user${
                            userIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchUsers]
    );

    const updateUser = useCallback(
        async (user: User) => {
            return APIService.put(`${API_URL}/${user.id}`, user)
                .then(() => {
                    fetchUsers();
                    displaySuccess(`Successfully updated user ${user.login}`);
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchUsers]
    );

    const changePassword = useCallback(
        (userId: string) => async (password: string) => {
            return APIService.put(`${API_URL}/${userId}`, {
                newPassword: password,
            })
                .then(() => {
                    displaySuccess(`Successfully updated password of user`);
                })
                .catch(displayError);
        },
        [displayError, displaySuccess]
    );

    return {
        users,
        roles,
        fetchUsers,
        fetchRoles,
        createUser,
        updateUser,
        deleteUsers,
        changePassword,
    };
};
